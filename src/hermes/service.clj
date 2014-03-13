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
  [body-params interceptors.global/keywordize-body-params
   interceptors.global/get-database])

(def html-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type
                       ["text/html"])]
      ["/feeds"
       {:get
        [:list-feeds
         ^:interceptors
         [interceptors.feed/find-feeds]
         (html/enlive-template html/list-feeds)]}
       ["/:id"
        {:get
         [:show-feed
          ^:interceptors
          [(interceptors.feed/find-by-id [:path-params :id])]
          (html/enlive-template html/show-feed)]}]]
      ["/accounts"
       {:get
        [:list-accounts
         ^:interceptors
         [interceptors.account/list-accounts]
         (html/enlive-template html/list-accounts)]}
       ["/new"
        {:get
         [:new-account
          (html/enlive-template html/new-account)]}]]
      ["/upload" {:get
                  [:upload-feed-form
                   (html/enlive-template html/upload-form)]}]]]])

(def file-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type ["application/octet-stream"])]
      ["/feeds/:id/download"
       {:get
        [:download-feed
         ^:interceptors
         [(interceptors.feed/find-by-id [:path-params :id])]
         interceptors.feed/download]}]]]])

(def api-routes
  `[[["/"
      ^:interceptors [~@global-interceptors
                      (negotiate-content-type ["application/edn"
                                               "application/json"])]
      ["/accounts"
       {:post
        [:create-account
         interceptors.account/create
         ^:interceptors
         [(validate-body-params interceptors.account/create-schema)]]}
       ["/:id"
        {:delete
         [:delete-account
          ^:interceptors
          [(validate-params :path-params interceptors.account/delete-schema)
           (interceptors.account/find-by-id [:path-params :id])]
          interceptors.account/delete]}]]
      ["/feeds/upload" {:post
                        [:upload-feed
                         ^:interceptors
                         [(rm/multipart-params)]
                         interceptors.feed/create]}]]]])

(def routes
  (mapcat expand-routes [html-routes api-routes file-routes]))

(defn service []
  {:env :prod
   ::bootstrap/routes routes
   ::bootstrap/resource-path "/public"
   ::bootstrap/host (config :server :hostname)
   ::bootstrap/type :jetty
   ::bootstrap/port (config :server :port)})
