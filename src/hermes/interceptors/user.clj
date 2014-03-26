(ns hermes.interceptors.user
  (:require [io.pedestal.service.interceptor :as i]
            [hermes.entities.user :as user]
            [hermes.db :as db]
            [ring.util.response :as ring-resp]
            [schema.core :as s]))

(def find-by-id-schema
  {:user-id java.util.UUID})

(defn find-by-id
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (let [id (get-in ctx (concat [:request] request-key-path))]
              (if-let [user (-> ctx
                                (get-in [:request :database])
                                (user/find-by-id id))]
                (assoc-in ctx [:request :user] user)
                (assoc ctx :response (ring-resp/not-found "User not found")))))))

(def find-by-api-key-schema
  {:api-key java.util.UUID})

(defn find-by-api-key
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (let [api-key (get-in ctx (concat [:request] request-key-path))]
              (if-let [user (-> ctx
                                (get-in [:request :database])
                                (user/find-by-api-key api-key))]
                (assoc-in ctx [:request :user] user)
                (assoc ctx :response (ring-resp/not-found "User not found")))))))

(def varchar100 (s/both String (s/pred #(< (count %) 100) 'varchar100?)))

(def create-schema
  {:account-id java.util.UUID
   :email varchar100})

(i/defhandler create
  [request]
  (let [email (get-in request [:body-params :email])
        account-id (get-in request [:account :account-id])
        database (:database request)
        user (user/create-user database account-id email)]
    (ring-resp/redirect-after-post (str "/accounts/" account-id "/users"))))

(i/defhandler delete
  [request]
  (let [user (:user request)
        database (:database request)]
    (user/delete-user database (:user-id user))
    (-> (ring-resp/response "Deleted")
        (ring-resp/status 204))))

(i/defbefore list-users
  [ctx]
  (let [database (get-in ctx [:request :database])
        account-id (get-in ctx [:request :account :account-id])]
    (assoc-in ctx [:request :users]
              (user/list-users database account-id))))
