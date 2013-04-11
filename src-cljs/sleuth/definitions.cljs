(ns sleuth.definitions
  (:require [dommy.core :as dommy]
            [goog.net.XhrIo :as xhr]
            [goog.events :as events]
            [goog.dom :as dom]
            clojure.edn))

(defn event-template
  [& {:keys [event-type selector]}]
  [:div.event
   [:input {:type "text" :name "event-type"} (str event-type)]
   [:input {:type "text" :name "selector"} (str selector)]])

(defn page-template
  [& {:keys [page-regex title events]}]
  [:div.page
   [:input {:type "text" :name "regex"} (str page-regex)]
   [:input {:type "text" :name "title"} (str title)]
   (map event-template events)
   [:button.add-event {:type "button" :name "add-event"} "Add Event"]])

(defn definition-template
  [& {:keys [site project pages]}]
  [:div.definition
   [:form.definition-form
    [:input {:type "text" :name "project"} (str project)]
    [:input {:type "text" :name "site"} (str site)]
    (map page-template pages) 
    [:button#add-page {:type "button" :name "add-page"} "Add Page"]
    [:button#submit {:type "button" :name "submit"} "Submit"]]])

(defn defintions-list-template
  [definitions]
  [:div.defintions
   [:ul
    (map #([:li {:data-id (str (:_id %))} (str (:project %))]) 
         definitions)]])

(defn definitions-page-template
  [definitions & [active]]
  [:div.definitions-page
   (defintions-list-template definitions)
   (if (nil? active) 
     (definition-template)
     (definition-template (first (filter #(= active (:_id %)) 
                                         definitions))))])

(defn xhr-to-edn 
  [f]
  (fn [e] (let [response (-> (.-target e)
                             .getResponseText
                             clojure.den/readString)]
            (f response))))

(defn switch-active
  [e]
  (let [id (-> e .-target .-dataset .-id)]
    (fetch id)))

(defn data-from-form
  (let []))

(defn add-page
  [e]
  (dommy/insert-before! [:button#add-page] (page-template)))

(defn add-event
  [e]
  (dommy/insert-before! [:button#add-event] (event-template)))

(defn submit
  [e]
  (xhr/send url nil (data-from-form)))

(defn render-page!
  [defintions]
  (dommy/append! [:body] (definitions-page-template defintions))
  (dommy/live-listener [:div.definitions] :li switch-active)
  (dommy/live-listener [:div.definition] :#add-page add-page)
  (dommy/live-listener [:div.definition] :.add-event add-event)
  (dommy/live-listener [:div.definition] :#submit submit))

(defn render-active!
  [definition]
  (dommy/replace! [:div.definition] (definition-template definition)))

(defn fetch
  []
  (let [url "/definitions"]
    (xhr/send url (xhr-to-edn render-page!) "GET"))
  [id]
  (let [url (str "/defintions/" id)]
    (xhr/send url (xhr-to-edn render-active!) "GET")))

