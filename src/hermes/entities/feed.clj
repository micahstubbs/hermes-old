(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-feed
  [db account_id filename]
  (first (jdbc/insert! db :feeds
                       {:account_id account_id
                        :feed_id (java.util.UUID/randomUUID)
                        :filename filename})))

(defn list-feeds
  [db account_id]
  (jdbc/query db ["select * from feeds where account_id = ?" account_id]))

(defn find-by-id
  [db account_id feed_id]
  (first (jdbc/query db
                     ["select * from feeds where account_id = ? and feed_id = ?" account_id feed_id])))
