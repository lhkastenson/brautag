(ns brautag.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [brautag.core-test]))

(doo-tests 'brautag.core-test)

