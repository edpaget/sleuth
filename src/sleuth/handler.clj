(ns sleuth.handler
  (:use compojure.core
        ring.middleware.edn
        ring.middleware.json
        sleuth.util)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [sleuth.auth :as auth] 
            [sleuth.users :as user]
            [sleuth.sites :as sites]
            [sleuth.events :as events]
            [monger.core :as m]
            ))

(m/connect!)
(m/set-db! (m/get-db "sleuth-dev"))


(defn wrap-dir-index
  [handler]
  (fn [req]
    (handler (update-in req [:uri]
                        #(if (= "/" %) "/index.html" %)))))

(defroutes app-routes
  (context "/events" [] events/event-routes)
  (context "/sites" [] sites/site-routes)
  (context "/auth" [] auth/auth-routes)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> app-routes
             wrap-dir-index
             wrap-edn-params
             wrap-json-params
             wrap-json-response))
