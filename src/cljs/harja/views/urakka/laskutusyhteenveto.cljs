(ns harja.views.urakka.laskutusyhteenveto
  "Urakan Laskutusyhteenveto välilehti:"
  (:require [reagent.core :refer [atom] :as r]
            [cljs.core.async :refer [<! >! chan]]

            [harja.ui.yleiset :refer [ajax-loader linkki livi-pudotusvalikko]]
            [harja.ui.komponentti :as komp]
            [harja.ui.ikonit :as ikonit]

            [harja.tiedot.raportit :as raportit]
            [harja.tiedot.urakka :as u]
            [harja.tiedot.navigaatio :as nav]
            [harja.views.urakka.valinnat :as valinnat]
            [harja.ui.lomake :refer [lomake]]
            [harja.loki :refer [log logt tarkkaile!]]
            [harja.ui.protokollat :refer [Haku hae]]
            [harja.domain.skeema :refer [+tyotyypit+]]
            [harja.ui.raportti :refer [muodosta-html]] 
            [harja.asiakas.kommunikaatio :as k]
            [harja.transit :as t])

  (:require-macros [cljs.core.async.macros :refer [go]]
                   [reagent.ratom :refer [reaction run!]]
                   [harja.atom :refer [reaction<!]]))


(defonce laskutusyhteenveto-nakyvissa? (atom false))

(defonce laskutusyhteenvedon-parametrit
  (reaction (let [ur @nav/valittu-urakka
                  [alkupvm loppupvm] @u/valittu-hoitokauden-kuukausi
                  nakymassa? @laskutusyhteenveto-nakyvissa?]
              (when (and ur alkupvm loppupvm nakymassa?)
                (raportit/urakkaraportin-parametrit
                 (:id ur)
                 :laskutusyhteenveto
                 {:alkupvm  alkupvm
                  :loppupvm loppupvm})))))


(defonce laskutusyhteenvedon-tiedot
  (reaction<! [p @laskutusyhteenvedon-parametrit]
              {:nil-kun-haku-kaynnissa? true}
              (when p
                (raportit/suorita-raportti p))))


(defn laskutusyhteenveto
  []
  (komp/luo
    (komp/lippu laskutusyhteenveto-nakyvissa?)
    (fn []
      (let [ur @nav/valittu-urakka
            tiedot @laskutusyhteenvedon-tiedot
            valittu-aikavali @u/valittu-hoitokauden-kuukausi]
        [:span.laskutusyhteenveto
         [:h3 "Laskutusyhteenveto"]
         [valinnat/urakan-hoitokausi ur]
         [valinnat/hoitokauden-kuukausi]
         
         (when-let [p @laskutusyhteenvedon-parametrit]
           [:span
            ^{:key "raporttixls"}
            [:form {:style {:float "right"} :target "_blank" :method "POST"
                    :action (k/excel-url :raportointi)}
             [:input {:type "hidden" :name "parametrit"
                      :value (t/clj->transit p)}]
             [:button.nappi-ensisijainen {:type "submit"}
              (ikonit/print)
              " Tallenna Excel"]]
            ^{:key "raporttipdf"}
            [:form {:style {:float "right"} :target "_blank" :method "POST"
                    :action (k/pdf-url :raportointi)}
            [:input {:type "hidden" :name "parametrit"
                     :value (t/clj->transit p)}]
            [:button.nappi-ensisijainen {:type "submit"}
             (ikonit/print)
             " Tallenna PDF"]]])
         
         (when-let [tiedot @laskutusyhteenvedon-tiedot]
           [muodosta-html (assoc-in tiedot [1 :tunniste] :laskutusyhteenveto)])]))))
