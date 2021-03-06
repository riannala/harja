(ns harja.palvelin.raportointi.raportit.materiaali
  "Materiaaliraportti"
  (:require [taoensso.timbre :as log]
            [harja.domain.roolit :as roolit]
            [harja.kyselyt.materiaalit :as materiaalit-q]
            [harja.kyselyt.urakat :as urakat-q]
            [harja.kyselyt.hallintayksikot :as hallintayksikot-q]
            [harja.kyselyt.konversio :as konv]
            [harja.domain.materiaali :as materiaalidomain]
            [harja.palvelin.raportointi.raportit.yleinen :refer [raportin-otsikko]]
            [harja.pvm :as pvm]
            [harja.fmt :as fmt]))

(defn muodosta-materiaaliraportti-urakalle [db user {:keys [urakka-id alkupvm loppupvm]}]
  (log/debug "Haetaan urakan toteutuneet materiaalit raporttia varten: " urakka-id alkupvm loppupvm)
  (let [toteuma-parametrit [db
                            urakka-id
                            (konv/sql-timestamp alkupvm)
                            (konv/sql-timestamp loppupvm)]
        toteutuneet-materiaalit (into []
                                      (apply materiaalit-q/hae-urakan-toteutuneet-materiaalit-raportille toteuma-parametrit))
        suunnitellut-materiaalit (into []
                                       (apply materiaalit-q/hae-urakan-suunnitellut-materiaalit-raportille toteuma-parametrit))
        suunnitellut-materiaalit-ilman-toteumia (filter
                                                  (fn [materiaali]
                                                    (not-any?
                                                      (fn [toteuma] (= (:materiaali-nimi toteuma) (:materiaalinimi materiaali)))
                                                      toteutuneet-materiaalit))
                                                  suunnitellut-materiaalit)
        lopullinen-tulos (mapv
                           (fn [materiaalitoteuma]
                             (if (nil? (:kokonaismaara materiaalitoteuma))
                               (assoc materiaalitoteuma :kokonaismaara 0)
                               materiaalitoteuma))
                           (reduce conj toteutuneet-materiaalit suunnitellut-materiaalit-ilman-toteumia))]
    lopullinen-tulos))

(defn muodosta-materiaaliraportti-hallintayksikolle [db user {:keys [hallintayksikko-id alkupvm loppupvm urakkatyyppi]}]
  (log/debug "Haetaan hallintayksikon toteutuneet materiaalit raporttia varten: " hallintayksikko-id alkupvm loppupvm)
  (let [toteutuneet-materiaalit (into []
                                      (materiaalit-q/hae-hallintayksikon-toteutuneet-materiaalit-raportille db
                                                                                                            (konv/sql-timestamp alkupvm)
                                                                                                            (konv/sql-timestamp loppupvm)
                                                                                                            hallintayksikko-id
                                                                                                            (when urakkatyyppi (name urakkatyyppi))))]
    toteutuneet-materiaalit))

(defn muodosta-materiaaliraportti-koko-maalle [db user {:keys [alkupvm loppupvm urakkatyyppi]}]
  (log/debug "Haetaan koko maan toteutuneet materiaalit raporttia varten: " alkupvm loppupvm)
  (let [toteutuneet-materiaalit (into []
                                      (materiaalit-q/hae-koko-maan-toteutuneet-materiaalit-raportille db
                                                                                                      (konv/sql-timestamp alkupvm)
                                                                                                      (konv/sql-timestamp loppupvm)
                                                                                                      (when urakkatyyppi (name urakkatyyppi))))]
    toteutuneet-materiaalit))

(defn- materiaalin-otsikko [t]
  (str (:materiaali-nimi t) " (" (:materiaali-yksikko t) ")"))


(defn suorita [db user {:keys [urakka-id
                               hallintayksikko-id alkupvm loppupvm urakkatyyppi] :as parametrit}]
  (let [konteksti (cond urakka-id :urakka
                        hallintayksikko-id :hallintayksikko
                        :default :koko-maa)
        toteumat
        (cond
          (and urakka-id alkupvm loppupvm)
          (muodosta-materiaaliraportti-urakalle db user {:urakka-id urakka-id
                                                         :alkupvm alkupvm
                                                         :loppupvm loppupvm})


          (and hallintayksikko-id alkupvm loppupvm)
          (muodosta-materiaaliraportti-hallintayksikolle db user
                                                         {:hallintayksikko-id hallintayksikko-id
                                                          :alkupvm alkupvm
                                                          :loppupvm loppupvm
                                                          :urakkatyyppi urakkatyyppi})

          (and alkupvm loppupvm)
          (muodosta-materiaaliraportti-koko-maalle db user {:alkupvm alkupvm
                                                            :loppupvm loppupvm
                                                            :urakkatyyppi urakkatyyppi}))

        raportin-nimi "Materiaaliraportti"
        otsikko (raportin-otsikko
                  (case konteksti
                    :urakka (:nimi (first (urakat-q/hae-urakka db urakka-id)))
                    :hallintayksikko (:nimi (first (hallintayksikot-q/hae-organisaatio db hallintayksikko-id)))
                    :koko-maa "KOKO MAA")
                  raportin-nimi alkupvm loppupvm)
        ;; Aluksi pitää laittaa materiaalit järjestykseen nimen (string) perusteella, sitten liittää
        ;; jokaiseen mukaan yksikkö, pitäen yllä alkuperäinen järjestys.
        materiaaliotsikot (mapv
                            (fn [materiaalin-nimi]
                              (some (fn [t]
                                      (when (= (:materiaali-nimi t) materiaalin-nimi)
                                        (materiaalin-otsikko t)))
                                    toteumat))
                            (sort-by materiaalidomain/materiaalien-jarjestys (distinct
                                                                               (map
                                                                                 #(str (:materiaali-nimi %))
                                                                                 toteumat))))
        toteumat-urakan-mukaan (when (not= konteksti :koko-maa)
                                 (group-by :urakka-nimi toteumat))
        toteumat-elyn-mukaan (when (= konteksti :koko-maa)
                               (group-by :hallintayksikko-nimi toteumat))]

    [:raportti {:nimi raportin-nimi}
     [:taulukko {:otsikko otsikko
                 :viimeinen-rivi-yhteenveto? true
                 :sheet-nimi raportin-nimi}
      (into []
            (concat
              [{:otsikko "Urakka"}]
              (map (fn [mat]
                     {:otsikko mat :fmt :numero})
                   materiaaliotsikot)))
      (keep identity
            (into
              []
              (concat
                ;; Tehdään rivi jokaiselle alueelle, jossa sen yhteenlasketut toteumat
                (for [[alue toteumat] (or toteumat-urakan-mukaan toteumat-elyn-mukaan)]
                  (into []
                        (concat [alue]
                                (let [toteumat-materiaalin-mukaan (group-by materiaalin-otsikko toteumat)]
                                  (for [m materiaaliotsikot]
                                    (reduce + (keep :kokonaismaara (toteumat-materiaalin-mukaan m))))))))

                ;; Tehdään yhteensä rivi, jossa kaikki toteumat lasketaan yhteen materiaalin perusteella
                (when (not (empty? toteumat))
                  [(concat ["Yhteensä"]
                           (let [toteumat-materiaalin-mukaan (group-by materiaalin-otsikko toteumat)]
                             (for [m materiaaliotsikot]
                               (reduce + (keep :kokonaismaara (toteumat-materiaalin-mukaan m))))))]))))]]))

    
