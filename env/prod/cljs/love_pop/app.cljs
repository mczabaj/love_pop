(ns love-pop.app
  (:require [love-pop.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
