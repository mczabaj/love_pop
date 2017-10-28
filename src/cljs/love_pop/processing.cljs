(ns love-pop.processing)

;; Processing Stuff

(defn sleep [msec]
  (let [deadline (+ msec (.getTime (js/Date.)))]
    (while (> deadline (.getTime (js/Date.))))))

(defn process [s]
  (sleep (* s 1000)))
