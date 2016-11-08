(ns harja.palvelin.raportointi.raportit.indeksitarkistus
  (:require [harja.palvelin.raportointi.raportit.laskutusyhteenveto
             :refer [hae-laskutusyhteenvedon-tiedot]]
            [harja.pvm :as pvm]
            [harja.palvelin.raportointi.raportit.yleinen :as yleinen]
            [taoensso.timbre :as log]
            [harja.kyselyt.urakat :as urakat-q]
            [harja.kyselyt.hallintayksikot :as hallintayksikot-q]
            [harja.tyokalut.functor :refer [fmap]]))

(defn summa [laskutusyhteenvedot avain]
  (reduce + 0
          (keep avain laskutusyhteenvedot)))

(defn indeksitaulukko [nimi kuukaudet laskutusyhteenvedot-kk]
  (let [summa-kk (memoize (fn [kk kentta]
                            (summa (laskutusyhteenvedot-kk kk) kentta)))
        suola? (#{"Kaikki yhteensä" "Talvihoito"} nimi)
        kentat [:kht_laskutetaan_ind_korotus
                :yht_laskutetaan_ind_korotus
                :erilliskustannukset_laskutetaan_ind_korotus
                :bonukset_laskutetaan_ind_korotus
                :muutostyot_laskutetaan_ind_korotus
                :akilliset_hoitotyot_laskutetaan_ind_korotus
                :sakot_laskutetaan_ind_korotus]
        kentat (if suola?
                 (conj kentat :suolasakot_laskutetaan_ind_korotus)
                 kentat)]
    [:taulukko {:otsikko nimi
                :viimeinen-rivi-yhteenveto? true}
     (into [{:otsikko "Kuukausi" :leveys 2}
            {:otsikko "Kokonais\u00ADhintaiset työt" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Yksikkö\u00ADhintaiset työt" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Erillis\u00ADkustannukset" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Bonus" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Muutos- ja lisä\u00ADtyöt" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Äkillinen hoitotyö" :leveys 2 :fmt :raha :tasaa :oikea}
            {:otsikko "Sanktiot" :leveys 2 :fmt :raha :tasaa :oikea}]
           (concat
             (when suola?
               [{:otsikko "Suolabonukset ja -sanktiot" :leveys 2 :fmt :raha :tasaa :oikea}])
             [{:otsikko "Yhteensä (€)" :leveys (if suola? 2 4) :fmt :raha :tasaa :oikea}]))

     (into
       []
       (concat
         (for [[alku _ :as kk] kuukaudet
               :let [kentan-arvot (map #(summa-kk kk %) kentat)]]
           (into [(pvm/kuukauden-lyhyt-nimi (pvm/kuukausi alku))]
                 (concat kentan-arvot
                         [(reduce + kentan-arvot)])))
         (let [summat (for [kentta kentat]
                        (reduce + (map #(summa-kk % kentta) kuukaudet)))]
           [(into ["Yhteensä"]
                  (concat
                    summat
                    [(reduce + summat)]))])))]))

(defn suorita [db user {:keys [alkupvm loppupvm urakka-id hallintayksikko-id] :as parametrit}]
  (let [urakat (yleinen/hae-kontekstin-urakat db {:urakka urakka-id
                                                  :hallintayksikko hallintayksikko-id
                                                  :urakkatyyppi "hoito"
                                                  :alku alkupvm :loppu loppupvm})
        urakka-idt (mapv :urakka-id urakat)
        kuukaudet (yleinen/kuukausivalit alkupvm loppupvm)
        laskutusyhteenvedot-kk (zipmap kuukaudet
                                       (map
                                         (fn [[alku loppu]]
                                           (->> urakka-idt
                                                (mapcat #(hae-laskutusyhteenvedon-tiedot
                                                          db user {:urakka-id %
                                                                   :alkupvm alku
                                                                   :loppupvm loppu}))))
                                         kuukaudet))
        tuotteet (into #{}
                       (mapcat
                         (fn [kuukauden-laskutusyhteenvedot]
                           (map :nimi kuukauden-laskutusyhteenvedot)))
                       (vals laskutusyhteenvedot-kk))
        alueen-nimi (if urakka-id
                       (:nimi (first (urakat-q/hae-urakka db urakka-id)))
                       (if hallintayksikko-id
                         (:nimi (first (hallintayksikot-q/hae-organisaatio db hallintayksikko-id)))
                         "KOKO MAA"))
        ;; Tehdään jokaiselle tuottelle omat kk-rivit tarkempia
        ;; taulukoita varten
        tuotteen-laskutusyhteenvedot-kk
        (zipmap tuotteet
                (map (fn [tuote]
                       (fmap (fn [kk-rivit]
                               (filter #(= (:nimi %) tuote) kk-rivit))
                             laskutusyhteenvedot-kk))
                     tuotteet))]

    (into []
          (concat [:raportti {:nimi (str "Indeksitarkistusraportti " alueen-nimi " " (pvm/pvm alkupvm) " - " (pvm/pvm loppupvm))}]
             [(indeksitaulukko "Kaikki yhteensä" kuukaudet laskutusyhteenvedot-kk)]
             (for [tuote tuotteet]
               (indeksitaulukko tuote kuukaudet (get tuotteen-laskutusyhteenvedot-kk tuote)))))))