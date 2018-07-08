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
    [:h2.alert.alert-info "Tip: try pressing CTRL+H to open re-frame tracing menu"]]])

(defn prettify-time [time-val]
  (let [mins (quot time-val 60)
        secs (- time-val (* mins 60))]
    (str (gstring/format "%02d:%02d" mins secs))))

(defn timer-clock []
  (when-let [timer-start? @(rf/subscribe [:timer-start?])]
    (let [timer-val @(rf/subscribe [:timer-val])]
      (if timer-start?
        (js/setTimeout #(rf/dispatch [:update-timer timer-val]) 1000))))
  (let [pretty-time-val (prettify-time @(rf/subscribe [:timer-val]))]
    [:div.row.d-flex.justify-content-center
     [:div.col.text-center
      [:h2 pretty-time-val]]]))

(defn timer []
  [:div.col-sm
   [:div.row.d-flex.justify-content-between.text-center
    [:div.col-sm
     [:button.btn.btn-primary
      {:on-click
       #(rf/dispatch [:start-timer])}
      "Start"]]
    [:div.col-sm
     [:button.btn.btn-secondary
      {:on-click
       #(rf/dispatch [:stop-timer])}
      "Stop"]]
    [:div.col-sm
     [:button.btn.btn-danger
      {:on-click
       #(rf/dispatch [:reset-timer])
       :disabled @(rf/subscribe [:timer-start?])}
      "Reset"]]]
   [timer-clock]])

(defn countdown-timer [event-time]
  (let [timer-val @(rf/subscribe [:timer-val])
        time-remaining (- timer-val event-time)]
       (if (> time-remaining 0)
         (str "(" (prettify-time time-remaining) ")")
         "")))

(defn event-timer-row [event-time]
  [:span.col.text-right (str (prettify-time event-time) (countdown-timer event-time))])

(defn events []
  [:div.col-md
   (doall
    (for [{:keys [id name time instructions]} @(rf/subscribe [:timer-events])]
      (list
   [:div.row {:key id}
    [:div.card.container-fluid
     [:div.card-body
      [:div.card-header.row
       [:span.col.text-left name]
       [event-timer-row time]]
      [:div.card-text  instructions]]]])))])

(defn timer-page []
  [:div.container
   [:div.row
    [events]
    [timer]]])

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
  #_(GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn fetch-timer-events! []
  (rf/dispatch [:set-timer-events [{:id 1 :name "Add extract 1" :time 3600 :instructions "Slowly pour the extract into boiling water, stirring occasionally."} 
                                   {:id 2 :name "Add cascade hops" :time 2700:instructions "Add the cascade hops slowly, being aware of overboils."} 
                                   {:id 3 :name "Add citra hops" :time 900 :instructions "Add the citra hops slowly, being aware of overboils."}]]))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  #_(fetch-docs!)
  (fetch-timer-events!)
  (hook-browser-navigation!)
  (mount-components))
