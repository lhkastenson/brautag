(ns user
  (:require [luminus-migrations.core :as migrations]
            [brautag.config :refer [env]]
            [mount.core :as mount]
            [brautag.figwheel :refer [start-fw stop-fw cljs]]
            [brautag.core :refer [start-app]]))

(defn start []
  (mount/start-without #'brautag.core/repl-server))

(defn stop []
  (mount/stop-except #'brautag.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn reset-db []
  (migrations/migrate ["reset"] {:database-url (:config-database-url env)}))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


