(ns hermes.db
  (:require [turbovote.resource-config :refer [config]]))

(defn database
  []
  (config :db))
