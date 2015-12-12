(ns tephalome.encryption.rsa
  (:gen-class)
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.string :as s])
  (:import (javax.crypto Cipher)
           (java.security Security KeyFactory)
           (java.security.spec RSAPublicKeySpec)
           (java.math BigInteger)
           (org.bouncycastle.jce.provider BouncyCastleProvider)))

(set! *warn-on-reflection* true)

(Security/addProvider (BouncyCastleProvider.))

(defn generate-keys []
  (let [generator (doto (java.security.KeyPairGenerator/getInstance "RSA")
                    (.initialize 1024))
        key-pair (.generateKeyPair generator)]
    {:public (.getPublic key-pair)
     :private (.getPrivate key-pair)}))

(defn encrypt
  [^java.security.PublicKey public] 
  (let [^Cipher cipher (doto (Cipher/getInstance "RSA")
                         (.init Cipher/ENCRYPT_MODE public))]    
    (fn [^String message]
      (let [bs (.getBytes message)
            ^bytes decoded (b64/encode (.doFinal cipher bs))]
        (String. decoded)))))

(defn decrypt
  [^java.security.PrivateKey private]
  (let [^Cipher cipher (Cipher/getInstance "RSA")]
    (.init cipher Cipher/DECRYPT_MODE private)
    (fn [^String message]
      (let [bs (b64/decode (.getBytes  message))]
        (String. (.doFinal cipher bs))))))

(defn serialize
  [^java.security.interfaces.RSAPublicKey key]
  (let [^BigInteger mod (.getModulus key)
        ^BigInteger exp (.getPublicExponent key)]
    (str  mod "|" exp)))

(defn gen-public
  [^String s]
  (let [[^String s-mod ^String s-exp] (s/split s #"\|")
        mod (BigInteger. s-mod)
        exp (BigInteger. s-exp)]
    (.generatePublic (KeyFactory/getInstance "RSA") (RSAPublicKeySpec. mod exp))))
