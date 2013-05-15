(ns sleuth.analytics
  (:use [sleuth.sites-persistence :only [fetch!]]
        [jayq.core :only [$ on off val append]]
        [dommy.core :only [append! replace! replace-contents!]])
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
  (apply (partial append! (sel1 :#main.container)) 
         (map list-template (vals sites))))

(defn list-all
  [user sites]
  (add-watch sites :render (fn [key a old-val new-val]
                             (render-list new-val)))
  (fetch! user sites))

(deftemplate graph [title data]
  (let [data (into {} (map (fn [[k v]] [k (count v)]) data))]
    (.log js/console (pr-str data))
    [:div.chart
     [:h5 title]
     [:ul.analytics
      (map (fn [[key value]] [:li [:label (str key ": ") value]]) data)]]))

(deftemplate show-analytics [{type :type selector :selector [date value] :results}]
  [:div.charts
   [:h4 type (when selector [:small (str " on " selector)])]
   (graph "By Date" date)
   (graph "By Value" value)])

(deftemplate event-template [id {:keys [_id type selector]}]
  [:li [:a {:href (str "#/analytics/" id "/event/" _id)} 
        (str type (when (not (empty? selector)) 
                    (str " on " selector)))]])

(deftemplate show-template [site]
  [:div.site-analytics 
   [:div.row
    [:div.span8.offset4
     [:h2 (:name site)]]]
   [:div.row
    [:div.span4
     [:ul.events
      (map (partial event-template (:_id site)) (:site-events site))
      [:li [:a {:href (str "#/analytics/" (:_id site) "/event/new")} "Add New Event"]]]]
    [:div.span8
     [:div.chart-box]]]])

(deftemplate show-new []
  [:div.new
   [:h3 "Query for Event"]
   [:label "Type: " [:input.type {:type "text"}]]
   [:label "Selector: " [:input.selector {:type "text"}]]
   [:button.btn.submit "Submit"]])

(defn save!
  [user {id :_id}]
  (fn [e] (let [data {:type (val ($ :input.type))
                      :selector (val ($ :input.selector))}] 
            (xhr/post (str "/sites/" id "/site-events") data @user
                      #(set! (.-hash js/location) (str "#/analytics/" id "/event/" (:_id %)))))))

(defn render-analytics
  [user {id :_id} {ev-id :_id type :type selector :selector}]
  (xhr/get (str "/sites/" id "/site-events/" ev-id) @user 
           #(replace-contents! (sel1 :.chart-box) (show-analytics %))))

(defn render-new-analytics
  [user site]
  (off ($ :button.submit) "click")
  (replace! (sel1 :.chart-box) (show-new))
  (on ($ :button.submit) "click" (save! user site)))

(defn render-show
  [user site & [ev-id]]
  (replace-contents! (sel1 :#main.container) (show-template site))
  (if (or (= ev-id "new") (nil? ev-id)) 
    (render-new-analytics user site)
    (render-analytics user site (->> (:site-events site)
                                     (filter #(= (:_id %) ev-id))
                                     first))))

(defn show
  [user sites id & [ev-id]]
  (add-watch sites :render (fn [key a old-val new-val]
                             (render-show user (new-val id) ev-id)))
  (fetch! user sites))
