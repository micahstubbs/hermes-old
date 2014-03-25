(ns hermes.entities.user
  (:require [clojure.java.jdbc :as jdbc]
            [hermes.db :as db]))

(defn create-user
  [db account-id email]
  (-> (jdbc/insert! db :users
                    {:user_id (java.util.UUID/randomUUID)
                     :account_id account-id
                     :api_key (java.util.UUID/randomUUID)
                     :email email})
      first
      db/normalize-keys))

(defn find-by-id
  [db user-id]
  (-> (jdbc/query db
                  ["select * from users where user_id = ? and active = true" user-id])
      first
      db/normalize-keys))

(defn find-by-api-key
  [db api-key]
  (-> (jdbc/query db
                  ["select * from users where api_key = ? and active = true" api-key])
      first
      db/normalize-keys))

(defn list-users
  [db account-id]
  (map db/normalize-keys
       (jdbc/query db
                   ["select * from users where account_id = ? and active = true" account-id])))

(defn delete-user
  [db user-id]
  (first (jdbc/update! db :users {:active false}
                       ["user_id = ?" user-id])))
