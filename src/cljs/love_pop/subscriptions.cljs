(ns love-pop.subscriptions
  (:require [love-pop.db :as db]
            [re-frame.core :refer [reg-sub]]))

;;subscriptions

(reg-sub :page
  (fn [db _] (:page db)))

(reg-sub :docs
  (fn [db _] (:docs db)))

(reg-sub :orders/simulation-started?
  (fn [db _] (get-in db [:orders :sim-started])))

(reg-sub :orders/list
  (fn [db _]
    (let [rows (get-in db [:orders :list :order :rows] "No Orders")]
      (if (= rows "No Orders")
        rows
        (sort-by :priority < rows)))))
