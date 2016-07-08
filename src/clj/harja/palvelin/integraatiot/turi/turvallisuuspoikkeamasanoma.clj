(ns harja.palvelin.integraatiot.turi.turvallisuuspoikkeamasanoma
  (:require [taoensso.timbre :as log]
            [harja.tyokalut.xml :as xml]
            [harja.geo :as geo]
            [harja.domain.turvallisuuspoikkeamat :as turpodomain]
            [harja.palvelin.integraatiot.api.tyokalut.liitteet :as liitteet])
  (:use [slingshot.slingshot :only [throw+]]))

(def +xsd-polku+ "xsd/turi/")

(def poikkeamatyyppi->numero
  {:tyotapaturma 8
   :vaaratilanne 32
   :turvallisuushavainto 64
   :muu 16})

(defn poikkeamatyypit->numerot [tyypit]
  (mapv
    (fn [tyyppi] [:tyyppi (poikkeamatyyppi->numero tyyppi)])
    tyypit))

(def ammatti->numero
  {:aluksen_paallikko 1
   :asentaja 2
   :asfalttityontekija 3
   :harjoittelija 4
   :hitsaaja 5
   :kunnossapitotyontekija 6
   :kansimies 7
   :kiskoilla_liikkuvan_tyokoneen_kuljettaja 8
   :konemies 9
   :kuorma-autonkuljettaja 10
   :liikenteenohjaaja 11
   :mittamies 12
   :panostaja 13
   :peramies 14
   :porari 15
   :rakennustyontekija 16
   :ratatyontekija 17
   :ratatyosta_vastaava 18
   :sukeltaja 19
   :sahkotoiden_ammattihenkilo 20
   :tilaajan_edustaja 21
   :turvalaiteasentaja 22
   :turvamies 23
   :tyokoneen_kuljettaja 24
   :tyonjohtaja 25
   :valvoja 26
   :veneenkuljettaja 27
   :vaylanhoitaja 28
   :muu_tyontekija 29
   :tyomaan_ulkopuolinen 30})

(def vamma->numero
  {:haavat_ja_pinnalliset_vammat 1
   :luunmurtumat 2
   :sijoiltaan_menot_nyrjahdykset_ja_venahdykset 3
   :amputoitumiset_ja_irti_repeamiset 4
   :tarahdykset_ja_sisaiset_vammat_ruhjevammat 5
   :palovammat_syopymat_ja_paleltumat 6
   :myrkytykset_ja_tulehdukset 7
   :hukkuminen_ja_tukehtuminen 8
   :aanen_ja_varahtelyn_vaikutukset 9
   :aarilampotilojen_valon_ja_sateilyn_vaikutukset 10
   :sokki 11
   :useita_samantasoisia_vammoja 12
   :muut 13
   :ei_tietoa 14})

(defn vammat->numerot [vammat]
  (mapv
    (fn [vammat] [:vammanlaatu (vamma->numero vammat)])
    vammat))

(def vahingoittunut-ruumiinosa->numero
  {:paan_alue 1
   :silmat 2
   :niska_ja_kaula 3
   :selka 4
   :vartalo 5
   :sormi_kammen 6
   :ranne 7
   :muu_kasi 8
   :nilkka 9
   :jalkatera_ja_varvas 10
   :muu_jalka 11
   :koko_keho 12
   :ei_tietoa 13})

(defn vahingoittuneet-ruumiinosat->numerot [vammat]
  (mapv
    (fn [vammat] [:vahingoittunutruumiinosa (vahingoittunut-ruumiinosa->numero vammat)])
    vammat))

(defn rakenna-tapahtumatiedot [data]
  (into [:tapahtumantiedot]
        (concat
          (poikkeamatyypit->numerot (:tyyppi data))
          [[:tapahtumapvm (xml/formatoi-paivamaara (:tapahtunut data))]
           [:tapahtumaaika (xml/formatoi-kellonaika (:tapahtunut data))]
           [:kuvaus (:kuvaus data)]])))

(defn rakenna-tapahtumapaikka [data]
  [:tapahtumapaikka
   [:paikka (:paikan-kuvaus data)]])

(defn rakenna-syyt-ja-seuraukset [data]
  (into [:syytjaseuraukset]
        (concat
          [[:seuraukset (:seuraukset data)]
           [:ammatti (ammatti->numero (:tyontekijanammatti data))]
           [:ammattimuutarkenne (:tyontekijanammattimuu data)]]
          (vammat->numerot (:vammat data))
          (vahingoittuneet-ruumiinosat->numerot (:vahingoittuneetruumiinosat data))
          [[:sairauspoissaolot (or (:sairauspoissaolopaivat data) 0)]
           [:sairauspoissaolojatkuu (true? (:sairauspoissaolojatkuu data))]
           [:sairaalahoitovuorokaudet (or (:sairaalavuorokaudet data) 0)]])))

(defn rakenna-tapahtumakasittely [data]
  [:tapahtumankasittely
   [:otsikko (:tapahtuman-otsikko data)]
   [:luontipvm (xml/formatoi-paivamaara (:luotu data))]
   [:tila (turpodomain/kuvaa-turpon-tila (:tila data))]])

(defn rakenna-poikkeamatoimenpide [data]
  [:poikkeamatoimenpide
   [:otsikko "string"]
   [:kuvaus "string"]
   [:toteuttaja "string"]
   [:tila "1"]])

(defn rakenna-poikkeamaliite [data]
  (mapv (fn [liite]
          [:poikkeamaliite
           [:tiedostonimi (:nimi liite)]
           [:tiedosto (:sisalto liite)]])
        (:liitteet data)))

(defn muodosta-viesti [data]
  (into [:imp:poikkeama {:xmlns:imp "http://importexport.xml.turi.oikeatoliot.fi"}]
        (concat
          [(rakenna-tapahtumatiedot data)
           (rakenna-tapahtumapaikka data)
           (rakenna-syyt-ja-seuraukset data)
           (rakenna-tapahtumakasittely data)
           (rakenna-poikkeamatoimenpide data)]
          (rakenna-poikkeamaliite data))))

(defn muodosta [data]
  (let [sisalto (muodosta-viesti data)
        xml (xml/tee-xml-sanoma sisalto)]
    (if (xml/validoi +xsd-polku+ "poikkeama-rest.xsd" xml)
      xml
      (let [virheviesti "Turvallisuuspoikkeamaa ei voida lähettää. XML ei ole validia."]
        (log/error virheviesti)
        (throw+ {:type :invalidi-turvallisuuspoikkeama-xml
                 :error virheviesti})))))
