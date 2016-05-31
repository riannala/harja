(ns harja.palvelin.raportointi.raportit.toimenpidekilometrit
  "Toimenpidekilometrit-raportti. Näyttää kuinka paljon kutakin kok. hint. työtä on tehty eri urakoissa."
  (:require [harja.palvelin.raportointi.raportit.yleinen :as yleinen]
            [harja.pvm :as pvm]
            [harja.tyokalut.functor :refer [fmap]]
            [jeesql.core :refer [defqueries]]
            [harja.kyselyt.urakat :as urakat-q]
            [harja.kyselyt.hallintayksikot :as hallintayksikot-q]
            [harja.palvelin.raportointi.raportit.yleinen :refer [raportin-otsikko]]
            [taoensso.timbre :as log]
            [harja.domain.hoitoluokat :as hoitoluokat]))

(defqueries "harja/palvelin/raportointi/raportit/toimenpidekilometrit.sql")

(defn alueen-hoitoluokkasarakkeet [alue hoitoluokat toteumat]
  (mapv
    (fn [hoitoluokka]
      (let [sopivat-rivit []]
        (reduce
          (fn [tulos seuraava]
            (+ tulos (or seuraava 0)))
          0
          (map :maara sopivat-rivit))))
    hoitoluokat))

(defn aluesarakkeet [alueet hoitoluokat toteumat]
  (mapcat
    (fn [alue]
      (alueen-hoitoluokkasarakkeet alue hoitoluokat toteumat))
    alueet))

(defn muodosta-datarivit [alueet hoitoluokat toteumat]
  (let [kilometrimaaraiset-toteumat (filter #(= (:yksikko %) "tiekm") toteumat)
        kappalemaaraiset-toteumat (filter #(= (:yksikko %) "kpl") toteumat)
        tehtava-nimet (into #{} (distinct (map :toimenpidekoodi-nimi toteumat)))]
    ;; Tehdään rivi jokaista tehtävää kohden
    (mapv
      (fn [{:keys [toimenpidekoodi-nimi yksikko]}]
        (concat
          [(str toimenpidekoodi-nimi " (" yksikko ")")]
          (aluesarakkeet alueet hoitoluokat toteumat)))
      tehtava-nimet)))

(defn suorita [db user {:keys [alkupvm loppupvm hoitoluokat urakka-id
                               hallintayksikko-id urakkatyyppi] :as parametrit}]
  (let [konteksti (cond urakka-id :urakka
                        hallintayksikko-id :hallintayksikko
                        :default :koko-maa)
        hoitoluokat (or hoitoluokat
                        ;; Jos hoitoluokkia ei annettu, näytä kaikki (työmaakokous)
                        (into #{} (map :numero) hoitoluokat/talvihoitoluokat))
        talvihoitoluokat (filter #(hoitoluokat (:numero %)) hoitoluokat/talvihoitoluokat)
        naytettavat-alueet (yleinen/naytettavat-alueet db konteksti
                                                       {:urakka urakka-id
                                                        :hallintayksikko hallintayksikko-id
                                                        :urakkatyyppi (when urakkatyyppi (name urakkatyyppi))
                                                        :alku alkupvm
                                                        :loppu loppupvm})
        toteumat (hae-kokonaishintaiset-toteumat db {:urakka urakka-id
                                                     :hallintayksikko hallintayksikko-id
                                                     :urakkatyyppi (when urakkatyyppi (name urakkatyyppi))
                                                     :alku alkupvm
                                                     :loppu loppupvm})
        raportin-nimi "Toimenpidekilometrit"
        otsikko (raportin-otsikko
                  (case konteksti
                    :urakka (:nimi (first (urakat-q/hae-urakka db urakka-id)))
                    :hallintayksikko (:nimi (first (hallintayksikot-q/hae-organisaatio db hallintayksikko-id)))
                    :koko-maa "KOKO MAA")
                  raportin-nimi alkupvm loppupvm)
        otsikkorivit (into [] (concat
                                [{:otsikko "Hoi\u00ADto\u00ADluok\u00ADka"}]
                                (mapcat
                                  (fn [_]
                                    (map (fn [{:keys [nimi]}]
                                           {:otsikko nimi :tasaa :keskita})
                                         talvihoitoluokat))
                                  naytettavat-alueet)))
        datarivit (muodosta-datarivit naytettavat-alueet hoitoluokat toteumat)]
    [:raportti {:nimi "Toimenpidekilometrit"
                :orientaatio :landscape}
     [:taulukko {:otsikko otsikko
                 :tyhja (if (empty? toteumat) "Ei raportoitavia tehtäviä.")
                 :rivi-ennen (concat
                               [{:teksti "Alue" :sarakkeita 1}]
                               (mapv
                                 (fn [{:keys [urakka-nimi hallintayksikko-nimi] :as alue}]
                                   {:teksti (or urakka-nimi hallintayksikko-nimi)
                                    :sarakkeita (count talvihoitoluokat)})
                                     naytettavat-alueet))
                 :sheet-nimi raportin-nimi}
      otsikkorivit
      datarivit]]))
