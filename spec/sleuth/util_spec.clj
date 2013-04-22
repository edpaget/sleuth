(ns sleuth.util_spec
  (:use speclj.core
        sleuth.util
        sleuth.factories)
  (:require [sleuth.users :as user]
            [clojure.data.codec.base64 :as b64]))

(describe "respond-with-edn"
          (it "should respond with a map"
              (should== (keys (respond-with-edn ()))
                        [:status :headers :body]))
          (it "should respond with a 200 status by default" 
              (should== (:status (respond-with-edn ())) 200)))
(describe "forbidden"
          (it "should return a status of 403"
              (should= (:status (forbidden)) 403)))
(describe "not-authorized"
          (it "shoudl return a status of 401"
              (should= (:status (not-authorized)) 401)))
(describe "sha256"
          (it "should return a string sha256 hash"
              (should (string? (sha256 "tets" "ing")))))
(let [user (create-user!)] 
  (describe "timestamp"
            (it "should add created at and updated-at keys to map"
                (should (:updated-at (timestamp user)))
                (should (:created-at (timestamp user)))))

  (describe "wrap-auth"
            (it "should add user to the params"
                (should== ((wrap-auth #(:user (:params %)) user/auth :user) 
                           {:headers {"authorization" (str "Basic " (->> (.getBytes "example@example.com:not-an-api-key")
                                                                         b64/encode 
                                                                         (map char)
                                                                         (apply str)))}
                            :params {:logs "something"}})
                          user))))

(describe "authed-request?"
          (it "should return true when a request has an authorization header"
              (should (authed-request? {:headers {"authorization" "something"}})))
          (it "should return false when a request doesn't have an auth header"
              (should-not (authed-request? {:headers {"content-type" "application/json"}}))))

(describe "has-user?"
          (it "should return true when there is a user"
              (should (has-user? {:params {:user "blah"}}))))

(describe "has-site?"
          (it "should return true when there is a site"
              (should (has-site? {:params {:site "blench"}}))))

(describe "require-auth"
          (it "should be true when there is an auth header and the predicate passes"
              (should ((require-auth #(map? %) has-site?)
                       {:headers {"authorization" "stuff"}
                        :params {:site "other stuff"}}))))


(run-specs)
