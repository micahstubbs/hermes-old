(ns hermes.entities.account
  (:require [clojure.java.jdbc :as jdbc]
            [hermes.db :as db]))

(defn create-account
  [db name location contact-email]
  (-> (jdbc/insert! db :accounts
                    {:account_id (java.util.UUID/randomUUID)
                     :name name
                     :location location
                     :contact_email contact-email})
      first
      db/normalize-keys))

(defn list-accounts
  [db]
  (map db/normalize-keys (jdbc/query db ["select * from accounts where active = true"])))

(defn find-by-id
  [db account-id]
  (-> (jdbc/query db

                  ["select * from accounts where account_id = ? and active = true" account-id])
      first
      db/normalize-keys))

(defn find-by-token
  [db api-token]
  (-> (jdbc/query db
                  ["select * from accounts where api_token = ? and active = true" api-token])
      first
      db/normalize-keys))

(defn delete-account
  [db account-id]
  (first (jdbc/update! db :accounts {:active false}
                       ["account_id = ?" account-id])))
