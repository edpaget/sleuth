(ns sleuth.test.handler
  (:use clojure.test
        ring.mock.request  
        sleuth.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))
  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404))))
  
  (testing "GET Index"
    (let [response (app (request :get "/defintions"))]
      (is (= (:status response) 200))
      (is (= (:body response) "[]"))))

  (testing "POST Create"
    (let [response (app (request :post "/defintions"))]
      (is (= (:status response) 201))
      (is (= (:body response) "()")))))