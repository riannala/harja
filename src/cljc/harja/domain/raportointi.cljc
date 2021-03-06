(ns harja.domain.raportointi
  (:require [harja.domain.roolit :as roolit]))

(def virhetyylit
  {:virhe "rgb(221,0,0)"
   :varoitus "rgb(255,153,0)"
   :info "rgb(0,136,204)"})


;; rajat-excel, virhetyylit-excel, ja solun-oletustyyli-excel ovat exceliin sidottuja,
;; ja täten niiden ei välttämättä tarvitsisi olla domain-namespacessa. On kuitenkin
;; perusteltua pitää ne täällä sen takia, että se helpottaa virhetyylit ja virhetyylit-
;; excel pitämistä synkassa. Haluamme tietenkin, että huomiovärit ovat kaikissa kolmessa
;; formaatissa edes melkein samat.

(def rajat-excel {:border-left :thin
                  :border-right :thin
                  :border-bottom :thin
                  :border-top :thin})

;; https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/IndexedColors.html
(def virhetyylit-excel
  {:virhe    (merge rajat-excel
                    {:background :dark_red
                     :font       {:color :white}})
   :varoitus (merge rajat-excel
                    {:background :orange
                     :font       {:color :black}})
   :info     (merge rajat-excel
                    {:background :light_turquoise
                     :font       {:color :black}})})

(defn solun-oletustyyli-excel [lihavoi? korosta?]
  (let [deep-merge (partial merge-with merge)]
    (cond-> {}
           lihavoi?
           (merge {:font {:bold true}})

           korosta?
           (deep-merge (merge rajat-excel {:background :yellow
                                                 :font       {:color :black}})))))

(defn varillinen-teksti [tyyli teksti]
  [:varillinen-teksti {:arvo teksti :tyyli tyyli}])

(def info-solu (partial varillinen-teksti :info))
(def varoitus-solu (partial varillinen-teksti :varoitus))
(def virhe-solu (partial varillinen-teksti :virhe))

(defn raporttielementti?
  "Raporttielementit ovat soluja, joissa on muutakin, kuin pelkkkä arvo.
  Käytetään erityisesti virheiden osoittamiseen (puuttuvat indeksit), mutta
  muitakin käyttötapauksia voi olla."
  [solu]
  (and (vector? solu)
       (> (count solu) 1)
       (keyword? (first solu))))

(defn sarakkeessa-raporttielementteja? [sarakkeen-indeksi taulukko-riveja]
  (let [sarakkeen-solut
        (mapcat
          (fn [rivi]
            (filter
              identity
              (map-indexed
                (fn [solun-indeksi solu] (when (= solun-indeksi sarakkeen-indeksi) solu))
                ;; Rivi voi olla mäppi, jossa optioita ja :rivi avaimen alla vektori,
                ;; tai pelkkä vektori
                (or (:rivi rivi) rivi))))
          taulukko-riveja)]
    (some raporttielementti? sarakkeen-solut)))

(defn raporttielementti-formatterilla
  "Liittää raporttielementtiin mukaan formatterin. Olettaa, että raporttielementin
  toinen arvo on mäppi, johon formatointifunktion voi liittää."
  [solu fmt]
  (if (raporttielementti? solu)
    (do
      #?(:cljs (harja.loki/log "Sain solun " (pr-str solu)))
      (when (and fmt (map? (second solu)))
        (assoc-in solu [1 :fmt] fmt)))

    ;; Jos annettu solu ei ole raporttielementti, voidaan sen arvo formatoida suoraan.
    (when fmt (fmt solu))))

(defn voi-nahda-laajemman-kontekstin-raportit? [kayttaja]
  (and (not (roolit/roolissa? roolit/tilaajan-laadunvalvontakonsultti kayttaja))
       (roolit/tilaajan-kayttaja? kayttaja)))

#?(:cljs
   (defn nykyinen-kayttaja-voi-nahda-laajemman-kontekstin-raportit? []
     (voi-nahda-laajemman-kontekstin-raportit? @harja.tiedot.istunto/kayttaja)))

(defn yrita
  "Yrittää tehdä ajaa funktion parametreineen. Jos heitetään poikkeus,
  palautetaan ensimmäinen parametri. Käytetään erityisesti formattereiden kanssa."
  [fn arvo & args]
  (try
    (apply (partial fn arvo) args)
    #?(:cljs (catch js/Object _ arvo))
    #?(:clj (catch Exception _ arvo))))