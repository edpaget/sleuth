(ns sleuth.auth
  (:use compojure.core
        sleuth.util)
  (:require [clojure.data.json :as json]
            [sleuth.users :as user]
            [clj-http.client :as client]
            [ring.util.codec :as codec]))

(defn verify
  [assertion]
  (client/post "https://login.persona.org/verify" 
               {:body (codec/url-encode (str "assertion=" assertion "&"
                                       "audience=" "http://localhost:3000"))}))

(defn create-or-update!
  [assertion]
  (let [verification (-> (verify assertion)
                         (json/read-str :key-fn keyword)
                         (:body))]
    (if (= "okay" (:status verification))
      (user/create-or-update! verification)
      (throw (Error.)))))

(defroutes auth-routes
  (POST "/login" {body :body} (respond-with-edn (create-or-update! body)))
  (POST "/logout" [] (respond-with-edn {:success true})))
