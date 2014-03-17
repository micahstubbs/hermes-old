(ns hermes.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.ring-middlewares :as rm]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [expand-routes]]
            [ring.util.response :as ring-resp]
            [turbovote.resource-config :refer [config]]
            [turbovote.pedestal-toolbox.params :refer :all]
            [turbovote.pedestal-toolbox.content-negotiation :refer :all]
            [hermes.interceptors.feed :as interceptors.feed]
            [hermes.interceptors.account :as interceptors.account]
            [hermes.interceptors.global :as interceptors.global]
            [hermes.renderers.html :as html]))

(def global-interceptors
  [body-params (keywordize-params :body-params)
   interceptors.global/get-database])

(def html-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type
                       ["text/html"])]
      ["/accounts"
       {:get
        [:list-accounts
         ^:interceptors
         [interceptors.account/list-accounts]
         (html/enlive-template html/list-accounts)]}
       ["/:account-id"
        ^:interceptors
        [(validate-params :path-params interceptors.account/find-schema)
         (interceptors.account/find-by-id [:path-params :account-id])]
        ["/feeds"
         {:get
          [:list-feeds
           ^:interceptors
           [interceptors.feed/find-feeds]
           (html/enlive-template html/list-feeds)]}
         ["/new"
          {:get
           [:upload-feed-form
            (html/enlive-template html/upload-form)]}]]]
       ["/new"
        {:get
         [:new-account
          (html/enlive-template html/new-account)]}]]]]])

(def file-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type ["application/octet-stream"])]
      ["/accounts/:account-id/feeds/:feed-id/download"
       {:get
        [:download-feed
         ^:interceptors
         [(validate-params :path-params interceptors.feed/find-schema)
          (interceptors.account/find-by-id [:path-params :account-id])
          (interceptors.feed/find-by-id [:path-params :feed-id])]
         interceptors.feed/download]}]]]])

(def api-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type ["application/edn"
                                               "application/json"])]
      ["/accounts"
       {:post
        [:create-account
         ^:interceptors
         [(validate-body-params interceptors.account/create-schema)]
         interceptors.account/create]}
       ["/:account-id"
        ^:interceptors
        [(validate-params :path-params interceptors.account/find-schema)
         (interceptors.account/find-by-id [:path-params :account-id])]
        {:delete
         [:delete-account
          interceptors.account/delete]}
        ["/feeds"
         {:post
          [:upload-feed
           ^:interceptors [(rm/multipart-params)]
           interceptors.feed/create]}]]]]]])

(def routes
  (mapcat expand-routes [html-routes api-routes file-routes]))

(defn service []
  {:env :prod
   ::bootstrap/routes routes
   ::bootstrap/resource-path "/public"
   ::bootstrap/host (config :server :hostname)
   ::bootstrap/type :jetty
   ::bootstrap/port (config :server :port)})
