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

(defn- reformat-keyword [kw]
  (-> kw
      name
      (clojure.string/replace \_ \-)
      keyword))

(defn normalize-keys
  [entity]
  (if (nil? entity)
    nil
    (into {} (for [[k v] entity]
               [(reformat-keyword k) v]))))
