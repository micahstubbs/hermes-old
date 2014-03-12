(ns hermes.entities.account
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-account
  [db name]
  (first (jdbc/insert! db :accounts
                       {:account_id (java.util.UUID/randomUUID)
                        :name name
                        :api_token (java.util.UUID/randomUUID)})))

(defn list-accounts
  [db]
  (jdbc/query db ["select * from accounts"]))

(defn find-by-id
  [db account_id]
  (first (jdbc/query db
                     ["select * from accounts where account_id = ?", account_id])))

(defn find-by-token
  [db api_token]
  (first (jdbc/query db
                     ["select * from accounts where api_token = ?", api_token])))
