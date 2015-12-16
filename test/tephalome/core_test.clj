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

(defn rooms-and-corners
  [f]
  (reset! t/rooms {})
  (f))

(use-fixtures :once start-server)

(use-fixtures :each rooms-and-corners)

(deftest test-room-list
  (let [{:keys [status body headers] :as resp} (t/room-list options)
        x-key (get headers "x-key")
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn body)]
    (testing "correct status" 
      (is (= 200 status)))
    (testing "returns empty list" 
      (is (= {} (edn/read-string m))))))

(deftest test-room-touch
  (let [{:keys [status body headers] :as resp}
        (t/room-touch (assoc  options :params {:name "test-room"}))
        x-key (get headers "x-key")
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn body)] 
    (testing "correct status" 
      (is (= 200 status)))
    (let [members (edn/read-string m)]
      (testing "you are a member of the room"
        (is (= [(e/serialize (:public ks))] members))))))

(deftest room
  (let [{:keys [status body headers] :as resp} @(http/get "http://localhost:6666/rooms" options)
        {x-key :x-key} headers
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn (slurp  body))] 
    (testing "correct status" 
      (is (= 200 status)))
    (testing "returns empty list" 
      (is (= {} (edn/read-string m)))))) 

(deftest room-touch
  (let [{:keys [status body headers] :as resp}
        @(http/post "http://localhost:6666/rooms/test-room" options)
        {x-key :x-key} headers
        k (d x-key)
        aes-fn (aes/decrypt k)
        m (aes-fn (slurp  body))] 
    (testing "correct status" 
      (is (= 200 status)))
    (let [members (edn/read-string m)]
      (testing "you are a member of the room"
        (is (= [(e/serialize (:public ks))] members))))))
