(ns tephalome.core-test
  (:require [clojure.test :refer :all]
            [tephalome.core :as t]
            [tephalome.encryption.rsa :as e]
            [tephalome.encryption.aes :as aes]
            [org.httpkit.client :as http]
            [clojure.edn :as edn]))

(def ks (e/generate-keys))
(def d (e/decrypt-key (:private ks)))
(def options {:headers {"x-public-key" (e/serialize (:public ks))}})

(defn start-server
  [f]
  (t/start 6666)
  (f)
  (t/stop))

(use-fixtures :once start-server)

(deftest test-app
  (let [{:keys [status body headers] :as resp} (t/app options)
        x-key (get headers "x-key")
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn body)] 
    (testing "correct status" 
      (is (= 200 status)))
    (testing "returns empty list" 
      (is (= [] (edn/read-string m))))))

(deftest room
  (let [{:keys [status body headers] :as resp} @(http/get "http://localhost:6666/" options)
        {x-key :x-key} headers
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn (slurp  body))] 
    (testing "correct status" 
      (is (= 200 status)))
    (testing "returns empty list" 
      (is (= [] (edn/read-string m)))))) 
