(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]
            [hermes.db :as db]))

(defn create-feed
  [db account-id filename]
  (-> (jdbc/insert! db :feeds
                    {:account_id account-id
                     :feed_id (java.util.UUID/randomUUID)
                     :filename filename})
      first
      db/normalize-keys))

(defn list-feeds
  [db account-id]
  (map db/normalize-keys (jdbc/query db ["select * from feeds where account_id = ?" account-id])))

(defn find-by-id
  [db account-id feed-id]
  (-> (jdbc/query db
                  ["select * from feeds where account_id = ? and feed_id = ?" account-id feed-id])
      first
      db/normalize-keys))
