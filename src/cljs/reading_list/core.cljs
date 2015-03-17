(ns reading-list.core
    (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [cljs-http.client :as http]
              [cljs.core.async :refer [put! <! >! chan timeout merge mix admix toggle pub sub unsub unsub-all close!]]
              [reading-list.dispacher :refer [dispach dispacher]]
              [reading-list.routes-init :as routes]
              )
    (:import goog.History))

(enable-console-print!)

(println "Clojure script app has started first line")

;; -------------------------
;; Initialize app
(defn init! []
  (routes/hook-browser-navigation!)
  (reagent/render-component [routes/current-page] (.getElementById js/document "app")))
