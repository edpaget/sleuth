(ns sleuth.handler
  (:use compojure.core
        ring.middleware.edn
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [sleuth.definitions :as d]
            [monger.core :as m]))

(m/connect!)
(m/set-db! (m/get-db "sleuth-dev"))

(defn wrap-user-info
  [handler]

  )

(defroutes app-routes
  (context "/definitions" [] d/definitions)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> app-routes
             wrap-edn-params
             wrap-json-params
             wrap-json-response))
