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
                    :account_id)]
    (testing "the account does not exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id (java.util.UUID/randomUUID)}})]
        (is (= 404 (-> out-ctx :response :status)))))
    (testing "the account does exist"
      (let [out-ctx (enter {:request {:database (db/database)
                                      :id account-id}})]
        (is (= account-id (get-in out-ctx [:request :account :account_id])))))))

(deftest validate-token-test
  (let [enter (:enter validate-token)
        token (java.util.UUID/randomUUID)
        account {:api_token token}]
    (testing "passthrough when the correct token is provided"
      (let [in-ctx {:request {:headers {validation-token-header-name (str token)}
                              :account account}}
            out-ctx (enter in-ctx)]
        (is (= out-ctx in-ctx)))
      (let [in-ctx {:request {:body-params {:api_token (str token)}
                              :account account}}
            out-ctx (enter in-ctx)]
        (is (= out-ctx in-ctx))))
    (testing "not authorized when token is not provided"
      (let [out-ctx (enter {:request {:account account}})]
        (is (= 401 (get-in out-ctx [:response :status])))))
    (testing "not authorized when the token is incorrect"
      (let [bad-token (str (java.util.UUID/randomUUID))
            out-ctx (enter {:request {:body-params
                                      {:api_token bad-token}
                                      :account account}})]
        (is (= 401 (get-in out-ctx [:response :status])))))
    (testing "not authorized when the token is not a UUID"
      (let [out-ctx (enter {:request {:body-params {:api_token "bad"}
                                      :account account}})]
        (is (= 401 (get-in out-ctx [:response :status])))))))

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
        (is (nil? (a/find-by-id (db/database) (:account_id account))))))))
