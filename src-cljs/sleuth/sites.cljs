(ns sleuth.sites
  (:use [jayq.core :only [$ inner value on off]])
  (:require [dommy.template :as dommy]
            [sleuth.xhr :as xhr]
            [sleuth.editable :as edit]))

(defn- site-template
  "Creates the dommy template for the site view"
  [site]
  [:div.span8
   (if-not (empty? site) 
     [:button.btn.btn-danger.pull-right.delete-site {:data-id (:_id site)} "Delete"])
   [:h2 
    [:span.editable {:data-field "name"} (str (or (:name site) "New Site") " ")]
    [:small "Double click field to edit"]]
   [:ul.site-info
    [:li [:label "Site: "] 
     [:span.editable {:data-field "url"} (or (:url site) "http://example.com")]]
    [:li [:label "Event Count: "] (str (or (:event-count site) 0))]
    [:li [:label "Site Key: "] (or (:site-key site) "Create a site!")]]])

(defn- site-listing
  [site]
  [:li.site [:a {:href (str "#/sites/"(:_id site))} (:name site)]])

(defn- sites-template
  "Creates the dommy template for the entire sites page"
  [sites active]
  [:div.row
   [:div.span4
    [:h4 "Your Sites"]
    [:ul.sites
     (map site-listing sites)
     [:li.new-site [:a {:href (str "#/sites/new")} "Add new site"]]]]
   [:div.site (site-template active)]])

(defn- render
  "Renders the entire site page"
  [sites active]
  (inner ($ :#main.container) 
         (dommy/node 
           (sites-template sites active))))

(defn- render-site
  "Renders only the active site"
  [site]
  (inner ($ :div.site) 
         (dommy/node
           (site-template site)))
  (if-not (nil? (:_id site))
    (set! (.-hash js/location) (str "#/sites/" (:_id site)))))

(defn- fetch!
  ([user sites]
   (xhr/get "/sites/" @user #(swap! sites into %)))
  ([user id active-site]
   (xhr/get (str "/sites/" id) @user #(swap! active-site conj %))))

(defn- save!
  "Saves the active site to the api"
  [site user]
  (let [deref-site @site
        id (:_id deref-site)]
    (if (nil? id)
      (xhr/post "/sites" deref-site user 
                #(swap! site conj %))
      (xhr/put (str "/sites/" id) deref-site user 
               #(swap! site conj %)))))

(defn- delete!
  [user sites active-site]
  (fn [e]
    (let [id (-> e .-target .-dataset .-id)]
      (xhr/delete (str "/sites/" id) @user #(do (swap! active-site {})
                                                (fetch! user sites)
                                                (set! (.-hash js/location) "#/sites"))))))

(defn update-active
  [active-site user]
  (fn [field value]
    (swap! active-site merge {(keyword field) value})
    (save! active-site @user)))

(defn initialize 
  [user & [id]]
  (let [sites (atom [])
        active-site (atom {})]
    (add-watch sites :watch-change 
               (fn [key a old-val new-val]
                 (render new-val @active-site)))
    (add-watch active-site :watch-change 
               (fn [key a old-val new-val]
                 (render-site new-val)))
    (fetch! user sites)
    (if-not (or (nil? id) (= "new" id) (= "" id)) 
      (fetch! user id active-site))
    (off ($ :#main.container) "click" :button.delete-site)
    (on ($ :#main.container) "click" :button.delete-site (delete! user sites active-site))
    (edit/initialize (update-active active-site user))
    (render @sites @active-site)))
