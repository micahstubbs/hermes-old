(ns hermes.interceptors.account-test
  (:require [hermes.interceptors.account :refer :all]
            [hermes.entities.account :as a]
            [hermes.db :as db]
            [hermes.test-helpers :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each with-fresh-db)

(deftest find-by-id-test
  (let [enter (:enter (find-by-id [:id]))
        account-id ((a/create-account (db/database)
                                       "Foo"
                                       "Somewhere"
                                       "foo@bar.com")
                    :account-id)]
    (testing "the account does not exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id (java.util.UUID/randomUUID)}})]
        (is (= 404 (-> out-ctx :response :status)))))
    (testing "the account does exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id account-id}})]
        (is (= account-id (get-in out-ctx [:request :account :account-id])))))))

(deftest list-accounts-test
  (let [enter (:enter list-accounts)]
    (testing "no accounts exist"
      (let [out-ctx (enter {:request {:database (db/database)}})]
        (is (empty? (get-in out-ctx [:request :accounts])))))
    (testing "with accounts"
      (a/create-account (db/database) "F" "F" "F")
      (a/create-account (db/database) "G" "G" "G")
      (let [out-ctx (enter {:request {:database (db/database)}})]
        (is (= 2 (count (get-in out-ctx [:request :accounts]))))
        (is (some #(= "G" (:name %)) (get-in out-ctx [:request :accounts])))))))

(deftest create-test
  (let [enter (:enter create)]
    (testing "redirect to account listing"
      (let [out-ctx (enter {:request
                            {:database (db/database)
                             :body-params {:name "John"
                                           :location "There"
                                           :contact-email "jdoe@ex.com"}}})]
        (is (= 303 (-> out-ctx :response :status)))
        (is (= "/accounts" (get-in out-ctx [:response :headers "Location"])))))))

(deftest delete-test
  (let [enter (:enter delete)
        account (a/create-account (db/database) "A" "B" "C")]
    (testing "deletes an account"
      (let [out-ctx (enter {:request
                            {:database (db/database)
                             :account account}})]
        (is (= 204 (-> out-ctx :response :status)))
        (is (empty? (a/find-by-id (db/database) (:account-id account))))))))
