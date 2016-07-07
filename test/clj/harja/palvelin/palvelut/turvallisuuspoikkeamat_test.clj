(ns harja.palvelin.palvelut.turvallisuuspoikkeamat-test
  (:require [clojure.test :refer :all]
            [taoensso.timbre :as log]
            [harja.palvelin.komponentit.tietokanta :as tietokanta]
            [harja.palvelin.palvelut.turvallisuuspoikkeamat :as tp]
            [harja.testi :refer :all]
            [clojure.core.match :refer [match]]
            [com.stuartsierra.component :as component]
            [harja.pvm :as pvm]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn jarjestelma-fixture [testit]
  (alter-var-root #'jarjestelma
                  (fn [_]
                    (component/start
                      (component/system-map
                        :db (tietokanta/luo-tietokanta testitietokanta)
                        :http-palvelin (testi-http-palvelin)
                        :hae-turvallisuuspoikkeamat (component/using
                                                      (tp/->Turvallisuuspoikkeamat)
                                                      [:http-palvelin :db])
                        :tallenna-turvallisuuspoikkeama (component/using
                                                          (tp/->Turvallisuuspoikkeamat)
                                                          [:http-palvelin :db])))))
  (testit)
  (alter-var-root #'jarjestelma component/stop))

(use-fixtures :once (compose-fixtures
                      jarjestelma-fixture
                      urakkatieto-fixture))

(deftest hae-turvallisuuspoikkeamat-test
  (let [vastaus (kutsu-palvelua (:http-palvelin jarjestelma)
                                :hae-turvallisuuspoikkeamat +kayttaja-jvh+
                                {:urakka-id @oulun-alueurakan-2005-2010-id
                                 :alku (pvm/luo-pvm (+ 1900 105) 9 1)
                                 :loppu (pvm/luo-pvm (+ 1900 106) 8 30)})]
    (is (match vastaus [{:id _
                         :ilmoituksetlahetetty nil
                         :kasitelty (_ :guard #(and (= (t/year (c/from-sql-date %)) 2005)
                                                    (= (t/month (c/from-sql-date %)) 10)
                                                    (= (t/day (c/from-sql-date %)) 5)))
                         :kommentti {:tyyppi nil}
                         :korjaavattoimenpiteet []
                         :kuvaus "Sepolla oli kiire lastata laatikot, ja torni kaatui päälle. Ehti onneksi pois alta niin ei henki lähtenyt."
                         :lahetetty nil
                         :lahetysonnistunut nil
                         :luotu _
                         :sairaalavuorokaudet 1
                         :sairauspoissaolopaivat 7
                         :sijainti {:coordinates [435847.0
                                                  7216217.0]
                                    :type :point}
                         :tapahtunut (_ :guard #(and (= (t/year (c/from-sql-date %)) 2005)
                                                     (= (t/month (c/from-sql-date %)) 9)
                                                     (= (t/day (c/from-sql-date %)) 30)))
                         :tr {:alkuetaisyys 6
                              :alkuosa 6
                              :loppuetaisyys 6
                              :loppuosa 6
                              :numero 6}
                         :tyontekijanammatti :porari
                         :tyontekijanammattimuu nil
                         :tyyppi #{:tyotapaturma}
                         :urakka 1
                         :vaaralliset-aineet #{}
                         :vahingoittuneetruumiinosat #{}
                         :vahinkoluokittelu #{}
                         :vammat #{}}]
               true))))

(defn poista-tp-taulusta
  [kuvaus]
  (let [id (ffirst (q (str "SELECT id FROM turvallisuuspoikkeama WHERE kuvaus='" kuvaus "'")))]
    (u (str "DELETE FROM korjaavatoimenpide WHERE turvallisuuspoikkeama=" id))
    (u (str "DELETE FROM turvallisuuspoikkeama_kommentti WHERE turvallisuuspoikkeama=" id))
    (u (str "DELETE FROM turvallisuuspoikkeama_liite WHERE turvallisuuspoikkeama=" id))
    (u (str "DELETE FROM turvallisuuspoikkeama WHERE id=" id))))

(deftest tallenna-turvallisuuspoikkeama-test
  (let [tp {:urakka @oulun-alueurakan-2005-2010-id
            :tapahtunut (pvm/luo-pvm (+ 1900 105) 9 1)
            :kasitelty (pvm/luo-pvm (+ 1900 105) 9 1)
            :tyontekijanammatti :kuorma-autonkuljettaja
            :kuvaus "e2e taas punaisena"
            :vammat #{:luunmurtumat}
            :sairauspoissaolopaivat 0
            :sairaalavuorokaudet 0
            :vakavuusaste :lieva
            :vaylamuoto :tie
            :tyyppi #{:tyotapaturma}
            :otsikko "Kävi möhösti"
            :tila :avoin
            :vahinkoluokittelu #{:ymparistovahinko}
            :sijainti {:type :point :coordinates [0 0]}
            :tr {:numero 1 :alkuetaisyys 2 :loppuetaisyys 3 :alkuosa 4 :loppuosa 5}}
        korjaavattoimenpiteet [{:kuvaus "Ei ressata liikaa"
                                :otsikko "Ressi pois!"
                                :tila :avoin
                                :suoritettu nil
                                :vastaavahenkilo "Kaikki yhdessä"}]
        uusi-kommentti {:tekija "Teemu" :kommentti "Näin on!" :liite nil}
        hoitokausi [(pvm/luo-pvm (+ 1900 105) 9 1) (pvm/luo-pvm (+ 1900 106) 8 30)]
        hae-tp-maara (fn [] (ffirst (q "SELECT count(*) FROM turvallisuuspoikkeama;")))
        vanha-maara (hae-tp-maara)]

    (is (oikeat-sarakkeet-palvelussa?
          [:id :urakka :tapahtunut :kasitelty :tyontekijanammatti :kuvaus
           :vammat :sairauspoissaolopaivat :sairaalavuorokaudet :sijainti :tyyppi
           [:tr :numero] [:tr :alkuetaisyys] [:tr :loppuetaisyys] [:tr :alkuosa] [:tr :loppuosa]]

          :tallenna-turvallisuuspoikkeama
          {:tp tp
           :korjaavattoimenpiteet korjaavattoimenpiteet
           :uusi-kommentti uusi-kommentti
           :hoitokausi hoitokausi}))

    (is (= (hae-tp-maara) (+ 1 vanha-maara)))

    ;; Tiukka testi, datan pitää olla tallentunut oikein
    (let [uusin-tp (as-> (first (q (str "SELECT
                                  id,
                                  urakka,
                                  tapahtunut,
                                  kasitelty,
                                  sijainti,
                                  kuvaus,
                                  sairauspoissaolopaivat,
                                  sairaalavuorokaudet,
                                  tr_numero,
                                  tr_alkuosa,
                                  tr_alkuetaisyys,
                                  tr_loppuosa,
                                  tr_loppuetaisyys,
                                  vahinkoluokittelu,
                                  vakavuusaste,
                                  tyyppi,
                                  tyontekijanammatti,
                                  tyontekijanammatti_muu,
                                  aiheutuneet_seuraukset,
                                  vammat,
                                  vahingoittuneet_ruumiinosat,
                                  sairauspoissaolo_jatkuu,
                                  ilmoittaja_etunimi,
                                  ilmoittaja_sukunimi,
                                  vaylamuoto,
                                  toteuttaja,
                                  tilaaja,
                                  turvallisuuskoordinaattori_etunimi,
                                  turvallisuuskoordinaattori_sukunimi,
                                  laatija_etunimi,
                                  laatija_sukunimi,
                                  tapahtuman_otsikko,
                                  paikan_kuvaus,
                                  vaarallisten_aineiden_kuljetus,
                                  vaarallisten_aineiden_vuoto
                                  FROM turvallisuuspoikkeama
                                  ORDER BY luotu DESC
                                  LIMIT 1;")))
                         turpo
                         ;; Tapahtumapvm ja käsittely -> clj-time
                         (assoc turpo 2 (c/from-sql-date (get turpo 2)))
                         (assoc turpo 3 (c/from-sql-date (get turpo 3)))
                         ;; Vahinkoluokittelu -> set
                         (assoc turpo 13 (into #{} (.getArray (get turpo 13))))
                         ;; Tyyppi -> set
                         (assoc turpo 15 (into #{} (.getArray (get turpo 15))))
                         ;; Vammat -> set
                         (assoc turpo 19 (into #{} (.getArray (get turpo 19))))
                         ;; Vahingoittuneet ruumiinosat -> set
                         (assoc turpo 20 (into #{} (.getArray (get turpo 20)))))]
      (is (vector uusin-tp))
      (is (match uusin-tp [_
                           1
                           (_ :guard #(and (= (t/year %) 2005)
                                           (= (t/month %) 9)
                                           (= (t/day %) 30)))
                           (_ :guard #(and (= (t/year %) 2005)
                                           (= (t/month %) 9)
                                           (= (t/day %) 30)))
                           (_ :guard #(some? %))
                           "e2e taas punaisena"
                           0
                           0
                           1
                           4
                           2
                           5
                           3
                           #{"ymparistovahinko"}
                           "lieva"
                           #{"tyotapaturma"}
                           "kuorma-autonkuljettaja"
                           nil
                           nil
                           #{"luunmurtumat"}
                           #{}
                           nil
                           nil
                           nil
                           "tie"
                           nil
                           nil
                           nil
                           nil
                           nil
                           nil
                           "Kävi möhösti"
                           nil
                           false
                           false]
                 true))

      ;; Myös korjaava toimenpide kirjattu täysin oikein
      (let [turpo-id (first uusin-tp)
              korjaava-toimenpide (as-> (first (q (str "SELECT
                                                      kuvaus,
                                                      suoritettu,
                                                      otsikko,
                                                      vastuuhenkilo,
                                                      toteuttaja,
                                                      tila
                                                      FROM korjaavatoimenpide
                                                      WHERE turvallisuuspoikkeama = " turpo-id ";")))
                                        toimenpide
                                        (assoc toimenpide 1 (c/from-sql-date (get toimenpide 1))))]

          (is (number? turpo-id))
          (is (vector korjaava-toimenpide))
          (is (match korjaava-toimenpide
                     ["Ei ressata liikaa"
                      nil
                      "Ressi pois!"
                      nil
                      nil
                      "avoin"]
                     true))))

    (poista-tp-taulusta "e2e taas punaisena")))
