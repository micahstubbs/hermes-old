(ns hermes.entities.feed
  (:require [clojure.java.jdbc :as jdbc]))

(defn create-feed
  [db filename]
  (first (jdbc/insert! db :feeds
                       {:feed_id (java.util.UUID/randomUUID) :filename filename})))

(defn list-feeds
  [db]
  (jdbc/query db
              ["select * from feeds"]))

(defn find-by-id
  [db feed_id]
  (first (jdbc/query db
                     ["select * from feeds where feed_id = ?", feed_id])))
