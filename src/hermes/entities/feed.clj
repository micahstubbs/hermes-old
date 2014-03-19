(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-feed
  [db account-id filename]
  (first (jdbc/insert! db :feeds
                       {:account-id account-id
                        :feed-id (java.util.UUID/randomUUID)
                        :filename filename})))

(defn list-feeds
  [db account-id]
  (jdbc/query db ["select * from feeds where account_id = ?" account-id]))

(defn find-by-id
  [db account-id feed-id]
  (first (jdbc/query db
                     ["select * from feeds where account_id = ? and feed_id = ?" account-id feed-id])))
