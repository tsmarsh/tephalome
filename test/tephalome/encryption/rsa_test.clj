(ns tephalome.encryption.rsa-test
  (:require [tephalome.encryption.rsa :refer :all]
            [clojure.test :refer :all]))

(deftest rsa
  (let [{public :public
         private :private} (generate-keys)
        message "AES Key"
        s-pub (serialize public)
        pub (gen-public s-pub)
        e (encrypt pub)
        d (decrypt private)]
    (testing "end-to-end"
      (is (= message (d (e message)))))))
