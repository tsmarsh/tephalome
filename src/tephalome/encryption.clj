(ns tephalome.encryption
  (:gen-class)
  (:import (javax.crypto Cipher)))

(set *warn-on-reflection*)

(defn generate-keys []
  (let [generator (doto (java.security.KeyPairGenerator/getInstance "RSA" "SunRsaSign")
                    (.initialize 1024))
        key-pair (.generateKeyPair generator)]
    {:public (.getPublic key-pair)
     :private (.getPrivate key-pair)}))

(defn encrypt
  [algo
   public]
  (fn [message]
    (let [cipher (Cipher/getInstance algo)
          bs (.getBytes message)]
      (.init cipher Cipher/ENCRYPT_MODE public)
      (.doFinal cipher bs))))

(defn decrypt
  [algo
   private]
  (fn [message]
    (let [cipher (Cipher/getInstance algo)]
      (.init cipher Cipher/DECRYPT_MODE private)
      (String. (.doFinal cipher message)))))


