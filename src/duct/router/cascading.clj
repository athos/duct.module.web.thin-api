(ns duct.router.cascading
  (:require [integrant.core :as ig]))

(defn- routes* [& handlers]
  (fn
    ([req]
     (reduce (fn [_ handler]
               (when-let [res (handler req)]
                 (reduced res)))
             nil
             handlers))
    ([req respond raise]
     (letfn [(f [handlers]
               (if (seq handlers)
                 (let [[handler & handlers] handlers]
                   (handler req #(if % (respond %) (f handlers)) raise))))]
       (f handlers)))))

(defmethod ig/init-key :duct.router/cascading [_ routes]
  (apply routes* routes))
