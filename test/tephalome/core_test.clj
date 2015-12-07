(ns tephalome.core-test
  (:require [clojure.test :refer :all]
            [tephalome.core :as t]
            [org.httpkit.client :as http]
            [clojure.edn :as edn]))

(defn start-server
  [f]
  (t/start 6666)
  (f)
  (t/stop))

(use-fixtures :once start-server)

(deftest room
  (let [{:keys [status body]} @(http/get "http://localhost:6666/")]    
    (testing "can list the rooms"
      (is (= 200 status))
      (is (= [] (edn/read-string (slurp body))))))) 
