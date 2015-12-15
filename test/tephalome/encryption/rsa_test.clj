(ns tephalome.encryption.rsa-test
  (:require [tephalome.encryption.rsa :refer :all]
            [tephalome.encryption.aes :as aes]
            [clojure.test :refer :all]))

(deftest rsa
  (let [{public :public
         private :private} (generate-keys)]
    (testing "string"
      (let [message "Short string"
            s-pub (serialize public)
            pub (gen-public s-pub)
            e (encrypt message pub)
            d (decrypt-string private)]
        (is (= message (d e)))))
    (testing "aes key"
      (let [message (aes/generate-key)
            e (encrypt message public)
            d (decrypt-key private)]
        (is (= message (d e)))))))
