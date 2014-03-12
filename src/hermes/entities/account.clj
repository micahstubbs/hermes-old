(ns hermes.entities.account
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-account
  [db name location contact-email]
  (first (jdbc/insert! db :accounts
                       {:account_id (java.util.UUID/randomUUID)
                        :name name
                        :api_token (java.util.UUID/randomUUID)
                        :location location
                        :contact_email contact-email})))

(defn list-accounts
  [db]
  (jdbc/query db ["select * from accounts where active = true"]))

(defn find-by-id
  [db account_id]
  (first (jdbc/query db
                     ["select * from accounts where account_id = ? and active = true" account_id])))

(defn find-by-token
  [db api_token]
  (first (jdbc/query db
                     ["select * from accounts where api_token = ? and active = true" api_token])))

(defn delete-account
  [db account_id]
  (first (jdbc/update! db :accounts {:active false}
                       ["account_id = ?" account_id])))
