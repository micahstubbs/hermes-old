(ns dev.db
  (:require [hermes.db :as db]
            [ragtime.sql.files :refer [migrations]]
            [ragtime.core :refer [migrate-all rollback]]
            [ragtime.sql.database :refer [->SqlDatabase]]))

(def ragtime-db (merge (->SqlDatabase) (db/database)))

(defn reset-db! []
  (doseq [m (reverse (migrations))]
    (rollback ragtime-db m)))

(defn run-migrations! []
  (migrate-all ragtime-db (migrations)))
