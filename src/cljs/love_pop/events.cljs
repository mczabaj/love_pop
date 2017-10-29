(ns love-pop.events
  (:require [love-pop.db :as db]
            [love-pop.processing :as p]
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
   nil                 {:order-added   :select-paper}
   ;; intermediate states
   :select-paper       {:step-complete :lazer-cut}
   :lazer-cut          {:step-complete :assemble-sculpture}
   :assemble-sculpture {:step-complete :assemble-card}
   :assemble-card      {:step-complete :pack-order}
   :pack-order         {:step-complete :mail-order}
   :mail-order         {:step-complete :finished}
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

(defn step-complete [db _]
  (new-state db :step-complete))

(defn init-state [{:keys [db]} _]
  (println "init stated")
  {:db (-> db
         (assoc-in [:orders :ui/state] nil)
         (assoc-in [:orders :completed] 0)
         (assoc-in [:orders :cards-completed] 0)
         (assoc-in [:workstations] (utils/set-workstations)))})

(defn add-order [{:keys [db]} _]
  (let [order (utils/gen-order)]
    {:db (-> db
           (assoc-in [:orders :list] order)
           (new-state :order-added))
     :dispatch [:orders/select-paper order]}))

(defn select-paper [db [_ order]]
  (let [rows  (get-in order [:order :rows])
        tot-q (apply + (map (fn [r] (:quantity r)) rows))]
    (assoc-in db [:workstations :select-paper :waiting] tot-q)))

(defn process [db ws])


(reg-event-fx :orders/init-state init-state)
(reg-event-fx :orders/add-order add-order)

(reg-event-db :orders/step-complete step-complete)

(reg-event-db :orders/select-paper select-paper)

(reg-event-db :process process)
; (reg-event-fx :orders/lazer-cut lazer-cut)
; (reg-event-fx :orders/assemble-sculpture assemble-sculpture)
; (reg-event-fx :orders/assemble-card assemble-card)
; (reg-event-fx :orders/pack-order pack-order)
; (reg-event-fx :orders/mail-order mail-order)
; (reg-event-fx :orders/finished finish)
