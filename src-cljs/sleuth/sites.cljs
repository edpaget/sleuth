(ns sleuth.sites
  (:use [jayq.core :only [$ inner value]])
  (:require [dommy.template :as dommy]
            [sleuth.xhr :as xhr]))

(defn sites-template
  [sites active]
  [:div.row
   [:div.span4
    [:h4 "Your Sites"]
    [:ul.sites
     (doseq [site sites]
       [:li.site [:a {:href (str "#/sites/"(:_id site))} (:name site)]])
     [:li.new-site [:a {:href (str "#/sites/new")}]]]]
   [:div.site (site-template active)]])

(defn site-template
  [site]
  [:div.span8
   [:h1 (or (:name site) "New Site")
    [:small "Double click field to edit"]]
   [:ul.site-info
    [:li [:label "Site: "] (or (:url site) "http://example.com")]
    [:li [:label "Event Count: "] (str (or (:event-count site) 0))]
    [:li [:label "Site Key: "] (or (:site-key site) "Create a site!")]]])

(defn render
  [sites active]
  (inner ($ :#main.container) 
         (dommy/node 
           (sites-template sites active))))

(defn render-site
  [site]
  (inner ($ :.site) 
         (dommy/node
           (site-template site))))

(defn fetch
  ([user sites]
   (xhr/get "/sites" user nil #(swap! sites conj %)))
  ([user id active-site]
   (xhr/get (str "/sites/" id) nil #(swap! sites conj %))))

(defn initialize 
  [user & [id]]
  (let [sites (atom [])
        active-site (atom {})]
    (add-watch sites :watch-change (fn [key a old-val new-val]
                                     (render new-val @active-site)))
    (add-watch sites :watch-change (fn [key a old-val new-val]
                                     (render-site new-val)))
    (fetch user sites)
    (render @sites @active-site)))
