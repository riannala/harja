(ns harja.palvelin.integraatiot.sampo.kasittely.urakat-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [harja.testi :refer :all]
            [harja.palvelin.integraatiot.sampo.tyokalut :refer :all]
            [harja.palvelin.integraatiot.sampo.kasittely.urakat :as urakat]))

(deftest tarkista-urakan-tallentuminen
  (tuo-urakka)
  (is (= 1 (count (hae-urakat))) "Luonnin jälkeen urakka löytyy Sampo id:llä.")
  (poista-urakka))

(deftest tarkista-urakan-paivittaminen
  (tuo-urakka)
  (tuo-urakka)
  (is (= 1 (count (hae-urakat))) "Tuotaessa sama urakka uudestaan, päivitetään vanhaa eikä luoda uutta.")
  (poista-urakka))

(deftest tarkista-yhteyshenkilon-sitominen-urakkaan
  (tuo-urakka)
  (is (onko-yhteyshenkilo-sidottu-urakkaan?) "Urakalle löytyy luonnin jälkeen sampoid:llä sidottu yhteyshenkilö.")
  (poista-urakka))

(deftest tarkista-urakkatyypin-asettaminen
  (tuo-urakka)
  (is (= "hoito" (hae-urakan-tyyppi)) "Urakkatyyppi on asetettu oikein ennen kuin hanke on tuotu.")
  (poista-urakka)

  (tuo-urakka)
  (tuo-hanke)
  (is (= "hoito" (hae-urakan-tyyppi)) "Urakkatyyppi on asetettu oikein kun urakka on tuotu ensin.")
  (poista-urakka)
  (poista-hanke)

  (tuo-hanke)
  (tuo-urakka)
  (is (= "hoito" (hae-urakan-tyyppi)) "Urakkatyyppi on asetettu oikein kun hanke on tuotu ensin.")
  (poista-urakka)
  (poista-hanke))

(deftest tarkista-hallintayksikon-asettaminen
  (tuo-urakka)
  (is (.contains (hae-urakan-hallintayksikon-nimi) "Pohjois-Pohjanmaa") "Urakan hallintayksiköksi on asetettu Pohjois-Pohjanmaan ELY")
  (poista-urakka))

(deftest tarkista-alueurakkanumeron-purku
  (let [osat (urakat/pura-alueurakkanro "TESTI" "TYS-0666")]
    (is (= "TYS" (:tyypit osat)) "Tyypit on purettu oikein")
    (is (= "0666" (:alueurakkanro osat)) "Alueurakkanumero on purettu oikein"))

  (let [osat (urakat/pura-alueurakkanro "TESTI" "TYS0666")]
    (is (nil? (:tyypit osat)) "Tyyppiä ei ole päätelty")
    (is (nil? (:alueurakkanro osat)) "Alueurakkanumeroa ei ole otettu"))

  (let [osat (urakat/pura-alueurakkanro "TESTI" "TYS-!0666")]
    (is (= "TYS" (:tyypit osat)) "Tyyppi on päätelty oikein ")
    (is (nil? (:alueurakkanro osat)) "Alueurakkanumeroa ei ole otettu"))

  (let [osat (urakat/pura-alueurakkanro "TESTI" "THS-0666")]
    (is (= "THS" (:tyypit osat)) "Tyyppi on päätelty oikein ")
    (is (= "0666" (:alueurakkanro osat))   "Alueurakkanumero on purettu oikein"))

  (let [osat (urakat/pura-alueurakkanro "TESTI" "T--0FF666")]
    (is (nil? (:tyypit osat)) "Tyyppiä ei ole päätelty")
    (is (nil? (:alueurakkanro osat)) "Alueurakkanumeroa ei ole otettu")))