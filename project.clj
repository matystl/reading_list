(defproject reading_list "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs" "target/generated/clj" "target/generated/cljx"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cljsjs/react "0.12.2-5"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.4.4"]
                 [reagent-utils "0.1.3"]
                 [secretary "1.2.1"]
                 [org.clojure/clojurescript "0.0-3058" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [prone "0.8.0"]
                 [compojure "1.3.2"]
                 [selmer "0.8.0"]
                 [environ "1.0.0"]

                 [com.domkm/silk "0.0.4"] ; simple matching library for clojure and clojurescript

                 [com.cemerick/piggieback "0.1.5"]
                 [weasel "0.5.0"]
                 [ring/ring-json "0.2.0"]
                 [leiningen "2.5.1"]
                 [cljs-http "0.1.14"]
                 [figwheel "0.2.5-SNAPSHOT"]
                 [routes-clj "0.1.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler reading-list.handler/app
         :uberwar-name "reading_list.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "reading_list.jar"

  :main reading-list.server

  :clean-targets ^{:protect false} ["resources/public/js"]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "target/generated/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        ;;:externs       ["react/externs/react.js"]
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns reading-list.dev
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen "2.5.1"]
                                  [figwheel "0.2.5-SNAPSHOT"]
                                  [weasel "0.6.0-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.6.0"]]

                   :source-paths ["env/dev/clj"]

                   :plugins [[lein-figwheel "0.2.5-SNAPSHOT"]
                             [com.keminglabs/cljx "0.6.0" :exclusions [org.clojure/clojure]]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:nrepl-port 7889
                              :http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler reading-list.handler/app}

                   :env {:dev? true}

                   :prep-tasks [["cljx" "once"]]

                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/generated/clj"
                                    :rules :clj}
                                   {:source-paths ["src/cljx"]
                                    :output-path "target/generated/cljs"
                                    :rules :cljs}]}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "reading-list.dev"
                                                         :source-map true}}}}}

             :uberjar {:hooks [cljx.hooks leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                          :cljsbuild {:builds {:app {:compiler {:main "isomorfic.prod"}}}}
                          }})
