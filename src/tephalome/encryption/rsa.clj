(ns tephalome.encryption.rsa
  (:gen-class)
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.string :as s])
  (:import (javax.crypto Cipher)
           (javax.crypto.spec SecretKeySpec)
           (java.security Security KeyFactory)
           (java.security.spec RSAPublicKeySpec)
           (java.math BigInteger)
           (org.bouncycastle.jce.provider BouncyCastleProvider)))

(set! *warn-on-reflection* true)

(Security/addProvider (BouncyCastleProvider.))

(defn _encrypt
  [^java.security.PublicKey public] 
  (let [^Cipher cipher (doto (Cipher/getInstance "RSA")
                         (.init Cipher/ENCRYPT_MODE public))]    
    (fn [^bytes message]
      (let [bs message]
        (.doFinal cipher message)))))

(defn _decrypt
  [^java.security.PrivateKey private]
  (let [^Cipher cipher (doto (Cipher/getInstance "RSA")
                         (.init Cipher/DECRYPT_MODE private))]
    (fn [^bytes message]
      (let [bs (b64/decode message)]
        (.doFinal cipher bs)))))

(defprotocol Encryptable
  (encrypt [this k]))

(extend-type SecretKeySpec
  Encryptable
  (encrypt [this k] 
    (let [base (_encrypt k)
          ^bytes bits  (b64/encode (base (.getEncoded this)))]
      (String. bits))))

(extend-type String
  Encryptable
  (encrypt [this k]
    (let [base (_encrypt k)
          ^bytes bits  (b64/encode (base (.getBytes this)))]
      (String. bits))))



(defn generate-keys []
  (let [generator (doto (java.security.KeyPairGenerator/getInstance "RSA")
                    (.initialize 1024))
        key-pair (.generateKeyPair generator)]
    {:public (.getPublic key-pair)
     :private (.getPrivate key-pair)}))

(defn decrypt-string
  [^java.security.PrivateKey private]
  (let [base (_decrypt private)]
    (fn [^String message]
      (let [bs (.getBytes  message)
            ^bytes bits (base bs)]
        (String. bits)))))

(defn decrypt-key
  [^java.security.PrivateKey private]
  (let [base (_decrypt private)]
    (fn [^String message]
      (let [bs (.getBytes  message)
            ^bytes bits (base bs)]
        (SecretKeySpec. bits 0 (count bits) "AES")))))

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
