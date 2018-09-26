(ns duct.middleware.web.stacktrace
  (:require [integrant.core :as ig]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]))

(defmethod ig/init-key :duct.middleware.web/stacktrace [_ options]
  #(wrap-stacktrace % options))
