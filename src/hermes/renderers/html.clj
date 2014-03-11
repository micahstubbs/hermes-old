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

(e/defsnippet render-feed "hermes/renderers/html/feed-snippets.html" [:.feedlet]
  [feed]
  [:.feed-id] (e/content (:feed_id feed))
  [:.feed-file] (e/set-attr :href (str "/feeds/" (:feed_id feed) "/download"))
  [:.feed-file] (e/content (:filename feed)))

(e/deftemplate list-feeds "hermes/renderers/html/list-feeds.html"
  [ctx]
  [:body] (e/content (map #(render-feed %) (get-in ctx [:request :feeds]))))

(e/deftemplate upload-form "hermes/renderers/html/feed-upload.html" [ctx])

(e/deftemplate show-feed "hermes/renderers/html/show-feed.html"
  [ctx]
  [:body] (e/content (render-feed (get-in ctx [:request :feed]))))
