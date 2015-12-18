(ns tephalome.core
  (:require [org.httpkit.server :as s]
            [tephalome.encryption.rsa :as e]
            [tephalome.encryption.aes :as aes])
  
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST]]))

(defonce server (atom nil))

(def rooms (atom {}))

(defn touch
  [room-list
   name
   public-key]
  (swap! room-list update-in [name] conj public-key))

(defn serialize-room
  [room]
  (str (reduce (fn [ks k] (conj ks (e/serialize k))) [] room)))

(defn room-list
  [req]
  (let [public-key (get (:headers req) "x-public-key")
        key (e/gen-public public-key)
        aes-key (aes/generate-key)
        a-fn (aes/encrypt aes-key)
        body (a-fn (pr-str @rooms))]
    {:status  200
     :headers {"Content-Type" "application/edn"
               "x-key"(e/encrypt aes-key key)}
     :body    body}))

(defn room-touch
  [req]
  (let [public-key (get (:headers req) "x-public-key")
        key (e/gen-public public-key)
        aes-key (aes/generate-key)
        a-fn (aes/encrypt aes-key)
        name (keyword (-> req :params :name))
        body (-> (touch rooms name key)
                 name
                 serialize-room
                 a-fn)]
    {:status  200
     :headers {"Content-Type" "application/edn"
               "x-key"(e/encrypt aes-key key)}
     :body    body}))

(defroutes all-routes
  (GET "/rooms" [] room-list)
  (POST "/rooms/:name" [] room-touch))

(defn start [port] 
  (reset! server (s/run-server #'all-routes {:port port})))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [&args]
  (start 8080))
