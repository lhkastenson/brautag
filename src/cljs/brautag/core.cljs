(ns brautag.core
  (:require [ajax.core :refer [GET POST]]
            [brautag.ajax :refer [load-interceptors!]]
            [brautag.events]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [goog.string :as gstring]
            [goog.string.format]
            [markdown.core :refer [md->html]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary])
  (:import goog.History))

(defn nav-link [uri title page]
  [:li.nav-item
   {:class (when (= page @(rf/subscribe [:page])) "active")}
   [:a.nav-link {:href uri} title]])

(defn navbar []
  [:nav.navbar.navbar-dark.bg-primary.navbar-expand-md
   {:role "navigation"}
   [:button.navbar-toggler.hidden-sm-up
    {:type "button"
     :data-toggle "collapse"
     :data-target "#collapsing-navbar"}
    [:span.navbar-toggler-icon]]
   [:a.navbar-brand {:href "#/"} "brautag"]
   [:div#collapsing-navbar.collapse.navbar-collapse
    [:ul.nav.navbar-nav.mr-auto
     [nav-link "#/" "Home" :home]
     [nav-link "#/about" "About" :about]
     [nav-link "#/timer" "Timer" :timer]]]])

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn home-page []
  [:div.container
   [:div.row>div.col-sm-12
    [:h2.alert.alert-info "Tip: try pressing CTRL+H to open re-frame tracing menu"]]
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

(defn timer []
  (when-let [timer-start? @(rf/subscribe [:timer-start?])]
    (let [timer-val @(rf/subscribe [:timer-val])]
      (if timer-start?
        (js/setTimeout #(rf/dispatch [:update-timer timer-val]) 1000))))
  (let [timer-val @(rf/subscribe [:timer-val])
        mins (quot timer-val 60)
        secs (- timer-val (* mins 60))]
    [:div.row 
     [:h2 (str (gstring/format "%02d:%02d" mins secs))]]))

(defn timer-page []
  [:div.container
   [:div.row
    [:button
     {:on-click
      #(rf/dispatch [:start-timer])}
     "Start timer!"]
    [:button
     {:on-click
      #(rf/dispatch [:stop-timer])}
     "Stop timer!"]
    [:button
     {:on-click
      #(rf/dispatch [:reset-timer])}
     "Reset timer!"]]
   [timer]])

(defn state-blob
  "This is a state-blob for debugging"
  []
  [:div
   (pr-str @(rf/subscribe [:app-db]))])

(def pages
  {:home #'home-page
   :about #'about-page
   :timer #'timer-page})

(defn page []
  [:div
   [navbar]
   [state-blob]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/timer" []
  (rf/dispatch [:set-active-page :timer]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
