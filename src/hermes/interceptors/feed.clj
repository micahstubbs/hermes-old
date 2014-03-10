(ns hermes.interceptors.feed
  ( :require [io.pedestal.service.interceptor :as i]
             [hermes.entities.feed :as f]
             [ring.util.response :as ring-resp])
  (:import (java.io FileInputStream)))

(def feeds (atom []))

(i/defbefore find-feeds
  [ctx]
  (assoc-in ctx [:request :feeds] @feeds))

(defn find-by-id
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (println "****** REQUEST" (:request ctx))
            (let [id (get-in ctx (concat [:request] request-key-path))]
              (println "****** found" (some #(= id (:id %)) @feeds))
              (if-let [feed (-> (filter #(= id (:id %)) @feeds)
                                first)]
                (-> ctx
                    (assoc-in [:request :id] id)
                    (assoc-in [:request :feed] feed))
                (ring-resp/not-found "Feed not found"))))))

(i/defhandler create
  [request]
  (let [id (str (java.util.UUID/randomUUID))
        params (:params request)
        filename (get-in params ["datafile" :filename])
        file (get-in params ["datafile" :tempfile])]
    (clojure.pprint/pprint params)
    (swap! feeds conj {:id id :filename filename :file file})
    (ring-resp/redirect-after-post (str "/feeds/" id))))

(i/defhandler download
  [request]
  (ring-resp/response (FileInputStream. (get-in request [:feed :file]))))
