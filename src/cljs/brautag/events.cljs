(ns brautag.events
  (:require [brautag.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(reg-event-db
 :start-timer
 (fn [db [_]]
   (assoc db :timer-start? true)))

(reg-event-db
 :stop-timer
 (fn [db [_]]
   (assoc db :timer-start? false)))

(reg-event-db
 :reset-timer
 (fn [db [_]]
   (assoc db :timer-start? false :timer-val 3600)))

(reg-event-db
 :update-timer
 (fn [db [_ timer-val]]
   (assoc db :timer-val (dec timer-val))))

;;subscriptions
(reg-sub
 :app-db
 (fn [db]
   db))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
 :timer-start?
 (fn [db _]
   (:timer-start? db)))

(reg-sub
 :timer-val
 (fn [db _]
   (:timer-val db)))
