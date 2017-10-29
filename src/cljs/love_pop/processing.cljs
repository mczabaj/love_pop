(ns love-pop.processing
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require [re-frame.core :as rf]
            [cljs.core.async :refer [<! timeout]]))

;; Processing Stuff
(defn sleep [msec]
  (go
    (<! (timeout 3000))))

;
; (defn sleep [msec]
;   (let [deadline (+ msec (.getTime (js/Date.)))]
;     (while (> deadline (.getTime (js/Date.))))))

(defn process-time [s]
  (sleep (* s 1000)))
