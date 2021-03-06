(ns harja-laadunseuranta.tarkastusreittimuunnin
  "Tämä namespace tarjoaa funktiot Harjan mobiililla laadunseurantatyökalulla tehtyjen reittimerkintöjen
   muuntamiseksi Harja-tarkastukseksi. Tärkein funktio on reittimerkinnat-tarkastuksiksi, joka
   hoitaa varsinaisen muunnostyön."
  (:require [taoensso.timbre :as log]
            [harja-laadunseuranta.tietokanta :as tietokanta]
            [harja-laadunseuranta.kyselyt :as q]
            [harja-laadunseuranta.utils :as utils]
            [harja.kyselyt.tarkastukset :as tark-q]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn etenemissuunta
  "Palauttaa 1 jos tr-osoite2 on suurempi kuin tr-osoite1.
   Palauttaa -1 jos tr-osoite2 on pienempi kuin tr-osoite1
   Palauttaa 0 jos samat.
   Jos ei jostain syystä voida määrittää, palauttaa nil"
  [tr-osoite1 tr-osoite2]
  (when (and (:aet tr-osoite1) (:aet tr-osoite2))
    (cond
      (< (:aet tr-osoite1) (:aet tr-osoite2)) -1
      (> (:aet tr-osoite1) (:aet tr-osoite2)) 1
      (= (:aet tr-osoite1) (:aet tr-osoite2)) 0)))

;; NOTE On mahdollista, että epätarkka GPS heittääkin yhden pisteen muita taaemmas, jolloin tilanne
;; tulkitaan ympärikääntymiseksi.
(defn tr-osoitteet-sisaltavat-ymparikaantymisen?
  "Ottaa kolme tr-osoitetta vectorissa ja kertoo sisältävätkö ne ympärikääntymisen. Päättely tehdään niin, että
   tutkitaan ensin mihin suuntaan edetään kahden ensimmäisen pisteen kohdalla ja jos kolmas piste eteneekin
   päinvastaiseen suuntaan, on tapahtunut ympärikääntyminen."
  [tr-osoitteet]
  (if (every? some? tr-osoitteet)
    (let [etenemissuunta-piste1-piste2 (etenemissuunta (first tr-osoitteet) (second tr-osoitteet))
          etenemissuunta-piste2-piste3 (etenemissuunta (second tr-osoitteet) (get tr-osoitteet 2))]
      (if (or (= etenemissuunta-piste1-piste2 0)
              (= etenemissuunta-piste2-piste3 0))
        false
        (not= etenemissuunta-piste1-piste2 etenemissuunta-piste2-piste3)))
    false))

(def +kahden-pisteen-valinen-sallittu-aikaero-s+ 180)

(defn- seuraava-mittausarvo-sama? [nykyinen-reittimerkinta
                                  seuraava-reittimerkinta
                                  mittaus-avain]
  (cond
    (nil? (mittaus-avain nykyinen-reittimerkinta))
    (= (nil? (mittaus-avain nykyinen-reittimerkinta))
       (nil? (mittaus-avain seuraava-reittimerkinta)))

    (number? (mittaus-avain nykyinen-reittimerkinta))
    (= (mittaus-avain nykyinen-reittimerkinta)
       (mittaus-avain seuraava-reittimerkinta))

    (vector? (mittaus-avain nykyinen-reittimerkinta))
    (every? #(= (mittaus-avain seuraava-reittimerkinta) %)
            (mittaus-avain nykyinen-reittimerkinta))))

(defn- tarkastus-jatkuu?
  "Ottaa reittimerkinnän ja järjestyksesä seuraavan reittimerkinnän ja kertoo muodostavatko ne loogisen jatkumon,
   toisin sanoen tulkitaanko seuraavan pisteen olevan osa samaa tarkastusta vai ei."
  [nykyinen-reittimerkinta seuraava-reittimerkinta]
  (and
    ;; Jatkuvat havainnot pysyvät samana myös seuraavassa pisteessä
    (= (:jatkuvat-havainnot nykyinen-reittimerkinta) (:jatkuvat-havainnot seuraava-reittimerkinta))
    ;; Seuraava piste on osa samaa tietä. Jos seuraavalle pistelle ei ole pystytty määrittelemään tietä,
    ;; niin oletetaan kuitenkin, että se on osa samaa tarkastusta niin kauan kuin osoite oikeasti vaihtuu
    (or (nil? (:tr-osoite seuraava-reittimerkinta))
        (= (get-in nykyinen-reittimerkinta [:tr-osoite :tie]) (get-in seuraava-reittimerkinta [:tr-osoite :tie])))
    ;; Edellisen pisteen kirjauksesta ei ole kulunut ajallisesti liian kauan
    ;; Jos on kulunut, emme tiedä, mitä näiden pisteiden välillä on tapahtunut, joten on turvallista
    ;; päättää edellinen tarkastus ja aloittaa uusi.
    (or
      (nil? (:aikaleima nykyinen-reittimerkinta))
      (nil? (:aikaleima seuraava-reittimerkinta))
      (<= (t/in-seconds (t/interval (c/from-sql-time (:aikaleima nykyinen-reittimerkinta))
                                    (c/from-sql-time (:aikaleima seuraava-reittimerkinta))))
          +kahden-pisteen-valinen-sallittu-aikaero-s+))
    ;; Soratiemittauksen mittausarvot pysyvät samana. Soratiemittauksessa mittausarvot voivat olla päällä
    ;; pitkän aikaa ja mittausarvot tallentuvat tällöin usealle pisteelle. Jos jokin mittausarvoista muuttuu,
    ;; halutaan tarkastuskin katkaista, jotta samat päällä olevat mittausarvot muodostavat aina oman reitin.
    ;; Edellisessä merkinnässä tehty mittaus on joko numero tai vector numeroita, jos kyseessä yhdistetty
    ;; reittimerkintä
    (and (seuraava-mittausarvo-sama? nykyinen-reittimerkinta seuraava-reittimerkinta :soratie-tasaisuus)
         (seuraava-mittausarvo-sama? nykyinen-reittimerkinta seuraava-reittimerkinta :kiinteys)
         (seuraava-mittausarvo-sama? nykyinen-reittimerkinta seuraava-reittimerkinta :polyavyys)
         (seuraava-mittausarvo-sama? nykyinen-reittimerkinta seuraava-reittimerkinta :sivukaltevuus))

    ;; Seuraava piste ei aiheuta reitin kääntymistä ympäri
    ;; PENDING GPS:n epätarkkuudesta johtuen aiheuttaa liikaa ympärikääntymisiä eikä toimi oikein, siksi kommentoitu
    #_(not (tr-osoitteet-sisaltavat-ymparikaantymisen? [; Edellinen sijainti
                                                        (:tr-osoite (get (:sijainnit nykyinen-reittimerkinta) (- (count (:sijainnit nykyinen-reittimerkinta)) 2)))
                                                        ;; Nykyinen sijainti
                                                        (:tr-osoite (last (:sijainnit nykyinen-reittimerkinta)))
                                                        ;; Seuraavan pisteen sijainti
                                                        (:tr-osoite seuraava-reittimerkinta)]))))

(defn- yhdista-reittimerkinnan-kaikki-havainnot
  "Yhdistää reittimerkinnän pistemäiset havainnot ja jatkuvat havainnot."
  [reittimerkinta]
  (if (empty? (:jatkuvat-havainnot reittimerkinta))
    (filterv some? [(:pistemainen-havainto reittimerkinta)])
    (filterv some? (conj (:jatkuvat-havainnot reittimerkinta)
                         (:pistemainen-havainto reittimerkinta)))))

(defn keskiarvo
  [numerot]
  (cond
    (nil? numerot)
    nil

    (number? numerot)
    numerot

    (empty? numerot)
    nil

    :default
    (float (with-precision 3
             (/ (apply + numerot) (count numerot))))))

(defn- paattele-tarkastustyyppi [reittimerkinta]
  (cond
    ;; Jos sisältää soratiemittauksia, tyyppi on soratiemittaus
    (or (:soratie-tasaisuus reittimerkinta)
        (:kiinteys reittimerkinta)
        (:polyavyys reittimerkinta)
        (:sivukaltevuus reittimerkinta))
    "soratie"

    ;; Muuten laatutarkastus
    :default
    "laatu"))

(defn- reittimerkinta-tarkastukseksi
  "Muuntaa reittimerkinnän Harja-tarkastukseksi.
   Reittimerkintä voi olla joko yksittäinen (pistemäinen) reittimerkintä tai
   jatkuvista havainnoista kasattu, yhdistetty reittimerkintä."
  [reittimerkinta]
  (let [yhdista-arvot-vectoriin (fn [reittimerkinta avain]
                                  (cond
                                    (nil? (avain reittimerkinta))
                                    []

                                    (number? (avain reittimerkinta))
                                    [(avain reittimerkinta)]

                                    (vector? (avain reittimerkinta))
                                    (avain reittimerkinta)))
        yhdista-mittausarvot (fn [reittimerkinta mittaus-avain]
                               (cond
                                 (nil? (mittaus-avain reittimerkinta))
                                 nil

                                 (number? (mittaus-avain reittimerkinta))
                                 (mittaus-avain reittimerkinta)

                                 (vector? (mittaus-avain reittimerkinta))
                                 (keskiarvo (mittaus-avain reittimerkinta))))]
    {:aika (:aikaleima reittimerkinta)
     :tyyppi (paattele-tarkastustyyppi reittimerkinta)
     :tarkastusajo (:tarkastusajo reittimerkinta)
     ;; Reittimerkintöjen id:t, joista tämä tarkastus muodostuu
     :reittimerkinta-idt (yhdista-arvot-vectoriin reittimerkinta :id)
     :sijainnit (or (:sijainnit reittimerkinta) [{:sijainti (:sijainti reittimerkinta)
                                                  :tr-osoite (:tr-osoite reittimerkinta)}])
     :liitteet (yhdista-arvot-vectoriin reittimerkinta :kuva)
     :vakiohavainnot (yhdista-reittimerkinnan-kaikki-havainnot reittimerkinta)
     :havainnot (:kuvaus reittimerkinta)
     :talvihoitomittaus {:talvihoitoluokka nil
                         :lumimaara (yhdista-mittausarvot reittimerkinta :lumisuus)
                         :tasaisuus (yhdista-mittausarvot reittimerkinta :talvihoito-tasaisuus)
                         :kitka (yhdista-mittausarvot reittimerkinta :kitkamittaus)
                         :ajosuunta nil
                         :lampotila_ilma (yhdista-mittausarvot reittimerkinta :lampotila)
                         :lampotila_tie nil}
     :soratiemittaus {:hoitoluokka nil
                      :tasaisuus (yhdista-mittausarvot reittimerkinta :soratie-tasaisuus)
                      :kiinteys (yhdista-mittausarvot reittimerkinta :kiinteys)
                      :polyavyys (yhdista-mittausarvot reittimerkinta :polyavyys)
                      :sivukaltevuus (yhdista-mittausarvot reittimerkinta :sivukaltevuus)}
     :laadunalitus (boolean (:laadunalitus reittimerkinta))}))

(defn viimeinen-indeksi [sekvenssi]
  (- (count sekvenssi) 1))

(defn- keraa-seuraavan-pisteen-arvot
  "Ottaa reittimerkinnän ja järjestyksessä seuraavan reittimerkinnän.
   Lisää seuraavan mittauksen tiedot edelliseen (numero tai vector)"
  [reittimerkinta seuraava-reittimerkinta arvo-avain]
  (if (nil? (arvo-avain seuraava-reittimerkinta))
    reittimerkinta

    (cond (nil? (arvo-avain reittimerkinta)) ;; Aseta arvoksi seuraava mittaus
          (assoc reittimerkinta arvo-avain (arvo-avain seuraava-reittimerkinta))

          (number? (arvo-avain reittimerkinta)) ;; Muunna vectoriksi
          (assoc reittimerkinta arvo-avain [(arvo-avain reittimerkinta)
                                            (arvo-avain seuraava-reittimerkinta)])

          (vector? (arvo-avain reittimerkinta)) ;; Lisää seuraava arvo vectoriin
          (assoc reittimerkinta arvo-avain (conj (arvo-avain reittimerkinta)
                                                 (arvo-avain seuraava-reittimerkinta))))))

(defn- keraa-seuraavan-pisteen-sijainti
  "Ottaa reittimerkinnän ja järjestyksessä seuraavan reittimerkinnän.
   Lisää seuraavan sijainnin ja TR-osoitteen tiedot edelliseen."
  [reittimerkinta seuraava-reittimerkinta]
  (if (nil? (:sijainti seuraava-reittimerkinta)) ; Käytännössä mahdoton tilanne, mutta tarkistetaan nyt kuitenkin
    reittimerkinta
    (if (nil? (:sijainnit reittimerkinta))
      (-> reittimerkinta
          (assoc :sijainnit [{:sijainti (:sijainti reittimerkinta)
                              :tr-osoite (:tr-osoite reittimerkinta)}
                             {:sijainti (:sijainti seuraava-reittimerkinta)
                              :tr-osoite (:tr-osoite seuraava-reittimerkinta)}])
          (dissoc :sijainti))
      (assoc reittimerkinta :sijainnit (conj (:sijainnit reittimerkinta) {:sijainti (:sijainti seuraava-reittimerkinta)
                                                                          :tr-osoite (:tr-osoite seuraava-reittimerkinta)})))))

(defn- keraa-seuraavan-pisteen-laadunalitus
  "Ottaa reittimerkinnän ja järjestyksessä seuraavan reittimerkinnän.
   Asettaa laadunalituksen trueksi, jos seuraavalla merkinnällä on laadunalitus."
  [reittimerkinta seuraava-reittimerkinta]
  (if (true? (:laadunalitus seuraava-reittimerkinta))
    (assoc reittimerkinta :laadunalitus true)
    reittimerkinta))

(defn- keraa-reittimerkintojen-kuvaukset
  "Yhdistää samalla jatkuvalla havainnolla olevat kuvauskentät yhteen"
  [reittimerkinta seuraava-reittimerkinta]
  (if (nil? (:kuvaus seuraava-reittimerkinta))
    reittimerkinta
    (assoc reittimerkinta :kuvaus (str (when-let [k (:kuvaus reittimerkinta)]
                                         (str k "\n")) (:kuvaus seuraava-reittimerkinta)))))

(defn- yhdista-jatkuvat-reittimerkinnat
  "Ottaa joukon reittimerkintöjä ja yhdistää ne yhdeksi loogiseksi jatkumoksi."
  [reittimerkinnat]
  (reduce
    (fn [reittimerkinnat seuraava-merkinta]
      (if (empty? reittimerkinnat)
        (conj reittimerkinnat seuraava-merkinta)
        (let [viimeisin-yhdistetty-reittimerkinta (last reittimerkinnat)]
          (if (tarkastus-jatkuu? viimeisin-yhdistetty-reittimerkinta seuraava-merkinta)
            ;; Sama tarkastus jatkuu, ota seuraavan mittauksen tiedot
            ;; ja lisää ne viimeisimpään reittimerkintään
            (assoc reittimerkinnat
              (viimeinen-indeksi reittimerkinnat)
              (as-> viimeisin-yhdistetty-reittimerkinta edellinen
                    (keraa-seuraavan-pisteen-sijainti edellinen seuraava-merkinta)
                    (keraa-seuraavan-pisteen-laadunalitus edellinen seuraava-merkinta)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :talvihoito-tasaisuus)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :lumisuus)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :kitkamittaus)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :soratie-tasaisuus)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :kiinteys)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :polyavyys)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :sivukaltevuus)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :lampotila)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :kuva)
                    (keraa-seuraavan-pisteen-arvot edellinen seuraava-merkinta :id)
                    (keraa-reittimerkintojen-kuvaukset edellinen seuraava-merkinta)))
            ;; Uusi tarkastus alkaa
            (conj reittimerkinnat seuraava-merkinta)))))
    []
    reittimerkinnat))

(defn- pistemainen-havainto?
  [reittimerkinta]
  (boolean (and (or (:pistemainen-havainto reittimerkinta)
                    ;; Kuvan tai tekstiä sisältävän merkinnän pitäisi olla aina
                    ;; lomakkeelta kirjattu pistemäinen yleishavainto,
                    ;; mutta varmistetaan nyt kuitenkin
                    (:kuva reittimerkinta)
                    (:kuvaus reittimerkinta)))))

(defn- toiseen-merkintaan-liittyva-merkinta?
  [reittimerkinta]
  (some? (:liittyy-merkintaan reittimerkinta)))

(defn- reittimerkinnat-reitillisiksi-tarkastuksiksi
  "Käy annetut reittimerkinnät läpi ja muodostaa niistä reitilliset tarkastukset"
  [reittimerkinnat]
  (let [jatkuvat-reittimerkinnat (filter #(and (not (pistemainen-havainto? %))
                                               (not (toiseen-merkintaan-liittyva-merkinta? %)))
                                         reittimerkinnat)
        yhdistetyt-reittimerkinnat (yhdista-jatkuvat-reittimerkinnat jatkuvat-reittimerkinnat)]
    (mapv reittimerkinta-tarkastukseksi yhdistetyt-reittimerkinnat)))

(defn- reittimerkinnat-pistemaisiksi-tarkastuksiksi
  "Käy annetut reittimerkinnät läpi ja muodostaa niistä pistemäiset tarkastukset"
  [reittimerkinnat]
  (let [pistemaiset-reittimerkinnat (filter #(and (pistemainen-havainto? %)
                                                  (not (toiseen-merkintaan-liittyva-merkinta? %)))
                                            reittimerkinnat)]
    (mapv reittimerkinta-tarkastukseksi pistemaiset-reittimerkinnat)))

(defn- liita-tarkastukseen-liittyvat-merkinnat
  "Etsii ja lisää tarkastukseen siihen liittyvät tiedot.
   Jos liittyviä tietoja ei ole, tarkastus palautuu sellaisenaan."
  [tarkastus liittyvat-merkinnat]
  (let [tarkastukseen-liittyvat-merkinnat (filter
                                            #((into #{} (:reittimerkinta-idt tarkastus))
                                               (:liittyy-merkintaan %))
                                            liittyvat-merkinnat)]
    (if (empty? tarkastukseen-liittyvat-merkinnat)
      tarkastus
      (merge tarkastus
             ;; Lisätään mahdolliset kuvaukset perään rivinvaihdoilla erotettuna
             {:havainnot (let [kuvaukset (map :kuvaus tarkastukseen-liittyvat-merkinnat)]
                           (if (empty? kuvaukset)
                             (:havainnot tarkastus)
                             (str (when-let [olemassaoleva-kuvaus (:havainnot tarkastus)]
                                    (str olemassaoleva-kuvaus) "\n")
                                  (str/join "\n" (map :kuvaus tarkastukseen-liittyvat-merkinnat)))))
              ;; Lisätään mahdolliset kuvaliitteet tarkastukseen
              :liitteet (let [kuvat (map :kuva tarkastukseen-liittyvat-merkinnat)]
                          (if (empty? kuvat)
                            (:liitteet tarkastus)
                            (apply conj (:liitteet tarkastus) kuvat)))
              ;; Merkitään tarkastukseen laadunalitus jos se, tai mikä tahansa liittyvistä merkinnöistä,
              ;; sisältää laadunalituksen
              :laadunalitus (let [laadunalitukset (map :laadunalitus tarkastukseen-liittyvat-merkinnat)
                                  _ (log/debug "Laadunalitukset: " (pr-str laadunalitukset))]
                              (if (empty? laadunalitukset)
                                (:laadunalitus tarkastus)
                                (boolean (some true? (conj laadunalitukset (:laadunalitus tarkastus))))))}))))

(defn- liita-tarkastuksiin-lomakkeelta-kirjatut-tiedot
  "Ottaa mapin, jossa on reittimerkinnöistä muunnetut Harja-tarkastukset (pistemäiset ja reitilliset),
   sekä toisiin merkintöihin liittyvät merkinnät. Etsii ja lisää tarkastuksiin niihin kirjatut
   liittyvät tiedot."
  [tarkastukset liittyvat-merkinnat]
  {:reitilliset-tarkastukset (mapv #(liita-tarkastukseen-liittyvat-merkinnat % liittyvat-merkinnat)
                                   (:reitilliset-tarkastukset tarkastukset))
   :pistemaiset-tarkastukset (mapv #(liita-tarkastukseen-liittyvat-merkinnat % liittyvat-merkinnat)
                                   (:pistemaiset-tarkastukset tarkastukset))})

(defn reittimerkinnat-tarkastuksiksi
  "Reittimerkintämuunnin, joka käy reittimerkinnät läpi ja palauttaa mapin, jossa reittimerkinnät muutettu
   reitillisiksi ja pistemäisiksi Harja-tarkastuksiksi."
  [tr-osoitteelliset-reittimerkinnat]
  (let [tarkastukset {:reitilliset-tarkastukset (reittimerkinnat-reitillisiksi-tarkastuksiksi tr-osoitteelliset-reittimerkinnat)
                      :pistemaiset-tarkastukset (reittimerkinnat-pistemaisiksi-tarkastuksiksi tr-osoitteelliset-reittimerkinnat)}
        liittyvat-merkinnat (filterv toiseen-merkintaan-liittyva-merkinta?
                                     tr-osoitteelliset-reittimerkinnat)
        tarkastukset-lomaketiedoilla (liita-tarkastuksiin-lomakkeelta-kirjatut-tiedot tarkastukset
                                                                                      liittyvat-merkinnat)]
    tarkastukset-lomaketiedoilla))

;; -------- Tarkastuksen tallennus kantaan --------

(defn- muodosta-tarkastuksen-geometria
  [db {:keys [tie aosa aet losa let] :as tieosoite}]
  (when (and tie aosa aet)
    (:geom (first (q/tr-osoitteelle-viiva
              db
              {:tr_numero tie
               :tr_alkuosa aosa
               :tr_alkuetaisyys aet
               :tr_loppuosa (or losa aosa)
               :tr_loppuetaisyys (or let aet)})))))

(defn- kasittele-pistemainen-tarkastusreitti
  "Asettaa tieosoitteen paatepisteen (losa / let) nilliksi jos sama kuin lahtopiste (aosa / aet)"
  [osoite]
  (if (and (= (:aosa osoite)
              (:losa osoite))
           (= (:aet osoite)
              (:let osoite)))
    (-> osoite
        (assoc :losa nil :let nil))
    osoite))

(defn luo-kantaan-tallennettava-tarkastus
  "Ottaa reittimerkintämuuntimen luoman tarkastuksen ja palauttaa mapin,
   jolla tarkastus voidaan lisätä kantaan.

   Reittimerkintämuuntimen luoma tarkastus koostuu joko yhdestä tai useammasta
   sijaintipisteestä sen mukaan onko kyse pistemäisestä vai reitillisestä tarkastuksesta
   On tosin mahdollista, että myös reitillinen tarkastus on tallentunut vain yhdellä sijainnilla."
  [db tarkastus kayttaja]
  (let [tarkastuksen-reitti (:sijainnit tarkastus)
        lahtopiste (:tr-osoite (first tarkastuksen-reitti))
        paatepiste (:tr-osoite (last tarkastuksen-reitti))
        koko-tarkastuksen-tr-osoite {:tie (:tie lahtopiste)
                                     :aosa (:aosa lahtopiste)
                                     :aet (:aet lahtopiste)
                                     :losa (or (:losa paatepiste) (:aosa paatepiste))
                                     :let (or (:let paatepiste) (:aet paatepiste))}
        ;; Pistemäisessä sekä lähtö- että paatepiste ovat samat, jolloin losa ja let ovat samat. Käsitellään ne:
        koko-tarkastuksen-tr-osoite (kasittele-pistemainen-tarkastusreitti koko-tarkastuksen-tr-osoite)
        geometria (muodosta-tarkastuksen-geometria db koko-tarkastuksen-tr-osoite)]

    (assoc tarkastus
      :tarkastaja (str (:etunimi kayttaja) " " (:sukunimi kayttaja))
      :tr_numero (:tie koko-tarkastuksen-tr-osoite)
      :tr_alkuosa (:aosa koko-tarkastuksen-tr-osoite)
      :tr_alkuetaisyys (:aet koko-tarkastuksen-tr-osoite)
      :tr_loppuosa (:losa koko-tarkastuksen-tr-osoite)
      :tr_loppuetaisyys (:let koko-tarkastuksen-tr-osoite)
      :sijainti geometria
      :lahde "harja-ls-mobiili")))

(defn- tallenna-tarkastus! [db tarkastus kayttaja]
  (log/debug "Aloitetaan tarkastuksen tallennus")
  (let [tarkastus (luo-kantaan-tallennettava-tarkastus db tarkastus kayttaja)
        _ (q/luo-uusi-tarkastus<! db
                                  (merge tarkastus
                                         {:luoja (:id kayttaja)}))
        _ (log/debug "Uusi tarkastus luotu!")
        tarkastus-id (tark-q/luodun-tarkastuksen-id db)
        sisaltaa-talvihoitomittauksen? (not (empty? (remove nil? (vals (:talvihoitomittaus tarkastus)))))
        sisaltaa-soratiemittauksen? (not (empty? (remove nil? (vals (:soratiemittaus tarkastus)))))]


    (doseq [vakiohavainto-id (:vakiohavainnot tarkastus)]
      (log/debug "Tallennetaan vakiohavainnot: " (pr-str (:vakiohavainnot tarkastus)))
      (q/luo-uusi-tarkastuksen-vakiohavainto<! db
                                               {:tarkastus tarkastus-id
                                                :vakiohavainto vakiohavainto-id}))
    (when sisaltaa-talvihoitomittauksen?
      (log/debug "Tallennetaan talvihoitomittaus: " (pr-str (:talvihoitomittaus tarkastus)))
      (q/luo-uusi-talvihoitomittaus<! db
                                      (merge (:talvihoitomittaus tarkastus)
                                             {:tarkastus tarkastus-id})))
    (when sisaltaa-soratiemittauksen?
      (log/debug "Tallennetaan soratiemittaus: " (pr-str (:soratiemittaus tarkastus)))
      (q/luo-uusi-soratiemittaus<! db
                                   (merge (:soratiemittaus tarkastus)
                                          {:tarkastus tarkastus-id})))
    (doseq [liite (:liitteet tarkastus)]
      (log/debug "Tallennetaan liite (yksi monesta): " (pr-str liite))
      (q/luo-uusi-tarkastus-liite<! db
                                    {:tarkastus tarkastus-id
                                     :liite liite}))
    (log/debug "Tarkastuksen tallennus suoritettu")))

(defn tallenna-tarkastukset!
  "Tallentaa reittimerkintämuuntimen luomat tarkastukset kantaan"
  [db tarkastukset kayttaja]
  (let [kaikki-tarkastukset (reduce conj
                                    (:pistemaiset-tarkastukset tarkastukset)
                                    (:reitilliset-tarkastukset tarkastukset))]
    (doseq [tarkastus kaikki-tarkastukset]
      (tallenna-tarkastus! db tarkastus kayttaja))))