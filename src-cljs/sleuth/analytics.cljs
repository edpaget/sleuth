(ns sleuth.analytics
  (:use [sleuth.sites-persistence :only [fetch!]]
        [dommy.core :only [append! replace-contents!]])
  (:use-macros [dommy.macros :only [sel1 deftemplate]])
  (:require [sleuth.xhr :as xhr]
            [sleuth.editable :as edit]))

(deftemplate list-template [site]
  [:div.row.analytics-list
   [:div.span12
    [:a {:href (str "#/analytics/" (:_id site))} 
     [:h2 (:name site) [:small (str " " (:url site))]]]]])

(defn render-list
  [sites]
  (doseq [site (vals sites)]
    (append! (sel1 :#main.container) (list-template site))))

(defn list-all
  [user sites]
  (add-watch sites :render (fn [key a old-val new-val]
                             (render-list new-val)))
  (when (empty? @sites)
    (fetch! user sites))
  (render-list @sites))

(deftemplate event-template [event]
  [:li (str (:type event) (when (:selector event) 
                            (str " on " (:selector-event))))])

(deftemplate show-template [site]
  [:div.site-analytics 
   [:div.row
    [:div.span8.offset4
     [:h2 (:name site)]]]
   [:div.row
    [:div.span4
     [:ul.events
      (doseq [event (:events site)] 
        (event-template (:events site)))]]
    [:div.span8
     [:div.create-query [:button.add-event "Add Event Query"]]
     [:div.analytics "analytics here"]]]])

(defn render-show
  [site ev-id]
  (replace-contents! (sel1 :#main.container) (show-template site)))

(defn show
  [user sites id & [ev-id]]
  (add-watch sites :render (fn [key a old-val new-val]
                             (render-show (new-val id) ev-id)))
  (when (empty? @sites)
    (fetch! user sites))
  (render-show (@sites id) ev-id))
