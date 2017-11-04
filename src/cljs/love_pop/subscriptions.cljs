(ns love-pop.subscriptions
  (:require [love-pop.db :as db]
            [re-frame.core :refer [reg-sub] :as rf]))

;;subscriptions

(reg-sub :page
  (fn [db _] (:page db)))

(reg-sub :docs
  (fn [db _] (:docs db)))

(reg-sub :orders/started
  (fn [db _]
    (get-in db [:orders :started] false)))

(reg-sub :orders/backlog
  (fn [db _]
    (let [orders (into {} (get-in db [:orders :backlog]))]
      (if (nil? orders) "No Orders" (str orders)))))

(reg-sub :orders/total-orders
  (fn [db _]
    (get-in db [:orders :completed])))

(reg-sub :orders/total-cards
  (fn [db _]
    (get-in db [:orders :cards-completed])))

(def workstations [:select-paper :lazer-cut :assemble-sculpture
                   :assemble-card :pack-mail-order])

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-count"))
    (fn [db _] (get-in db [:workstations ws :count]))))

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-complete-time"))
    (fn [db _] (get-in db [:workstations ws :complete-time]))))

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-waiting"))
    (fn [db _] (get-in db [:workstations ws :waiting] 0))))

;;test timeout
(reg-sub :test/counter
  (fn [db _] (get-in db [:test :counter] 0)))
