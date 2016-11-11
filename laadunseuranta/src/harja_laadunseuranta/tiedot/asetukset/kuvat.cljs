(ns harja-laadunseuranta.tiedot.asetukset.kuvat
  (:require-macros [harja-laadunseuranta.ui.inline-kuva :refer [inline-svg]]))

(defn- ikoni [nimi]
  (str "img/" nimi))

(defn havainto-ikoni [nimi]
  (str "img/havainnot/" nimi ".svg"))

(def +autonuoli+ (ikoni "nuoli.svg"))
(def +harja-logo+ (ikoni "harja_logo_soft.svg"))
(def +harja-logo-ilman-tekstia+ (ikoni "harja_logo_soft_ilman_tekstia.svg"))
(def +kamera+ (ikoni "kamera.png"))
(def +info+ (ikoni "info.svg"))
(def +check+ (ikoni "check.svg"))
(def +cross+ (ikoni "cross.svg"))
(def +spinner+ (ikoni "ajax-loader.gif"))
(def +hampurilaisvalikko+ (ikoni "hampurilaisvalikko.svg"))
(def +avattu+ (ikoni "avattu.png"))
(def +havaintopiste+ (ikoni "havaintopiste.png"))
