(ns tephalome.core
  (:require [org.httpkit.server :as s]))

(defonce server (atom nil))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "application/edn"}
   :body    "[]"})

(defn start [port] 
  (reset! server (s/run-server #'app {:port port})))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [&args]
  (start 8080))
