(ns sleuth.util)

(defn respond-with-edn
  [body & [status content-type]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str body)})
