(ns love-pop.subscriptions
  (:require [love-pop.db :as db]
            [re-frame.core :refer [reg-sub] :as rf]))

;;subscriptions

(reg-sub :page
  (fn [db _] (:page db)))

(reg-sub :docs
  (fn [db _] (:docs db)))

(reg-sub :orders/simulation-started?
  (fn [db _] (get-in db [:orders :sim-started])))

(reg-sub :orders/list
  (fn [db _]
    (let [orders (get-in db [:orders :list] nil)]
      (if (nil? orders) "No Orders" (str orders)))))

(reg-sub :orders/rows
  (fn [db _]
    (let [rows (get-in db [:orders :list :order :rows] "No Rows")]
      (if (= "No Rows" rows)
        rows
        (sort-by :priority < rows)))))

(reg-sub :orders/total-orders
  (fn [db _]
    (get-in db [:orders :completed])))

(reg-sub :orders/total-cards
  (fn [db _]
    (get-in db [:orders :cards-completed])))

(def workstations [:select-paper :lazer-cut :assemble-sculpture
                   :assemble-card :pack-order :mail-order])

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-count"))
    (fn [db _] (get-in db [:workstations ws :count]))))

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-complete-time"))
    (fn [db _] (get-in db [:workstations ws :complete-time]))))

(doseq [ws workstations]
  (reg-sub (keyword "workstations" (str (name ws) "-waiting"))
    (fn [db _] (get-in db [:workstations ws :waiting] 0))))

(reg-sub :order/state
  (fn [db _] (get-in db [:orders :ui/state])))
