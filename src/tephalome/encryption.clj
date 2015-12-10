(ns tephalome.encryption
  (:gen-class)
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.string :as s])
  (:import (javax.crypto Cipher)
           (java.security Security KeyFactory)
           (java.security.spec RSAPublicKeySpec)
           (java.math BigInteger)
           (org.bouncycastle.jce.provider BouncyCastleProvider)))

(set *warn-on-reflection*)

(Security/addProvider (BouncyCastleProvider.))

(defn generate-keys []
  (let [generator (doto (java.security.KeyPairGenerator/getInstance "RSA")
                    (.initialize 1024))
        key-pair (.generateKeyPair generator)]
    {:public (.getPublic key-pair)
     :private (.getPrivate key-pair)}))

(defn encrypt
  [public]
  (fn [message]
    (let [cipher (doto (Cipher/getInstance "RSA")
                       (.init Cipher/ENCRYPT_MODE public))
          bs (.getBytes message)]
      (println "Count: " (count bs))
      (println "BS: " bs)
      (String. (b64/encode (.doFinal cipher bs))))))

(defn decrypt
  [private]
  (fn [message]
    (let [cipher (Cipher/getInstance "RSA")
          bs (b64/decode (.getBytes  message))]
      (.init cipher Cipher/DECRYPT_MODE private)
      (String. (.doFinal cipher bs)))))

(defn serialize
  [key]
  (let [mod (.getModulus key)
        exp (.getPublicExponent key)]
    (str  mod "|" exp)))

(defn gen-public
  [s]
  (let [[s-mod, s-exp] (s/split s #"\|")
        mod (BigInteger. s-mod)
        exp (BigInteger. s-exp)]
    (.generatePublic (KeyFactory/getInstance "RSA") (RSAPublicKeySpec. mod exp))))
