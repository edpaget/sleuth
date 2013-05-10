(ns sleuth.site-events
  (:use [jayq.core :only [$ inner value on off]])
  (:require [sleuth.xhr :as xhr]
            [dommy.template :as temp]))

(defn event-listing
  [event]
  (let [event-name (str (event "type") (if-let [sel (event "selector")]
                                        (str " on " sel ".")
                                       "."))] 
    [:li.event [:a {:href "#/site"}]]))

(defn events-list-template
  [events]
  [:div.events
   [:ul
    [:li.event [:span.editable]]
    (
     )]]) 


