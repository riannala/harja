(ns harja.domain.tierekisteri-tietue
  (:require [clojure.string :as str]
    #?@(:cljs [[harja.loki :refer [log]]]
        :clj  [
            [taoensso.timbre :as log]])
            [harja.tyokalut.merkkijono :as merkkijono]
            [harja.tyokalut.merkkijono :as merkkijono]
            [harja.pvm :as pvm]))

(defn- jarjesta-ja-suodata-tietolajin-kuvaus [tietolajin-kuvaus]
  (sort-by :jarjestysnumero (filter :jarjestysnumero (:ominaisuudet tietolajin-kuvaus))))

(defn- heita-poikkeus [tietolaji virhe]
  (let [viesti (str "Virhe tietolajin " tietolaji " arvojen käsittelyssä: " virhe)]
    (throw (Exception. viesti))))

(defn- validoi-arvo [tietolaji {:keys [kenttatunniste pakollinen pituus]} arvo]
  (when (and pakollinen (not arvo))
    (heita-poikkeus tietolaji (str "Pakollinen arvo puuttuu kentästä: " kenttatunniste)))
  (when (< pituus (count arvo))
    (heita-poikkeus tietolaji (str "Liian pitkä arvo kentässä: " kenttatunniste " maksimipituus: " pituus))))

(defn- castaa-teksti-kentan-mukaiseen-tyyppiin [arvo-tekstina kentan-kuvaus]
  (condp = (:tietotyyppi kentan-kuvaus)
    "merkkijono" arvo-tekstina
    "numeerinen" (when-not (merkkijono/vaadi-kokonaisluku
                             (Integer/parseInt arvo-tekstina)))
    "paivamaara" (when-not (merkkijono/vaadi-iso-8601-paivamaara
                             (pvm/iso-8601->pvm arvo-tekstina)))
    "koodisto"))

(defn- hae-arvo
  "Ottaa arvot-stringin ja etsii sieltä halutun arvon käyttäen apuna kenttien-kuvaukset -mappia.
   Palauttaa arvon castattuna oikeaan tietotyyppiin."
  [arvot-merkkijono kenttien-kuvaukset jarjestysnumero]
  (let [jarjestysnumeron-kentta (first (filter #(= (:jarjestysnumero %) jarjestysnumero)
                                               kenttien-kuvaukset))
        alkuindeksi (apply +
                           (map :pituus
                                (filter #(< (:jarjestysnumero %) jarjestysnumero)
                                        kenttien-kuvaukset)))
        loppuindeksi (+ alkuindeksi (:pituus jarjestysnumeron-kentta))
        arvo-teksti (clojure.string/trim (subs arvot-merkkijono alkuindeksi loppuindeksi))
        arvo-castattu (castaa-teksti-kentan-mukaiseen-tyyppiin arvo-teksti jarjestysnumeron-kentta)]
    arvo-castattu))

(defn- castaa-kentta-stringiksi [arvo kentan-kuvaus]
  (condp = (:tietotyyppi kentan-kuvaus)
    "merkkijono" arvo
    "numeerinen" (str arvo)
    "paivamaara"
    "koodisto"
    ))

(defn- muodosta-kentta [tietolaji arvot-map {:keys [pituus kenttatunniste] :as kentan-kuvaus}]
  (let [arvo (get arvot-map kenttatunniste)]
    (validoi-arvo tietolaji kentan-kuvaus arvo)
    (merkkijono/tayta-oikealle pituus arvo)))

(defn tietolajin-arvot-map->string
  "Ottaa arvot-mapin ja purkaa sen stringiksi käyttäen apuna annettua tietolajin kuvausta.
  Tietolajin kuvaus on tierekisterin palauttama kuvaus tietolajista, muunnettuna Clojure-mapiksi."
  [arvot-map tietolajin-kuvaus]
  (let [tietolaji (:tunniste tietolajin-kuvaus)
        kenttien-kuvaukset (jarjesta-ja-suodata-tietolajin-kuvaus tietolajin-kuvaus)
        string-osat (map (partial muodosta-kentta tietolaji arvot-map) kenttien-kuvaukset)]
    (str/join string-osat)))

(defn- pura-kentta [arvot-merkkijono
                    tietolaji
                    kenttien-kuvaukset
                    {:keys [jarjestysnumero kenttatunniste] :as kentan-kuvaus}]
  (let [arvo (hae-arvo arvot-merkkijono kenttien-kuvaukset jarjestysnumero)]
    (validoi-arvo tietolaji kentan-kuvaus arvo)
    {kenttatunniste arvo}))

(defn tietolajin-arvot-merkkijono->map
  "Ottaa arvot-stringin ja purkaa sen mapiksi käyttäen apuna annettua tietolajin kuvausta.
  Tietolajin kuvaus on tierekisterin palauttama kuvaus tietolajista, muunnettuna Clojure-mapiksi."
  [arvot-merkkijono tietolajin-kuvaus]
  (let [tietolaji (:tunniste tietolajin-kuvaus)
        kenttien-kuvaukset (jarjesta-ja-suodata-tietolajin-kuvaus tietolajin-kuvaus)
        map-osat (mapv
                   (partial pura-kentta arvot-merkkijono tietolaji kenttien-kuvaukset)
                   kenttien-kuvaukset)]
    (reduce merge map-osat)))


