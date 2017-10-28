(ns love-pop.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [markdown.core :refer [md->html]]
            [love-pop.events]
            [love-pop.subscriptions]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn home-page []
  [:div.container
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

;; Order page View components

(defn add-button []
  [:input.btn.btn-primary {:value "Add Order"
                           :type "button"
                           :on-click #(rf/dispatch [:orders/add-order])}])

(defn workstation [name-kw]
  [:div.col-md-2
    [:p (name name-kw)]
    [:span
      [:p (str "Cards Waiting: " @(rf/subscribe [(keyword "workstations" (str (name name-kw) "-waiting"))]))]
      [:p (str "Workstations: " @(rf/subscribe [(keyword "workstations" (str (name name-kw) "-count"))]))]
      [:p (str "Time to Complete: "  @(rf/subscribe [(keyword "workstations" (str (name name-kw) "-complete-time"))]))]]])

(defn status-row []
  [:div.row
    [workstation :select-paper]
    [workstation :lazer-cut]
    [workstation :assemble-sculpture]
    [workstation :assemble-card]
    [workstation :pack-order]
    [workstation :mail-order]])

(defn total-section []
  [:div.row
    [:span
      [:p "Total Orders Processed"]
      [:p @(rf/subscribe [:orders/total-orders])]]
    [:span
      [:p "Total Cards Ordered and Created"]
      [:p @(rf/subscribe [:orders/total-cards])]]])

(defn orders-page []
  (rf/dispatch [:orders/init-state])
  (fn []
    [:div.container
      [:h3 "Click Add Order to start the simulation."]
      [:p "You can click Add Order again to generate additional orders."]
      [add-button] [:span @(rf/subscribe [:order/state])]
      [:span
        [:p "Orders: " @(rf/subscribe [:orders/list])]
        [:p "Order rows:  " @(rf/subscribe [:orders/rows])]]
      [status-row]
      [total-section]]))
