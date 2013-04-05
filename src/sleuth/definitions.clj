(ns sleuth.definitions
  (:use compojure.core))

(defn respond-with
  [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str body)})

(defroutes definitions
   (GET "/" [] (respond-with []))
   (POST "/" [] (respond-with '() 201))
   (GET "/" [id] (respond-with '()))
   (PATCH "/:id" [id] (respond-with '())))