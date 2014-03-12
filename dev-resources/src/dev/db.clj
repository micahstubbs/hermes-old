(ns dev.db
  (:require [clojure.java.jdbc :as jdbc]
            [ragtime.sql.file :as ragtime]))

(defn clear-feeds!
  [database]
  (jdbc/execute! database ["DELETE FROM feeds"]))

(defn reset-db! []
  (let [database (db/database)]
    (clear-feeds! database)))
