(ns hermes.test-helpers
  (:require [clojure.test :refer :all]
            [hermes.db :as db]))

(defn with-fresh-db [f]
  (db/reset-db!)
  (db/run-migrations!)
  (f))

(defn temp-file [name]
  (let [file (java.io.File/createTempFile name nil)]
    (.deleteOnExit file)
    file))
