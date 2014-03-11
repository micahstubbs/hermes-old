(ns hermes.interceptors.global
  (:require [io.pedestal.service.interceptor :as i]
            [hermes.db :as db]))

(i/defbefore get-database
  [ctx]
  (assoc-in ctx [:request :database] (db/database)))
