(ns hermes.interceptors.account
  (:require [io.pedestal.service.interceptor :as i]
            [hermes.entities.account :as account]
            [hermes.db :as db]
            [ring.util.response :as ring-resp]
            [schema.core :as s]))

(defn find-by-id
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (let [id (get-in ctx (concat [:request] request-key-path))]
              (if-let [account (-> ctx
                                   (get-in [:request :database])
                                   (account/find-by-id id))]
                (-> ctx
                    (assoc-in [:request :id] id)
                    (assoc-in [:request :account] account))
                (assoc ctx :response (ring-resp/not-found "Account not found")))))))

(defn find-by-token
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (let [token (get-in ctx (concat [:request] request-key-path))]
              (if-let [account (-> ctx
                                   (get-in [:request :database])
                                   (account/find-by-token token))]
                (-> ctx
                    (assoc-in [:request :token] token)
                    (assoc-in [:request :account] account))
                (assoc ctx :response (ring-resp/not-found "Account not found")))))))

(def varchar100 (s/both String (s/pred #(< (count %) 100) 'varchar100?)))

(def create-schema
  {:name varchar100
   :location varchar100
   :contact-email varchar100})

(i/defhandler create
  [request]
  (let [params (:body-params request)
        database (:database request)]
    (if-let [account (apply account/create-account database
                                             (map params [:name :location :contact-email]))]
      (ring-resp/redirect-after-post (str "/accounts"))
      (ring-resp/not-found "Could not create account"))))

(def delete-schema
  {:id java.util.UUID})

(i/defhandler delete
  [request]
  (let [account (:account request)
        database (:database request)]
    (account/delete-account database (:account_id account))
    (-> (ring-resp/response "Deleted")
        (ring-resp/status 204))))


(i/defbefore find-accounts
  [ctx]
  (assoc-in ctx [:request :accounts]
            (account/list-accounts (get-in ctx [:request :database]))))
