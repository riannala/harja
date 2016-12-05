(ns harja.palvelin.raportointi.excel
  "Harja raporttielementtien vienti Excel muotoon"
  (:require [taoensso.timbre :as log]
            [dk.ative.docjure.spreadsheet :as excel]
            [clojure.string :as str]
            [harja.domain.raportointi :as raportti-domain])
  (:import (org.apache.poi.ss.util CellReference WorkbookUtil CellRangeAddress CellUtil)
           (org.apache.poi.ss.usermodel CellStyle)))

(defmulti muodosta-excel
  "Muodostaa Excel data annetulle raporttielementille.
  Dispatch tyypin mukaan (vektorin 1. elementti)."
  (fn [elementti workbook]
    (assert (raportti-domain/raporttielementti? elementti))
    (first elementti)))

(defmulti muodosta-solu
  (fn [elementti tyyli]
    (if (raportti-domain/raporttielementti? elementti)
      (first elementti)
      :vain-arvo)))


(defn solu [rivi-nro sarake-nro]
  (.formatAsString (CellReference. rivi-nro sarake-nro)))

(defmulti aseta-kaava!
  (fn [[tyyppi &_] _ _ _]
    tyyppi))

(defmethod aseta-kaava! :summa-vasen [[_ alkusarake-nro] cell rivi-nro sarake-nro]
  (.setCellFormula cell
                   (str "SUM("
                        (solu rivi-nro alkusarake-nro)
                        ":"
                        (solu rivi-nro (dec sarake-nro))
                        ")")))


(defn- ilman-soft-hyphenia [data]
  (if (string? data)
    (.replace data "\u00AD" "")
    data))

(defmethod muodosta-solu :vain-arvo [arvo solun-tyyli] [arvo solun-tyyli])

(defmethod muodosta-solu :liitteet [[_ liitteet] solun-tyyli]
  [(count liitteet) solun-tyyli])

(defmethod muodosta-solu :arvo-ja-osuus [[_ {:keys [arvo osuus]}] solun-tyyli]
  [arvo solun-tyyli])

(defmethod muodosta-solu :varillinen-teksti [[_ {:keys [arvo tyyli]}] solun-tyyli]
  [arvo (merge solun-tyyli (when tyyli (tyyli raportti-domain/virhetyylit-excel)))])

(defn- taulukko-otsikkorivi [otsikko-rivi sarakkeet sarake-tyyli]
  (dorun
    (map-indexed
      (fn [sarake-nro {:keys [otsikko] :as sarake}]
        (let [cell (.createCell otsikko-rivi sarake-nro)]
          (excel/set-cell! cell (ilman-soft-hyphenia otsikko))
          (excel/set-cell-style! cell sarake-tyyli)))
      sarakkeet)))

(defn luo-data-formaatti [workbook format-str]
  (.. (.getCreationHelper workbook) createDataFormat (getFormat format-str)))

(defmethod muodosta-excel :taulukko [[_ optiot sarakkeet data] workbook]
  (try
    (let [nimi (:otsikko optiot)
          viimeinen-rivi-yhteenveto? (:viimeinen-rivi-yhteenveto? optiot)
          viimeinen-rivi (last data)
          aiempi-sheet (last (excel/sheet-seq workbook))
          [sheet nolla] (if (and (nil? (:sheet-nimi optiot))
                                 (nil? nimi)
                                 aiempi-sheet)
                          [aiempi-sheet (+ 2 (.getLastRowNum aiempi-sheet))]
                          [(excel/add-sheet! workbook
                                             (WorkbookUtil/createSafeSheetName
                                              (or (:sheet-nimi optiot) nimi))) 0])
          sarake-tyyli (excel/create-cell-style! workbook {:background :blue
                                                           :font {:color :white}})
          rivi-ennen (:rivi-ennen optiot)
          rivi-ennen-nro nolla
          rivi-ennen-rivi (when rivi-ennen (.createRow sheet nolla))

          nolla (if rivi-ennen (inc nolla) nolla)
          otsikko-rivi (.createRow sheet nolla)]

      ;; Luodaan mahdollinen rivi-ennen
      (when rivi-ennen
        (reduce (fn [sarake-nro {:keys [teksti tasaa sarakkeita] :as sarake}]
                  (let [solu (.createCell rivi-ennen-rivi sarake-nro)]
                    (excel/set-cell! solu teksti)
                    (excel/set-cell-style! solu sarake-tyyli)
                    (CellUtil/setAlignment solu workbook
                                           (case tasaa
                                             :keskita CellStyle/ALIGN_CENTER
                                             :oikea CellStyle/ALIGN_RIGHT
                                             CellStyle/ALIGN_LEFT))
                    (when (> sarakkeita 1)
                      (.addMergedRegion sheet (CellRangeAddress. rivi-ennen-nro rivi-ennen-nro
                                                                 sarake-nro
                                                                 (+ sarake-nro sarakkeita -1))))
                    (+ sarake-nro sarakkeita)))
                0 rivi-ennen))

      ;; Luodaan otsikot saraketyylillä
      (taulukko-otsikkorivi otsikko-rivi sarakkeet sarake-tyyli)

      (dorun
       (map-indexed
        (fn [rivi-nro rivi]
          (let [rivi-nro (+ nolla 1 rivi-nro)
                [data optiot] (if (map? rivi)
                                [(:rivi rivi) rivi]
                                [rivi {}])
                row (.createRow sheet rivi-nro)]
            (dorun
             (map-indexed
               (fn [sarake-nro sarake]
                 (let [cell (.createCell row sarake-nro)
                       lihavoi? (or (:lihavoi? optiot)
                                    (and viimeinen-rivi-yhteenveto?
                                         (= rivi viimeinen-rivi)))
                       korosta? (:korosta? optiot)
                       formaatti-fn (fn [tyyli]
                                      (case (:fmt sarake)
                                        ;; .setDataFormat hakee indeksillä tyylejä.
                                        ;; Tyylejä voi määritellä itse (https://poi.apache.org/apidocs/org/apache/poi/xssf/usermodel/XSSFDataFormat.html)
                                        ;; tai voimme käyttää valmiita, sisäänrakennettuja tyylejä.
                                        ;; http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/BuiltinFormats.html
                                        :raha (.setDataFormat tyyli 8)
                                        :prosentti (.setDataFormat tyyli 10)
                                        :numero (.setDataFormat tyyli 2)
                                        :pvm (.setDataFormat tyyli 14)
                                        :pvm-aika (.setDataFormat tyyli 22)
                                        nil))
                       oletustyyli (raportti-domain/solun-oletustyyli-excel lihavoi? korosta?)
                       arvo-datassa (nth data sarake-nro)
                       [naytettava-arvo solun-tyyli] (if (raportti-domain/raporttielementti? arvo-datassa)
                                         (muodosta-solu arvo-datassa oletustyyli)
                                         [arvo-datassa oletustyyli])
                       tyyli (doto (excel/create-cell-style! workbook solun-tyyli)
                               formaatti-fn)]

                   (if-let [kaava (:excel sarake)]
                     (aseta-kaava! kaava cell rivi-nro sarake-nro)
                     (excel/set-cell! cell (ilman-soft-hyphenia naytettava-arvo)))
                   (excel/set-cell-style! cell tyyli)))
              sarakkeet))))
        data))

      ;; Laitetaan automaattiset leveydet
      (dotimes [i (count sarakkeet)]
        (.autoSizeColumn sheet i)))
    (catch Throwable t
      (log/error t "Virhe Excel muodostamisessa"))))

(defmethod muodosta-excel :raportti [[_ raportin-tunnistetiedot & sisalto] workbook]
  (let [sisalto (mapcat #(if (seq? %) % [%]) sisalto)]
    (doseq [elementti (remove nil? sisalto)]
      (muodosta-excel elementti workbook)))
  (:nimi raportin-tunnistetiedot))

(defmethod muodosta-excel :default [elementti workbook]
  (log/debug "Excel ei tue elementtiä: " elementti)
  nil)
