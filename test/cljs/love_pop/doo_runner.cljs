(ns love-pop.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [love-pop.core-test]))

(doo-tests 'love-pop.core-test)

