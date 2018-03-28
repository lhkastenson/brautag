(ns brautag.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [brautag.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[brautag started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[brautag has shut down successfully]=-"))
   :middleware wrap-dev})
