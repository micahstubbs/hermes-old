(ns hermes.entities.account
  (:require [clojure.java.jdbc :as jdbc]))

(def replacement-keys
  {:account_id :account-id :api_token :api-token :contact_email :contact-email})

(defn normalize-keys
  [account]
  (if (nil? account)
    nil
    (into {} (for [[k v] account]
               [(or (k replacement-keys) k) v]))))

(defn create-account
  [db name location contact-email]
  (-> (jdbc/insert! db :accounts
                    {:account_id (java.util.UUID/randomUUID)
                     :name name
                     :api_token (java.util.UUID/randomUUID)
                     :location location
                     :contact_email contact-email})
      first
      normalize-keys))

(defn list-accounts
  [db]
  (map normalize-keys (jdbc/query db ["select * from accounts where active = true"])))

(defn find-by-id
  [db account-id]
  (-> (jdbc/query db
                  ["select * from accounts where account_id = ? and active = true" account-id])
      first
      normalize-keys))

(defn find-by-token
  [db api-token]
  (-> (jdbc/query db
                  ["select * from accounts where api_token = ? and active = true" api-token])
      first
      normalize-keys))

(defn delete-account
  [db account-id]
  (first (jdbc/update! db :accounts {:active false}
                       ["account_id = ?" account-id])))
