(ns love-pop.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [love-pop.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[love_pop started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[love_pop has shut down successfully]=-"))
   :middleware wrap-dev})
