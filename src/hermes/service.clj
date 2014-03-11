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
            [hermes.interceptors.global :as interceptors.global]
            [hermes.renderers.html :as html]))

(def global-interceptors
  [body-params interceptors.global/get-database])

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
