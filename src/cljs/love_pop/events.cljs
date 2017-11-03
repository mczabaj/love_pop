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

(defn change-state [state-map state-path db transition]
  (if transition
    (let [current-state (get-in db state-path)
          new-state (get-in state-map [current-state transition])]
      (if new-state
        (assoc-in db state-path new-state)
        db))
    (.log js/console "new-state cannot process nil transition")))

(def new-state (partial change-state
                        state-transitions
                        [:orders :ui/state]))

(defn init-state [{:keys [db]} _]
  (println "init stated")
  {:db (-> db
         (assoc-in [:orders :completed] 0)
         (assoc-in [:orders :cards-completed] 0)
         (assoc-in [:workstations] (utils/set-workstations)))})

(defn- quantity-in-order [rows]
  (apply + (map (fn [r] (:quantity r)) rows)))

(defn add-order [{:keys [db]} _]
  (let [order (utils/gen-order)
        current-orders (get-in db [:orders :list])
        rows  (get-in order [:order :rows])
        total-in-q (quantity-in-order rows)]
    {:db (-> db
           (assoc-in [:orders :list] (conj current-orders order))
           (assoc-in [:workstations :select-paper :waiting] total-in-q)
           (new-state :order-added))
     :dispatch [:workstation/select-paper]}))
     ;; Test sleep call :dispatch [:test/timer]}))

(defn process-station [db current-event-kw next-event-kw]
  (let [rows-to-process (get-in db [:orders :list :order :rows])
        items-to-do     (get-in db [:workstations current-event-kw :waiting])
        time-to-do      (get-in db [:workstations current-event-kw :complete-time])
        batch-size      (get-in db [:workstations current-event-kw :count])
        new-count       (- items-to-do batch-size)]
    (println "event:" (name current-event-kw))
    (if (> items-to-do 0)
      (do (println "still here " items-to-do)
          (lp-ajax/curl (assoc-in db [:workstations current-event-kw :waiting] (if (pos? new-count) new-count 0)) ;;new db
                        "/sleep"
                        {:seconds time-to-do} ;;params
                        (keyword "workstation" (name current-event-kw)) ;;success-event
                        (keyword "workstation" (name current-event-kw)) ;;fail-event
                        15000 ;;timeout
                        :get))
      ;;else
      (do (println "moving on to " (name next-event-kw))
          ;; dispatch to step-complete to update total-section
          {:db (assoc-in db [:workstations (keyword (name next-event-kw)) :waiting] (quantity-in-order rows-to-process))
           :dispatch [next-event-kw]}))))

(defn select-paper [{:keys [db]} [event _]]
  (process-station db (keyword (name event)) :workstation/lazer-cut))

(defn lazer-cut [{:keys [db]} [event _]]
  (process-station db (keyword (name event)) :workstation/assemble-sculpture))

(defn assemble-sculpture [{:keys [db]} [event _]]
  (process-station db (keyword (name event)) :workstation/assemble-card))

(defn assemble-card [{:keys [db]} [event _]]
  (process-station db (keyword (name event)) :workstation/pack-mail-order))

(defn pack-mail-order [{:keys [db]} [event _]]
  (process-station db (keyword (name event)) :orders/finished))

(defn finished [db _]
  (let [rows      (get-in db [:orders :list :order :rows])
        completed (get-in db [:orders :completed])
        cards     (get-in db [:orders :cards-completed])]
    (-> db (assoc-in [:orders :completed] (+ completed (count rows)))
           (assoc-in [:orders :cards-completed] (+ cards (quantity-in-order rows))))))

(reg-event-fx :orders/init-state init-state)
(reg-event-fx :orders/add-order add-order)

(reg-event-fx :workstation/select-paper select-paper)

(reg-event-fx :workstation/lazer-cut lazer-cut)
(reg-event-fx :workstation/assemble-sculpture assemble-sculpture)
(reg-event-fx :workstation/assemble-card assemble-card)
(reg-event-fx :workstation/pack-mail-order pack-mail-order)

(reg-event-db :orders/finished finished)




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
