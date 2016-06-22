(ns harja-laadunseuranta.asetukset
  (:require [clojure.string :as s]))

(defn- prefix []
  (if (#{"localhost:8000" "localhost:3000" "harja-dev5" "80.69.173.64"} (.-host js/location))
    ""
    "harja/"))

(def +wmts-url+ (str "/" (prefix) "wmts/maasto/wmts"))
(def +wmts-url-kiinteistojaotus+ (str "/" (prefix) "wmts/kiinteisto/wmts"))
(def +wmts-url-ortokuva+ (str "/" (prefix) "wmts/maasto/wmts"))

;; reitintallennus
(def +tapahtumastore+ "tapahtumat")
(def +tarkastusajostore+ "tarkastusajo")
(def +pollausvali+ 2000)
(def +tallennus-url+ (str "/" (prefix) "laadunseuranta/api/reittimerkinta"))
(def +paatos-url+ (str "/" (prefix) "laadunseuranta/api/paata-tarkastusajo"))
(def +luonti-url+ (str "/" (prefix) "laadunseuranta/api/uusi-tarkastusajo"))
(def +trosoite-haku-url+ (str "/" (prefix) "laadunseuranta/api/hae-tr-osoite"))
(def +tr-tietojen-haku-url+ (str "/" (prefix) "laadunseuranta/api/hae-tr-tiedot"))
(def +kayttajatiedot-url+ (str "/" (prefix) "laadunseuranta/api/hae-kayttajatiedot"))
(def +liitteen-tallennus-url+ (str "/" (prefix) "laadunseuranta/tallenna-liite"))

(def +persistoitavien-max-maara+ 500)

(def +kuvatyyppi+ "image/webp")

;; kartta
(def +oletuszoom+ 14)
(def +preload-taso+ 1000)
(def +heading-ikonikorjaus+ -90)
(def +reittiviivan-leveys+ 4)

;; sivupaneeli
(def +sivupaneelin-leveys+ 240)

;; jos geolokaatio-apin antaman paikkatiedon tarkkuus on heikompi kun tämä luku, paikkatietoa ei rekisteröidä
(def +tarkkuusraja+ 50)

(def +tros-haun-treshold+ 100)

;; paluu harjaan
(def +harja-url+ (str "/" (prefix) "#urakat/laadunseuranta/"))
