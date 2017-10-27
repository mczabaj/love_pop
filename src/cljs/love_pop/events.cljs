(ns love-pop.events
  (:require [love-pop.db :as db]
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
   :nil                {:order-added   :order-started}
   ;; intermediate states
   :order-started      {:step-complete :select-paper}
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
(defn init-state [{:keys [db]} _]
  (println "init stated")
  {:db (assoc-in db [:workstations] (utils/set-workstations))})

(defn add-order
  [db _]
  (-> db
    (assoc-in [:orders :list] (utils/gen-order))
    (new-state :order-added)))

(reg-event-fx :orders/init-state init-state)
(reg-event-db :orders/add-order add-order)
