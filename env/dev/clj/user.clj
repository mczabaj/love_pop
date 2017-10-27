(ns user
  (:require 
            [mount.core :as mount]
            [love-pop.figwheel :refer [start-fw stop-fw cljs]]
            love-pop.core))

(defn start []
  (mount/start-without #'love-pop.core/repl-server))

(defn stop []
  (mount/stop-except #'love-pop.core/repl-server))

(defn restart []
  (stop)
  (start))


