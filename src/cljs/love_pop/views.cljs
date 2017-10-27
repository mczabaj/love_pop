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

(defn status-row [])

(defn status-section [])


(defn orders-page []
  [:div.container
    [:h3 "Click Add Order to start the simulation."]
    [:p "You can click Add Order again to generate additional orders."]
    [add-button]
    [:div (str @(rf/subscribe [:orders/list]))]])
