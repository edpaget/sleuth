(ns sleuth.util
  (:import [java.security MessageDigest]))

(defn- mongo-id-to-string
  [body]
  (let [id (:_id body)]
    (if-not (nil? id)
      (merge body {:_id (.toString id)})
      body))) 

(defn respond-with-edn
  "Converts the body into edn and allows the 
  status code of there response to be set"
  [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
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
