(ns tephalome.encryption-test
  (:require [tephalome.encryption :refer :all]
            [clojure.test :refer :all]))

(deftest rsa
  (let [{public :public
         private :private} (generate-keys)
        message "The quick brown fox jumps over the lazy log"
        algo "RSA"
        e (encrypt algo public)
        d (decrypt algo private)]
    (testing "end-to-end"
      (= message (d (e message))))))
