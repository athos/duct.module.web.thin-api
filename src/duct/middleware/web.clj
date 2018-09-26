(ns duct.middleware.web
  (:require [duct.logger :as logger]
            [integrant.core :as ig]
            [muuntaja.core :as mc]
            [muuntaja.middleware :as mm]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.util.response :as response]
            [duct.core.merge :as merge]))

(def ^:private request-log-keys
  [:request-method :uri :query-string])

(defn- log-request [logger request]
  (logger/log logger :info ::request (select-keys request request-log-keys)))

(defn- log-error [logger ex]
  (logger/log logger :error ::handler-error ex))

(defn wrap-log-requests
  "Log each request using the supplied logger. The logger must implement the
  duct.core.protocols/Logger protocol."
  [handler logger]
  (fn
    ([request]
     (log-request logger request)
     (handler request))
    ([request respond raise]
     (log-request logger request)
     (handler request respond raise))))

(defn wrap-log-errors
  "Log any exceptions with the supplied logger, then re-throw them."
  [handler logger]
  (fn
    ([request]
     (try
       (handler request)
       (catch Throwable ex
         (log-error logger ex)
         (throw ex))))
    ([request respond raise]
     (try
       (handler request respond (fn [ex] (log-error logger ex) (raise ex)))
       (catch Throwable ex
         (log-error logger ex)
         (throw ex))))))

(defn- internal-error [response]
  (response/status response 500))

(defn- not-found [response]
  (response/status response 404))

(defn wrap-hide-errors
  "Middleware that hides any uncaught exceptions behind a 500 'Internal Error'
  response generated by an error handler. Intended for use in production when
  exception details need to be hidden."
  [handler error-handler]
  (fn
    ([request]
     (try
       (handler request)
       (catch Throwable _ (internal-error (error-handler request)))))
    ([request respond raise]
     (try
       (handler request respond (fn [_] (respond (internal-error (error-handler request)))))
       (catch Throwable _ (respond (internal-error (error-handler request))))))))

(defn wrap-not-found
  "Middleware that returns a 404 'Not Found' response from an error handler if
  the base handler returns nil."
  [handler error-handler]
  (fn
    ([request]
     (or (handler request) (not-found (error-handler request))))
    ([request respond raise]
     (handler request #(respond (or % (not-found (error-handler request)))) raise))))

(defn- route-aliases-request [request aliases]
  (if-let [alias (aliases (:uri request))]
    (assoc request :uri alias)
    request))

(defn wrap-route-aliases
  "Middleware that takes a map of URI aliases. If the URI of the request matches
  a URI in the map's keys, the URI is changed to the value corresponding to that
  key."
  [handler aliases]
  (fn
    ([request]
     (handler (route-aliases-request request aliases)))
    ([request respond raise]
     (handler (route-aliases-request request aliases) respond raise))))

(defmethod ig/init-key ::log-requests [_ {:keys [logger]}]
  #(wrap-log-requests % logger))

(defmethod ig/init-key ::log-errors [_ {:keys [logger]}]
  #(wrap-log-errors % logger))

(defmethod ig/init-key ::hide-errors [_ {:keys [error-handler]}]
  #(wrap-hide-errors % error-handler))

(defmethod ig/init-key ::not-found [_ {:keys [error-handler]}]
  #(wrap-not-found % error-handler))

(defmethod ig/init-key ::route-aliases [_ aliases]
  #(wrap-route-aliases % aliases))

(defmethod ig/init-key ::defaults [_ defaults]
  #(wrap-defaults % defaults))

(defmethod ig/init-key ::webjars [_ _]
  (throw (ex-info (str "duct.module.web.thin-api does not support " ::webjars) {})))

(defn- deep-merge [a b]
  (if (and (map? a) (map b))
    (merge-with deep-merge a b)
    b))

(defmethod ig/init-key ::format [_ options]
  #(mm/wrap-format % (deep-merge mc/default-options options)))
