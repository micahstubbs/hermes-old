(ns hermes.db
  (:require [turbovote.resource-config :refer [config]]
            [ragtime.sql.files :refer [migrations]]
            [ragtime.core :refer [migrate-all rollback]]
            [ragtime.sql.database :refer [->SqlDatabase]]))

(defn database
  []
  (config :db))

(def ragtime-db (merge (->SqlDatabase) (database)))

(defn reset-db! []
  (doseq [m (reverse (migrations))]
    (rollback ragtime-db m)))

(defn run-migrations! []
  (migrate-all ragtime-db (migrations)))
