(ns sleuth.test.definitions
  (:use clojure.test
        ring.mock.request
        sleuth.definitions)
  (:require [monger.core :as mc]))

(mc/connect!)
(mc/set-db! (mc/get-db "monger-test"))

(deftest test-defintions
  (testing "respond-with"
    (is (= (respond-with '()) {:status 200 
                                :headers {"Content-Type" "application/edn"}
                                :body "()"}))
    (is (= (respond-with '() 201) {:status 201
                                   :headers {"Content-Type" "application/edn"}
                                   :body "()"})))
  (testing "create-or-update!"
    (is (= {:site "test" :project "test" :page-ids [] :user-id "test"} 
           (dissoc (create-or-update! {:user-id "test" :site "test" :project "test"}) 
                   :_id :created-at :updated-at)))
    (is (= {} (create-or-update! {:project "test" :pages []})))))
