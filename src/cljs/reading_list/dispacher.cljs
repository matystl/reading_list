(ns reading-list.dispacher
    (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljs-http.client :as http]
              [cljs.core.async :refer [put! <! >! chan timeout merge mix admix toggle pub sub unsub unsub-all close!]])
    )



; publisher is just a normal channel
(def dispacher-ch (chan))

(defn dispach [type data]
  (go (>! dispacher-ch {:type type :data data})))

; publication is a thing we subscribe to
(def dispacher
  (pub dispacher-ch :type))
