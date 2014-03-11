(defproject hermes "0.1.0-SNAPSHOT"
  :description "Hermes helps shuttle data from state sites to Metis"
  :url "https://hermes.votinginfoproject.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [io.pedestal/pedestal.service "0.2.2"]
                 [io.pedestal/pedestal.service-tools "0.2.2"]
                 [io.pedestal/pedestal.jetty "0.2.2"]
                 [turbovote.resource-config "0.1.0"]
                 [turbovote.pedestal-toolbox "0.3.1"]
                 [prismatic/schema "0.2.0"]
                 [clj-http "0.7.8"]
                 [enlive "1.1.5"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [org.clojure/java.jdbc "0.3.3"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:build [:uberjar :env-credentials]
             :ci  [:build {:resource-paths ["ci-resources"]}]
             :uberjar {:aot [hermes.server]}
             :test {:resource-paths ["test-resources"]}
             :dev {:source-paths ["dev-resources/src"]
                   :resource-paths ["dev-resources"]}}
  :uberjar-name "hermes.jar"
  :aliases  {"run-dev" ["trampoline" "run" "-m" "hermes.server/run-dev"]
             "test-with-env-creds" ["with-profile" "env-credentials,test" "test"]
             "docs" ["pedestal-service-docs"]}
  :repl-options  {:init-ns user
                  :init (try
                          (use 'io.pedestal.service-tools.dev)
                          (require 'hermes.service)
                          ;; Nasty trick to get around being unable to reference non-clojure.core symbols in :init
                          (eval '(init (hermes.service/service) #'hermes.service/routes))
                          (catch Throwable t
                            (println "ERROR: There was a problem loading io.pedestal.service-tools.dev")
                            (clojure.stacktrace/print-stack-trace t)
                            (println)))
                  :welcome (println "Welcome to pedestal-service! Run (tools-help) to see a list of useful functions.")}
  :main ^{:skip-aot true} hermes.server)
