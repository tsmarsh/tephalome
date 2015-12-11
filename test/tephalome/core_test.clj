(ns tephalome.core-test
  (:require [clojure.test :refer :all]
            [tephalome.core :as t]
            [tephalome.encryption :as e]
            [org.httpkit.client :as http]
            [clojure.edn :as edn]))

(def ks (e/generate-keys))
(def d (e/decrypt (:private ks)))
(def options {:headers {"x-public-key" (e/serialize (:public ks))}})
(defn start-server
  [f]
  (t/start 6666)
  (f)
  (t/stop))

(use-fixtures :once start-server)

(deftest test-app
  (let [{:keys [status body]} (t/app options)
        m (d body)]
    (testing "correct status" 
      (is (= 200 status)))
    (testing "returns empty list" 
      (is (= [] (edn/read-string m))))))

(deftest room
  (let [{:keys [status body] :as resp} @(http/get "http://localhost:6666/" options)]    
    (testing "can list the rooms"  
      (is (= 200 status))
      (is (= [] (edn/read-string  (d (slurp body)))))))) 
