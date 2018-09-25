(defproject org.clojars.athos/duct.module.web.thin-api "0.6.4-SNAPSHOT"
  :description "Duct module for running thin web API"
  :url "https://github.com/athos/duct.module.web.thin-api"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [duct/core "0.6.1"]
                 [duct/logger "0.2.1"]
                 [duct/server.http.jetty "0.2.0"]
                 [integrant "0.6.2"]
                 [metosin/muuntaja "0.4.1"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [org.webjars/normalize.css "5.0.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-devel "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-webjars "0.2.0"]]
  :profiles
  {:dev {:dependencies [[compojure "1.6.0"]
                        [ring/ring-mock "0.3.2"]]}})
