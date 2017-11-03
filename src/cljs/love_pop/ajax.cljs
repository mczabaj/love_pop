(ns love-pop.ajax
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(defn local-uri? [{:keys [uri]}]
  (not (re-find #"^\w+?://" uri)))

(defn default-headers [request]
  (if (local-uri? request)
    (-> request
        (update :uri #(str js/context %))
        (update :headers #(merge {"x-csrf-token" js/csrfToken} %)))
    request))

(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request default-headers})))


(defn curl
  "Make an HTTP request. Return a context map suitable for use with a
  side-effecting event handler."
  [db uri params success-event fail-event timeout method]
  {:db db
   :http-xhrio {:method          (or method :get)
                :uri             uri
                :params          params
                :timeout         (or timeout 5000)
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      (if (keyword? success-event)
                                   [success-event]
                                   success-event)
                :on-failure      (if (keyword? fail-event)
                                   [fail-event]
                                   fail-event)}})
