(ns brautag.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [brautag.layout :refer [error-page]]
            [brautag.routes.home :refer [home-routes]]
            [brautag.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [brautag.env :refer [defaults]]
            [mount.core :as mount]
            [brautag.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
          #'service-routes
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))
