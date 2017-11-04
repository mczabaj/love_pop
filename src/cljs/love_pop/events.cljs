(ns love-pop.events
  (:require [ajax.core :as ajax]
            [love-pop.ajax :as lp-ajax]
            [love-pop.db :as db]
            [love-pop.utils :as utils]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]))

;; dispatchers and default events

(reg-event-db :initialize-db
  (fn [_ _] db/default-db))

(reg-event-db :set-active-page
  (fn [db [_ page]] (assoc db :page page)))

(reg-event-db :set-docs
  (fn [db [_ docs]] (assoc db :docs docs)))

;; love-pop specific events

;; current state -> event -> new state
(def state-transitions
  {;; start state
   nil                 {:started       :select-paper}
   ;; intermediate states
   :select-paper       {:step-complete :lazer-cut}
   :lazer-cut          {:step-complete :assemble-sculpture}
   :assemble-sculpture {:step-complete :assemble-card}
   :assemble-card      {:step-complete :pack-mail-order}
   :pack-mail-order    {:step-complete :finished}
   ;; terminal state
   :finished            nil})

(defn change-state [state-map db state-path transition]
  (if transition
    (let [current-state (get-in db state-path)
          new-state (get-in state-map [current-state transition])]
      (if new-state
        (assoc-in db state-path new-state)
        db))
    (.log js/console "new-state cannot process nil transition")))

(def new-state (partial change-state
                        state-transitions))
                        ;;[:orders <the id> :process-state]

(defn init-state [{:keys [db]} _]
  (println "init stated")
  {:db (-> db
           (assoc-in [:orders :completed] 0)
           (assoc-in [:orders :cards-completed] 0)
           (assoc-in [:workstations] (utils/set-workstations)))})

(defn- quantity-in-order [rows]
  (apply + (map (fn [r] (:quantity r)) rows)))

(defn add-order [db _]
  (let [order (utils/gen-order)
        current-orders (get-in db [:orders :backlog])]
    (assoc-in db [:orders :backlog] (conj current-orders order))))

(defn update-order-state [db id new-state]
  (println "the processing order " (get-in db [:orders :backlog id]))
  (assoc-in db [:orders :backlog id :process-state] new-state))

(defn start [{:keys [db]} _]
  (println "backlog " (get-in db [:orders :backlog]))
  (let [orders      (get-in db [:orders :backlog])
        first-order (if (list? orders) (last orders) (last (list orders)))
        id          (do (println "first order" first-order) (ffirst first-order))
        rows        (do (println "first id" id) (get-in first-order [id :rows]))
        total-in-q  (quantity-in-order rows)]
    (if (= 0 (count orders))
      {:db (assoc-in db [:orders :started] false)}
      {:db (-> db
               (assoc-in [:orders :started] true)
               (assoc-in [:workstations :select-paper :waiting] total-in-q)
               (assoc-in [:workstations :select-paper :processing] id))
       :dispatch [:workstation/select-paper]})))

(defn order-to-process [db workstation]
  (let [orders         (get-in db [:orders :backlog])
        current-order  (filter (fn [[k,v]] (= workstation (get-in v [:process-state]))))]
    db))

(defn process-station [db current-event-kw next-event-kw]
  (let [id               (get-in db [:workstations current-event-kw :processing])
        orders           (into {} (get-in db [:orders :backlog]))
        rows-to-process  (get-in orders [id :rows])
        items-to-do      (get-in db [:workstations current-event-kw :waiting])
        time-to-do       (get-in db [:workstations current-event-kw :complete-time])
        batch-size       (get-in db [:workstations current-event-kw :count])
        new-count        (- items-to-do batch-size)]
    (if (> items-to-do 0)
      (do (println "still here " items-to-do)
          (lp-ajax/curl (assoc-in db [:workstations current-event-kw :waiting] (if (pos? new-count) new-count 0)) ;;new db
                        "/sleep"
                        {:seconds time-to-do} ;;params
                        (keyword "workstation" (name current-event-kw)) ;;success-event
                        (keyword "workstation" (name current-event-kw)) ;;fail-event
                        11000 ;;timeout
                        :get))
      ;; else
      (do (println "moving on to" (name next-event-kw))
          ;; dispatch to step-complete to update total-section
          {:db (if (= next-event-kw :orders/finished)
                 db
                 (-> db
                     (assoc-in [:workstations (keyword (name next-event-kw)) :waiting] (quantity-in-order rows-to-process))
                     (assoc-in [:workstations (keyword (name next-event-kw)) :processing] id)
                     (dissoc   [:workstations current-event-kw] :processing)))
           :dispatch [next-event-kw]}))))

(defn select-paper [{:keys [db]} [event _]]
  (println "selecting paper")
  (process-station db (keyword (name event)) :workstation/lazer-cut))

(defn lazer-cut [{:keys [db]} [event _]]
  (println "lazer cutting")
  (process-station db (keyword (name event)) :workstation/assemble-sculpture))

(defn assemble-sculpture [{:keys [db]} [event _]]
  (println "assembling sculpture")
  (process-station db (keyword (name event)) :workstation/assemble-card))

(defn assemble-card [{:keys [db]} [event _]]
  (println "assembling card")
  (process-station db (keyword (name event)) :workstation/pack-mail-order))

(defn pack-mail-order [{:keys [db]} [event _]]
  (println "packing and mailing")
  (process-station db (keyword (name event)) :orders/finished))

(defn finished [{:keys [db]} _]
  (let [id        (get-in db [:workstations :pack-mail-order :processing])
        orders    (into {} (get-in db [:orders :backlog]))
        rows      (get-in orders [id :rows])
        completed (get-in db [:orders :completed])
        cards     (get-in db [:orders :cards-completed])
        new-orders (dissoc orders id)]
    {:db (-> db
                      (assoc-in [:orders :completed] (+ completed (count rows)))
                      (assoc-in [:orders :cards-completed] (+ cards (quantity-in-order rows)))
                      (assoc-in [:orders :backlog] new-orders))
     :dispatch [:orders/start]}))

(reg-event-fx :orders/init-state init-state)
(reg-event-db :orders/add-order add-order)
(reg-event-fx :orders/start start)

(reg-event-fx :workstation/select-paper select-paper)
(reg-event-fx :workstation/lazer-cut lazer-cut)
(reg-event-fx :workstation/assemble-sculpture assemble-sculpture)
(reg-event-fx :workstation/assemble-card assemble-card)
(reg-event-fx :workstation/pack-mail-order pack-mail-order)

(reg-event-fx :orders/finished finished)




;; Sleep api test with FX updating UI
(defn timer [{:keys [db]} _]
  (let [uri "/sleep"
        ndb (assoc-in db [:test :counter] 1)]
    (lp-ajax/curl ndb
                  uri
                  {:seconds 3} ;;params
                  :test/timer2 ;;success-event
                  :test/timer2 ;;fail-event
                  5000 ;;timeout
                  :get)))

(defn timer2 [{:keys [db]} _]
  (let [uri "/sleep"
        ndb (assoc-in db [:test :counter] 2)]
    (lp-ajax/curl ndb
                  uri
                  {:seconds 3} ;;params
                  :test/timer3 ;;success-event
                  :test/timer3 ;;fail-event
                  5000 ;;timeout
                  :get)))

(defn timer3 [{:keys [db]} _]
  (let [uri "/sleep"
        ndb (assoc-in db [:test :counter] 3)]
    (lp-ajax/curl ndb
                  uri
                  {:seconds 3} ;;params
                  :test/timer4 ;;success-event
                  :test/timer4 ;;fail-event
                  5000 ;;timeout
                  :get)))

(defn timer4 [{:keys [db]} _]
  (let [uri "/sleep"
        ndb (assoc-in db [:test :counter] 4)]
    (lp-ajax/curl ndb
                  uri
                  {:seconds 3} ;;params
                  :test/done ;;success-event
                  :test/done ;;fail-event
                  5000 ;;timeout
                  :get)))


(defn done [db _]
  (assoc-in db [:test :counter] 5))

(reg-event-fx :test/timer  timer)
(reg-event-fx :test/timer2 timer2)
(reg-event-fx :test/timer3 timer3)
(reg-event-fx :test/timer4 timer4)
(reg-event-db :test/done done)
