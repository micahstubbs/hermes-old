(ns hermes.interceptors.global
  (:require [io.pedestal.service.interceptor :as i]
            [hermes.db :as db]
            [clojure.walk :as walk]))

(i/defbefore get-database
  [ctx]
  (assoc-in ctx [:request :database] (db/database)))

(i/defbefore keywordize-body-params
  [ctx]
  (update-in ctx [:request :body-params]
             (fn [body-params] (into {} (for [[k v] body-params]
                                         [(keyword k) v])))))
