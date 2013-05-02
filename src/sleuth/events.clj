(ns sleuth.events
  (:use compojure.core
        sleuth.util)
  (:require [sleuth.sites :as sites]
            [cemerick.bandalore :as sqs])
  (:import [org.bson.types ObjectId]))

(def client (sqs/create-client (get (System/getenv) "AMAZON_SECRET_ID")
                               (get (System/getenv) "AMAZON_SECRET_ACCESS_KEY")))

(def queue (sqs/create-queue client "sleuth-dev"))

(defn enqueue! 
  [{site :site logs "log"}]
  (if-let [events (map #(merge {"site-url" (:url site)} %) logs)]
    (doseq [event events]
      (sqs/send client queue (pr-str events)))))

(defn site-match
  [handler]
  (fn [req]
    (if (= (get-in req [:params :site :url])
           (get-in req [:headers "origin"]))
      (handler req)
      (forbidden))))

(defn wrap-site-info
  [handler]
  (wrap-auth handler sites/auth :site))  

(defroutes event-routes
  (OPTIONS "/" {headers :headers} 
           {:status 200
            :headers {"Access-Control-Allow-Origin" "*"
                      "Access-Control-Allow-Headers" "authorization,content-type"
                      "Access-Control-Allow-Methods" "POST"}})

  (-> (POST "/" {params :params} 
            (do (future (enqueue! params))
                {:status 201
                 :headers {"Content-Type" "text/plain;charset=utf-8"
                           "Access-Control-Allow-Origin" (get-in params [:site :url])
                           "Access-Control-Allow-Headers" "authorization,content-type"
                           "Access-Control-Allow-Methods" "POST"}
                 :body "5000"}))
      site-match
      (require-auth has-site?)
      wrap-site-info))
