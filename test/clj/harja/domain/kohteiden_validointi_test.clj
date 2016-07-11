(ns harja.domain.kohteiden-validointi-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [harja.testi :refer :all]
            [slingshot.slingshot :refer [throw+]]
            [slingshot.test]
            [harja.domain.yllapitokohteet :as yllapitokohteet]))

(defn tasmaa-poikkeus [{:keys [type virheet]} tyyppi koodi viesti]
  (and
    (= tyyppi type)
    (some (fn [virhe] (and (= koodi (:koodi virhe)) (.contains (:viesti virhe) viesti)))
          virheet)))

(deftest tarkista-kohteen-validius
  (is (thrown+?
        #(tasmaa-poikkeus
          %
          yllapitokohteet/+kohteissa-viallisia-sijainteja+
          yllapitokohteet/+viallinen-yllapitokohteen-sijainti+
          "Kohteen (id: 1) alkuosa on loppuosaa isompi")
        (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 {:aosa 2 :losa 1} nil))
      "Loppuosaa suurempi alkuosa otettiin kiini"))

(deftest tarkista-alikohteen-validius
  (let [kohde {:aosa 1 :aet 1 :losa 4 :let 4}
        alikohteet [{:tunnus "A"
                     :sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                    {:tunnus "B"
                     :sijainti {:aosa 2, :aet 2, :losa 3, :let 3}}
                    {:tunnus "C"
                     :sijainti {:aosa 3, :aet 3, :losa 5, :let 5}}]]
    (is (thrown+?
          #(tasmaa-poikkeus
            %
            yllapitokohteet/+kohteissa-viallisia-sijainteja+
            yllapitokohteet/+viallinen-yllapitokohdeosan-sijainti+
            "Alikohde (tunnus: C) ei ole kohteen (id: 1) sisällä.")
          (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde alikohteet))
        "Kohteen ulkopuolinen alikohde otettiin kiinni"))


  (let [kohde {:aosa 1 :aet 1 :losa 5 :let 5}
        alikohteet [{:tunnus "A"
                     :sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                    {:tunnus "B"
                     :sijainti {:aosa 2, :aet 2, :losa 3, :let 3}}
                    {:tunnus "C"
                     :sijainti {:aosa 3, :aet 3, :losa 4, :let 1}}
                    {:tunnus "D"
                     :sijainti {:aosa 4, :aet 4, :losa 5, :let 5}}]]
    (is (thrown+?
          #(tasmaa-poikkeus
            %
            yllapitokohteet/+kohteissa-viallisia-sijainteja+
            yllapitokohteet/+viallinen-yllapitokohdeosan-sijainti+
            "Alikohteet (tunnus: C ja tunnus: D) eivät muodosta yhteistä osuutta")
          (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde alikohteet))
        "Alikohteet jotka eivät muodosta yhtenäistä osaa otettiin kiinni"))


  (let [kohde {:aosa 1 :aet 1 :losa 1 :let 1}
        alikohteet [{:tunnus "A"
                     :sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                    {:tunnus "B"
                     :sijainti {:aosa 2, :aet 2, :losa 3, :let 3}}]]
    (is (thrown+?
          #(tasmaa-poikkeus
            %
            yllapitokohteet/+kohteissa-viallisia-sijainteja+
            yllapitokohteet/+viallinen-yllapitokohdeosan-sijainti+
            "Alikohteet eivät täytä kohdetta (id: 1)")
          (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde alikohteet))
        "Alikohteet jotka eivät täytä kohdetta otettiin kiinni")))

(deftest tarkista-validi-kohde
  (let [kohde {:aosa 1 :aet 1 :losa 4 :let 4}
        yksi-alikohde [{:tunnus "A"
                        :sijainti {:aosa 1, :aet 1, :losa 4, :let 4}}]
        kaksi-alikohdetta [{:tunnus "A"
                            :sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                           {:tunnus "B"
                            :sijainti {:aosa 2, :aet 2, :losa 4, :let 4}}]
        monta-alikohdetta [{:tunnus "A"
                            :sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                           {:tunnus "B"
                            :sijainti {:aosa 2, :aet 2, :losa 3, :let 3}}
                           {:tunnus "C"
                            :sijainti {:aosa 3, :aet 3, :losa 4, :let 4}}]]
    (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde yksi-alikohde)
    (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde kaksi-alikohdetta)
    (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 kohde monta-alikohdetta)
    (yllapitokohteet/tarkista-kohteen-ja-alikohteiden-sijannit 1 {:aosa 1
                                                                  :aet 1
                                                                  :losa 5
                                                                  :let 16}
                                                               [{:tunnus "A",
                                                                 :sijainti {:aosa 1,
                                                                            :aet 1,
                                                                            :losa 2,
                                                                            :let 1}}
                                                                {:tunnus "B",
                                                                 :sijainti {:aosa 2,
                                                                            :aet 1,
                                                                            :losa 5,
                                                                            :let 16}}])))

(deftest tarkista-alustatoimenpiteiden-validius
  (let [kohde {:aosa 1 :aet 1 :losa 4 :let 4}
        alustatoimenpiteet [{:sijainti {:aosa 1, :aet 1, :losa 2, :let 2}}
                            {:sijainti {:aosa 2, :aet 2, :losa 5, :let 3}}]]
    (is (thrown+?
          #(tasmaa-poikkeus
            %
            yllapitokohteet/+kohteissa-viallisia-sijainteja+
            yllapitokohteet/+viallinen-alustatoimenpiteen-sijainti+
            "Alustatoimenpide ei ole kohteen (id: 1) sisällä.")
          (yllapitokohteet/tarkista-alustatoimenpiteiden-sijainnit 1 kohde alustatoimenpiteet))
        "Kohteen ulkopuolinen alustatoimenpide otettiin kiinni")))