(ns hermes.renderers.html
  (:require [net.cgrand.enlive-html :as e]
            [io.pedestal.service.interceptor :as i]
            [clojure.string :as s]))

(defn enlive-template [template]
  (i/interceptor
   :leave
   (fn [ctx]
     (if (:response ctx)
       ctx ;; don't overwrite responses already on the ctx
       (let [html (template ctx)]
         (assoc ctx :response
                {:status 200
                 :headers {}
                 :body (s/join "" html)}))))))

(e/defsnippet render-feed "hermes/renderers/html/list-feeds.html" [:.feed]
  [feed]
  [:.feed-id] (e/content (str (:feed-id feed)))
  [:.feed-file] (e/set-attr :href (str "/feeds/" (:feed-id feed) "/download"))
  [:.feed-file] (e/content (:filename feed)))

(e/deftemplate list-feeds "hermes/renderers/html/list-feeds.html"
  [ctx]
  [:body] (e/content (map #(render-feed %) (get-in ctx [:request :feeds]))))

(e/defsnippet render-user "hermes/renderers/html/list-users.html" [:.user]
  [user]
  [:user-email (e/content (:email user))])

(e/deftemplate list-users "hermes/renderers/html/list-users.html" [ctx]
  [:body (e/content (map #(render-user %) (get-in ctx [:request :users])))])

(e/deftemplate upload-form "hermes/renderers/html/feed-upload.html" [ctx]
  [:form] (e/set-attr :action (str "/users/" (get-in ctx [:request :user :user-id]) "/feeds")))

(e/deftemplate show-feed "hermes/renderers/html/show-feed.html"
  [ctx]
  [:body] (e/content (render-feed (get-in ctx [:request :feed]))))

(e/deftemplate new-account "hermes/renderers/html/new-account.html" [ctx])

(e/defsnippet render-account "hermes/renderers/html/list-accounts.html" [:.account]
  [account]
  [:#name] (e/content (:name account))
  [:#account_id] (e/content (str (:account-id account)))
  [:#location] (e/content (:location account))
  [:#contact-email] (e/content (:contact-email account)))

(e/deftemplate list-accounts "hermes/renderers/html/list-accounts.html"
  [ctx]
  [:body] (e/content (map #(render-account %) (get-in ctx [:request :accounts]))))
