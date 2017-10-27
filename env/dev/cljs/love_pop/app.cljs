(ns ^:figwheel-no-load love-pop.app
  (:require [love-pop.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
