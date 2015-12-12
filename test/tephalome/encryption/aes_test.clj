(ns tephalome.encryption.aes-test
  (:require [tephalome.encryption.aes :refer :all]
            [clojure.test :refer :all]))

(deftest aes
  (let [k (generate-key)
        message "I could be really long, but I'm not."
        e (encrypt k)
        d (decrypt k)]
    (testing "end-to-end"
      (is (= message (d (e message)))))))
