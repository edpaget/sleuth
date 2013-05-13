(ns sleuth.sites
  (:use [jayq.core :only [$ inner value on off]]
        [sleuth.sites-persistence :only [fetch! save! delete!]])
  (:require [dommy.template :as dommy]
            [sleuth.xhr :as xhr]
            [sleuth.editable :as edit]))

(defn- site-template
  "Creates the dommy template for the site view"
  [site]
  [:div.span9.site
   (if-not (empty? site) 
     [:button.btn.btn-danger.pull-right.delete-site {:data-id (:_id site)} "Delete"])
   [:h2 
    [:span.editable {:data-field "name"} (str (or (:name site) "New Site") " ")]
    [:small "Double click field to edit"]]
   [:ul.site-info
    [:li [:label "Site: "] 
     [:span.editable {:data-field "url"} (or (:url site) "http://example.com")]]
    [:li [:label "Site Key: "] (or (:site-key site) "Create a site!")]]])

(defn- site-listing
  [site]
  [:li.site [:a {:href (str "#/sites/"(:_id site))} (:name site)]])

(defn- sites-template
  "Creates the dommy template for the entire sites page"
  [sites active]
  [:div.row
   [:div.span3.sites-list
    [:h4 "Your Sites"]
    [:ul.sites
     (map site-listing sites)
     [:li.new-site [:a {:href (str "#/sites/new")} "Add new site"]]]]
   (site-template active)])

(defn- render
  "Renders the entire site page"
  [sites active]
  (inner ($ :#main.container) 
         (dommy/node 
           (sites-template (vals sites) active))))

(defn update-active
  [sites active-site user]
  (fn [field value]
    (let [active-site (merge active-site {(keyword field) value})] 
      (save! sites active-site @user))))

(defn initialize 
  [user sites & [id]]
  (add-watch sites :render
             (fn [key a old-val new-val]
               (render new-val (new-val id))))
  (when (or (nil? id) (empty? @sites)) 
    (fetch! user sites))
  (render @sites (@sites id))
  (off ($ :#main.container) "click" :button.delete-site)
  (on ($ :#main.container) "click" :button.delete-site (delete! user sites))
  (edit/initialize (update-active sites (@sites id) user)))
