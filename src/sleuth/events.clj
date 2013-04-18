(ns sleuth.events
  (:use compojure.core
        sleuth.util)
  (:require [monger.collection :as mc]
            [sleuth.sites :as sites]
            )
  (:import [org.bson.types ObjectId]))

(defn create!
  [{site :site logs "log"}]
  (println logs)
  (if-let [events (map #(merge {:_id (ObjectId.) :site-id (:_id site)} %) logs)]
    (do (println events)
        (mc/insert-batch (str "events-" (:url site)) events))))

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
            (do (println params)
                (future (create! params))
                {:status 201
                 :headers {"Content-Type" "text/plain;charset=utf-8"
                           "Access-Control-Allow-Origin" (get-in params [:site :url])
                           "Access-Control-Allow-Headers" "authorization,content-type"
                           "Access-Control-Allow-Methods" "POST"}
                 :body "5000"}))
      site-match
      (require-auth has-site?)
      wrap-site-info))
