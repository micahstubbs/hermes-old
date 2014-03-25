(ns hermes.interceptors.feed
  (:require [io.pedestal.service.interceptor :as i]
             [hermes.entities.feed :as f]
             [ring.util.response :as ring-resp]
             [hermes.db :as db]
             [hermes.entities.feed :as feed]
             [clojure.java.io :as io])
  (:import (java.io FileInputStream File)))

(def find-schema
  {:feed-id java.util.UUID
   :user-id java.util.UUID})

(defn find-by-id
  [request-key-path]
  (i/interceptor
   :enter (fn [ctx]
            (let [user-id (get-in ctx [:request :user :user-id])
                  id (get-in ctx (concat [:request] request-key-path))
                  db (get-in ctx [:request :database])]
              (if-let [feed (feed/find-by-id db user-id id)]
                (assoc-in ctx [:request :feed] feed)
                (assoc ctx :response (ring-resp/not-found "Feed not found")))))))

(i/defbefore list-feeds
  [ctx]
  (assoc-in ctx [:request :feeds]
            (feed/list-feeds (get-in ctx [:request :database])
                             (get-in ctx [:request :user :user-id]))))

(i/defhandler create
  [request]
  (let [params (:params request)
        database (:database request)
        user-id (get-in request [:user :user-id])
        filename (get-in params ["datafile" :filename])
        input-file (get-in params ["datafile" :tempfile])]
    (if filename
      (let [feed (feed/create-feed database user-id filename)
            dir (str "/tmp/" (:feed-id feed))
            file (str dir "/" filename)]
        (if-not (or (nil? filename) (= 0 (count filename)))
          (do
            (.mkdir (File. dir))
            (io/copy input-file (File. file))
            (ring-resp/redirect-after-post (str "/users/" user-id "/feeds")))))
      (ring-resp/not-found "filename not provided"))))

(i/defhandler download
  [request]
  (let [feed (:feed request)
        filepath (str "/tmp/" (:feed-id feed) "/" (:filename feed))]
    (-> (ring-resp/file-response filepath)
        (ring-resp/header "Content-Disposition" (str "attachment; filename=" (:filename feed))))))
