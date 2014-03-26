(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]
            [hermes.db :as db]))

(defn create-feed
  [db user-id filename]
  (-> (jdbc/insert! db :feeds
                    {:user_id user-id
                     :feed_id (java.util.UUID/randomUUID)
                     :filename filename})
      first
      db/normalize-keys))

(defn list-feeds
  [db user-id]
  (map db/normalize-keys (jdbc/query db ["select * from feeds where user_id = ?" user-id])))

(defn find-by-id
  [db user-id feed-id]
  (-> (jdbc/query db
                  ["select * from feeds where user_id = ? and feed_id = ?" user-id feed-id])
      first
      db/normalize-keys))
