(ns dev.db
  (:require [hermes.db :as db]
            [clojure.java.jdbc :as jdbc]))

(defn clear-feeds!
  [database]
  (jdbc/execute! database ["DELETE FROM feeds"]))

(defn reset-db! []
  (let [database (db/database)]
    (clear-feeds! database)))
