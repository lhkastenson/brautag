(ns brautag.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[brautag started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[brautag has shut down successfully]=-"))
   :middleware identity})
