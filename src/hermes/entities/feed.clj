(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]))

(def replacement-keys
  {:account_id :account-id :feed_id :feed-id})

(defn normalize-keys
  [feed]
  (if (nil? feed)
    nil
    (into {} (for [[k v] feed]
               [(or (k replacement-keys) k) v]))))

(defn create-feed
  [db account-id filename]
  (-> (jdbc/insert! db :feeds
                    {:account_id account-id
                     :feed_id (java.util.UUID/randomUUID)
                     :filename filename})
      first
      normalize-keys))

(defn list-feeds
  [db account-id]
  (map normalize-keys (jdbc/query db ["select * from feeds where account_id = ?" account-id])))

(defn find-by-id
  [db account-id feed-id]
  (-> (jdbc/query db
                  ["select * from feeds where account_id = ? and feed_id = ?" account-id feed-id])
      first
      normalize-keys))
