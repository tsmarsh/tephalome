(ns tephalome.core
  (:require [org.httpkit.server :as s]
            [tephalome.encryption.rsa :as e]))

(defonce server (atom nil))

(defn app [req]
  (let [
        public-key (get (:headers req) "x-public-key")
        key (e/gen-public public-key)
        e-fn (e/encrypt key)
        body (e-fn "[]")
        ]
    {:status  200
     :headers {"Content-Type" "application/edn"}
     :body    body}))

(defn start [port] 
  (reset! server (s/run-server #'app {:port port})))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [&args]
  (start 8080))
