(ns reading-list.pages
  (:require [reading-list.view.home :as home]
            [reading-list.view.about :as about]))


(def pages {:home-page home/page
            :about-page about/page})
