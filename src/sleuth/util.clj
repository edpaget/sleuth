(ns sleuth.util)

(defn mongo-id-to-string
  [body]
  (let [id (:_id body)]
    (if-not (nil? id)
      (merge body {:_id (.toString id)})
      body))) 

(defn respond-with-edn
  [body & [status content-type]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str (mongo-id-to-string body))})

(defn forbidden
  []
  (respond-with-edn {:authorized false} 403))  
