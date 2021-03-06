(ns sleuth.util
  (:require [clojure.string :as s]
            [clojure.data.codec.base64 :as b64]
            [clj-time.format :as f])
  (:import [java.security MessageDigest]))

(declare mongo-id-to-string)
(declare datetime-to-string)

(defn- convert-ids
  [[k v]]
  (cond (= :_id k) {k (.toString v)}
        (not (nil? (re-find #"-ids" (str k)))) {k (into [] (map #(.toString %) v))}
        (not (nil? (re-find #"-id" (str k)))) {k (.toString v)}
        (vector? v) {k (mongo-id-to-string v)}
        (seq? v) {k (mongo-id-to-string v)}
        true {k v}))

(defn- convert-dates
  [[k v]]
  (cond (= "created_at" k) {k (f/unparse (f/formatters :rfc822) v)}
        (vector? v) {k (datetime-to-string v)}
        (seq? v) {k (datetime-to-string v)}
        true {k v}))

(defn- convert-objects-to-string
  [conv body]
  (cond (map? body) (apply merge (map conv body))
        (seq? body) (map (partial convert-objects-to-string conv) body)
        (vector? body) (map (partial convert-objects-to-string conv) body)
        true body))

(def mongo-id-to-string (partial convert-objects-to-string convert-ids))
(def datetime-to-string (partial convert-objects-to-string convert-dates))

(defn respond-with-edn
  "Converts the body into edn and allows the 
  status code of there response to be set"
  [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn;charset=utf-8"}
   :body (pr-str (mongo-id-to-string body))})

(defn forbidden
  "Responds with 403"
  []
  (respond-with-edn {:authorized false} 403))  

(defn timestamp
  "Adds created-at and updated-at fields to a map"
  [m]
  (let [now (System/currentTimeMillis)]
    (merge {:created-at now} m {:updated-at now})))

(defn sha256
  "Generates SHA-256 hash of the given inputs"
  [& inputs]
  (let [md (MessageDigest/getInstance "SHA-256")
        input (apply str inputs)]
    (. md update (.getBytes input))
    (let [digest (.digest md)]
      (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn not-authorized
  [] 
  (respond-with-edn {:authorized false} 401))

(defn authed-request?
  [req]
  (not (nil? (get-in req [:headers "authorization"])))) 

(defn require-auth
  [handler predicate]
  (fn [req]
    (if (and (authed-request? req) (predicate req))
      (handler req)
      (not-authorized))))

(defn has-user?
  [req]
  (not (nil? (-> req :params :user))))

(defn has-site?
  [req]
  (not (nil? (-> req :params :site))))

(defn wrap-auth
  [handler auth-func param-name]
  (fn [req]
    (if (authed-request? req)
      (let [auth (last (re-find #"^Basic (.+)$" (get-in req [:headers "authorization"])))
            [id key] (s/split (->> (b64/decode (.getBytes auth))
                                          (map char)
                                          (apply str)) #":")]
        (if-let [user (auth-func id key)]
          (let [req* (assoc req :params (merge (:params req) 
                                               {param-name user}))]
            (handler req*))
          (not-authorized)))
      (handler req))))
