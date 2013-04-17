(ns sleuth.xhr
  (:require [goog.net.XhrIo :as xhr]
            cljs.reader))

(defn xhr-to-edn 
  [f]
  (if (nil? f)
    nil
    (fn [e] 
      (let [target (.-target e)]
        (if (nil? (some #{(.getStatus target)} [200 201 202]))
          (f)
          (let [response (-> target 
                             .getResponseText
                             cljs.reader/read-string)]
            (f response)))))))

(defn auth
  [{:keys [email api-key]}]
  (js/btoa (str "Basic " email ":" api-key)))

(defn post-or-put
  [url data method & [user callback]]
  (let [headers  ["Content-Type" "application/edn"
                  "Accept" "applicaiton/edn"]
        headers  (if-not (nil? user)
                   (into [] (concat headers 
                                    ["Authorization" (auth user)]))
                   headers)
        edn-data (pr-str data)]
    (xhr/send url (xhr-to-edn callback) 
              method edn-data (apply js-obj headers))))

(defn post
  [url data & [user callback]]
  (post-or-put url data "POST" user callback))

(defn put
  [url data & [user callback]]
  (post-or-put url data "PUT" user callback))

(defn delete
  [url & [user callback]]
  (post-or-put url nil "DELETE" user callback))

(defn get 
  [url user & [callback]]
  (let [headers (js-obj "Accept" "application/edn"
                        "Authorization" (auth user))]
    (xhr/send url (xhr-to-edn callback) "GET" nil headers)))
