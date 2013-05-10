(ns sleuth.auth
  (:use compojure.core
        sleuth.util)
  (:require [clojure.data.json :as json]
            [sleuth.users :as user]
            [clj-http.client :as client]))

(defn verify
  [assertion]
  (let [body  (json/write-str {:assertion assertion
                               :audience (if (get (System/getenv) "LEIN_NO_DEV")
                                           "https://sleuther.herokuapp.com"
                                           "http://localhost:3000")})]
    (client/post "https://login.persona.org/verify" 
                 {:body body
                  :headers {"Content-Type" "application/json"}})))

(defn create-or-update!
  [assertion]
  (let [verification (-> (verify assertion)
                         :body
                         (json/read-str :key-fn keyword))]
    (if (= "okay" (:status verification))
      (user/create-or-update! verification)
      (throw (Exception. "Verification Failed")))))

(defroutes auth-routes
  (POST "/login" [assertion]
        (try
          (respond-with-edn (create-or-update! assertion))
          (catch Exception e
            (respond-with-edn {:exception (.getMessage e)}
                              500))))
  (POST "/logout" [] 
        (respond-with-edn {:success true})))
