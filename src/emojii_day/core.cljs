(ns ^:figwheel-hooks emojii-day.core
  (:require [reagent.core :as r ]
            [emojii-day.emoji-data :as data]
            [emojii-day.validate :as validate]))

(enable-console-print!)

(defn e-target-value [f & args]
  (fn [e]
    (apply f (concat (list (.. e -target -value )) args) )))

;;  State
(defonce app-state (r/atom {:text "" :report {}}))
(def report-state (r/cursor app-state [:report]))

;; Actions
(defn validate-text [string]
  (swap! report-state #(validate/validate-report string)))

(defn update-text [text]
    (swap! app-state assoc :text text)
    (validate-text text))

;;  Components

(defn emoji-img [emoji]
  [:img {:src (emoji data/emoji) :class "emoji-img" :width 32 :height 32}])

(defn emoji-count [count]
  (if (< 0 count)
    [:b (str count "x")]
    [:noscript]))

(defn day-count [valid n emoji]
    [:span
     [:h4 (if valid "Valid! " "Invalid! ")
      [emoji-count n]
      " "
      (if (validate/weekday? emoji)
          [emoji-img emoji])]])

(defn ui-report [{:keys [valid match-day actual-day count is-empty ]}]
  [:div {:id "emoji-report"}
   [:p
    (cond
      is-empty [:span "Nothing to validate "]
      :else [day-count valid count match-day])
      [:p (if (and
               (validate/weekday? match-day)
               (not= match-day actual-day))
            [:span {:class "hint"}
          "(Even though today is " [:b {:class "no-hint"} (validate/to-weekday-str actual-day)] " )"])]]])

(defn ui-header []
  [:header {:id "emoji-header"}
   [:h2 [emoji-img :friday] "Today's emoji" [emoji-img :monday] [emoji-img :corgi]]])

(defn emjoii-box []
  [:div
   [ui-header]
   [:textarea
    {:value (:text @app-state)
     :rows 10
     :placeholder "Paste your day's emoji string from slack here..."
     :id "paste-box"
     :on-change (e-target-value update-text)}]
   [ui-report @report-state]])

(defn inside-state [state & name]
  [:div
   [:h2 name]
   [:code (str state)]])

(defn root []
  [:div
   [emjoii-box]])

(r/render-component [root]
                    (. js/document (getElementById "app")))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ;; (swap! app-state assoc :text "Hello World!")
)
