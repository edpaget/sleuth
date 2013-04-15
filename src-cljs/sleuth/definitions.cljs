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
  [& {:keys [_id page-regex title events]}]
  [:div.page {:data-id (str _id)}
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
     (definition-template active))])

(def defintions (atom {}))
(def active (atom {}))
 
(defn xhr-to-edn 
  [f]
  (fn [e] (let [response (-> (.-target e)
                             .getResponseText
                             clojure.edn/readString)]
            (f response))))

(defn data-from-form
  []
  ("data!"))

(defn add-page
  [e]
  (dommy/insert-before! [:button#add-page] (page-template)))

(defn add-event
  [e]
  (dommy/insert-before! [:button#add-event] (event-template)))

(defn submit
  [e]
  (if (not (nil? active))
    (let [url (str "/definitions/" (:_id active))]
      (xhr/send url (xhr-to-edn set-active) "PUT" (data-from-form)))
    (let [url "/definitions"]
      (xhr/send url (xhr-to-edn set-active "POST" (data-from-form))))))

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
  ([]
  (let [url "/definitions"]
    (xhr/send url (xhr-to-edn set-definitions) "GET")))
  ([id]
  (let [url (str "/defintions/" id)]
    (xhr/send url (xhr-to-edn set-active) "GET"))))
 
(defn initialize
  []
  (add-watch definitions 
             (fn [key a old-val new-val] 
               (if (not (= old-val new-val))
                 (render-page! new-val))))
  (add-watch active 
             (fn [key a old-val new-val]
               (if (not (= old-val new-val))
                 (render-active! new-val))))
  (fetch))
 
(defn set-definitions
  [defs]
  (swap! definitions defs))

(defn set-active
  [act]
  (swap! active act))

(defn switch-active
  [e]
  (let [id (-> e .-target .-id)]
    (fetch id)))
