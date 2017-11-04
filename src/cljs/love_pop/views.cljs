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

(defn add-button [text event-kw]
  [:input.btn.btn-primary {:value text
                           :type "button"
                           :on-click #(rf/dispatch [event-kw])}])

(defn workstation [name-kw]
  [:div.col-md-2
    [:p [:strong (name name-kw)]]
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
    [workstation :pack-mail-order]])

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
      ;; test timeout [:h2 @(rf/subscribe [:test/counter])]
      [:h3 "Click Add Order to start the simulation."]
      [:p "You can click Add Order again to generate additional orders."]
      [add-button "Add Order" :orders/add-order]
      (if (not @(rf/subscribe [:orders/started])) [add-button "Start Processing" :orders/start])
      [:span
        [:p "Orders: " @(rf/subscribe [:orders/backlog])]]
      [status-row]
      [total-section]]))
