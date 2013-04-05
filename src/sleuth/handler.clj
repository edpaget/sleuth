(ns sleuth.handler
  (:use compojure.core
        ring.middleware.edn
        monger.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [sleuth.definitions :as d]))

(connect!)
(set-db! (use-db "sleuth-dev"))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (context "/defintions" [] d/definitions)
  (route/not-found "Not Found"))

(def app (-> app-routes
             wrap-edn-params))
