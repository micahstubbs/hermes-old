(ns hermes.interceptors.user-test
  (:require [hermes.interceptors.user :refer :all]
            [hermes.entities.account :as a]
            [hermes.entities.user :as u]
            [hermes.db :as db]
            [hermes.test-helpers :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each with-fresh-db)

(deftest find-by-id-test
  (let [enter (:enter (find-by-id [:id]))
        account-id (:account-id (a/create-account (db/database)
                                                  "Foo"
                                                  "Somewhere"
                                                  "foo@bar.com"))
        user-id (:user-id (u/create-user (db/database) account-id "user@test.com"))]
    (testing "the user does not exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id (java.util.UUID/randomUUID)}})]
        (is (= 404 (-> out-ctx :response :status)))))
    (testing "the userOB does exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id user-id}})]
        (is (= user-id (get-in out-ctx [:request :user :user-id])))))))

(deftest find-by-api-key-test
  (let [enter (:enter (find-by-api-key [:id]))
        account-id (:account-id (a/create-account (db/database)
                                                  "Foo"
                                                  "Somewhere"
                                                  "foo@bar.com"))
        api-key (:api-key (u/create-user (db/database) account-id "user@test.com"))]
    (testing "the user does not exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id (java.util.UUID/randomUUID)}})]
        (is (= 404 (-> out-ctx :response :status)))))
    (testing "the userOB does exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id api-key}})]
        (is (= api-key (get-in out-ctx [:request :user :api-key])))))))

(deftest create-test
  (let [enter (:enter create)
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")]
    (testing "it creates the user"
      (let [in-ctx {:request {:database (db/database)
                              :account account
                              :body-params {:email "user@email.com"}}}
            out-ctx (enter in-ctx)]
        (is (= 303 (-> out-ctx :response :status)))))))

(deftest list-users-test
  (let [enter (:enter list-users)
        account (a/create-account (db/database)
                                  "Foo"
                                  "Somewhere"
                                  "foo@bar.com")
        off-axis-account (a/create-account (db/database)
                                           "Other"
                                           "Over There"
                                           "other@foo.com")
        user1 (u/create-user (db/database) (:account-id account) "user1@email.com")
        user2 (u/create-user (db/database) (:account-id account) "user2@email.com")
        user3 (u/create-user (db/database) (:account-id off-axis-account) "a@b.com")]
    (testing "it finds both users"
      (let [in-ctx {:request {:database (db/database)
                              :account account}}
            out-ctx (enter in-ctx)]
        (is (= 2 (-> out-ctx :request :users count)))))))
