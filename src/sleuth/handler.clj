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
            [monger.core :as m]
            [clojure.string :as s]
            [clojure.data.codec.base64 :as b64]))

(m/connect!)
(m/set-db! (m/get-db "sleuth-dev"))

(defn not-authorized
  [] 
  (respond-with-edn {:authorized false} 401))

(defn authed-request?
  [req]
  (not (nil? (:authorization req))))

(defn has-user?
  [req]
  (not (nil? (-> req :params :user))))

(defn wrap-user-info
  [handler]
  (fn [req]
    (if (authed-request? req)
      (let [auth (:authorization req)
            [email api-key] (->> (b64/decode auth)
                                 (map char)
                                 (apply str)
                                 (s/split ":"))]
        (if-let [user (user/auth email api-key)]
          (let [req* (assoc req :params (merge (:params req) 
                                               {:user user}))]
            (handler req*))
          (not-authorized)))
      (handler req))))

(defn require-auth
  [handler]
  (fn [req]
    (if (and (authed-request? req) (has-user? req))
      (handler req)
      (not-authorized))))

(defn wrap-dir-index
  [handler]
  (fn [req]
    (handler (update-in req [:uri]
                        #(if (= "/" %) "/index.html" %)))))

(defroutes app-routes
  (context "/sites" [] (require-auth sites/site-routes))
  (context "/auth" [] auth/auth-routes)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> app-routes
             wrap-dir-index
             wrap-edn-params
             wrap-json-params
             wrap-json-response
             wrap-user-info))
