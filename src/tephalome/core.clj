(ns tephalome.core
  (:require [org.httpkit.server :as s]
            [tephalome.encryption.rsa :as e]
            [tephalome.encryption.aes :as aes]))

(defonce server (atom nil))

(defn app [req]
  (let [
        public-key (get (:headers req) "x-public-key")
        key (e/gen-public public-key)
        aes-key (aes/generate-key)
        a-fn (aes/encrypt aes-key)
        body (a-fn "[]")]
    {:status  200
     :headers {"Content-Type" "application/edn"
               "x-key"(e/encrypt aes-key key)}
     :body    body}))

(defn start [port] 
  (reset! server (s/run-server #'app {:port port})))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [&args]
  (start 8080))
