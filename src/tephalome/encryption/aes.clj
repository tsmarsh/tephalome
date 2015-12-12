(ns tephalome.encryption.aes
  (:gen-class)
  (:require [clojure.data.codec.base64 :as b64])
  (:import (javax.crypto Cipher)
           (java.security Security KeyFactory)))

(set! *warn-on-reflection* true)

(defn generate-key []
  (let [generator (doto (javax.crypto.KeyGenerator/getInstance "AES")
                    (.init 128))]
    (.generateKey generator)))

(defn encrypt [^javax.crypto.SecretKey key]
  (let [^Cipher cipher (doto (Cipher/getInstance "AES")
                 (.init Cipher/ENCRYPT_MODE key))]
    (fn [^String message]
      (let [^bytes bs (.getBytes message)
            ^bytes decoded-message (b64/encode (.doFinal cipher bs))]
        (String. decoded-message)))))

(defn decrypt [^javax.crypto.SecretKey key]
  (fn [^String message]
    (let [cipher (doto (Cipher/getInstance "AES")
                   (.init Cipher/DECRYPT_MODE key))
          bs (b64/decode (.getBytes message))]
      (String. (.doFinal cipher bs)))))
