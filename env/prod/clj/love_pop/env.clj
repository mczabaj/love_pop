(ns love-pop.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[love_pop started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[love_pop has shut down successfully]=-"))
   :middleware identity})
