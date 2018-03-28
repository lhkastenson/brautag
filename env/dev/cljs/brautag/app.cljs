(ns ^:figwheel-no-load brautag.app
  (:require [brautag.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
