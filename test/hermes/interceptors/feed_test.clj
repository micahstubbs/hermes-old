(ns hermes.interceptors.feed-test
  (:require [hermes.interceptors.feed :refer :all]
            [hermes.entities.feed :as f]
            [hermes.entities.account :as a]
            [hermes.db :as db]
            [hermes.test-helpers :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each with-fresh-db)

(deftest find-feeds-test
  (let [enter (:enter (find-by-id [:id]))
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")
        feed-id ((f/create-feed (db/database)
                                 (:account_id account)
                                 "test.txt")
                 :feed_id)]
    (testing "the feed does not exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :account account
                                      :id (java.util.UUID/randomUUID)}})]
        (is (= 404 (get-in out-ctx [:response :status])))))
    (testing "the feed does exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :account account
                                      :id feed-id}})]
        (is (= feed-id (get-in out-ctx [:request :feed :feed_id])))))))

(deftest list-feeds-test
  (let [enter (:enter list-feeds)
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")
        feed-id-1 ((f/create-feed (db/database)
                                  (:account_id account)
                                  "test1.txt")
                   :feed_id)
        feed-id-2 ((f/create-feed (db/database)
                                  (:account_id account)
                                  "test2.txt")
                   :feed_id)]
    (testing "finds all feeds"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :account account}})]
        (is (= 2 (count (get-in out-ctx [:request :feeds]))))))))

(deftest create-test
  (let [enter (:enter create)
        file-1 (temp-file "file-1")
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")
        out-ctx (enter {:request
                        {:database (db/database)
                         :account account
                         :params {"datafile" {:filename (.getName file-1)
                                              :tempfile file-1}}}})]
    (testing "creates the feed"
      (is (= 1 (count (f/list-feeds (db/database) (:account_id account))))))
    (testing "redirects to feeds listing"
      (is (= 303 (get-in out-ctx [:response :status])))
      (is (= (str "/accounts/" (:account_id account) "/feeds")
             (get-in out-ctx [:response :headers "Location"]))))))

(deftest download-test
  (let [enter (:enter download)
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")
        feed (f/create-feed (db/database)
                            (:account_id account)
                            "testfile.txt")
        out-ctx (enter {:request
                        {:database (db/database)
                         :feed feed}})]
    (testing "it returns the file for downloading"
      (is (= "attachment; filename=testfile.txt"
             (get-in out-ctx [:response :headers "Content-Disposition"]))))))
