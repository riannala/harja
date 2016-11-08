(ns harja-laadunseuranta.ui.paatason-navigointi
  (:require [reagent.core :as reagent :refer [atom]]
            [harja-laadunseuranta.tiedot.asetukset.kuvat :as kuvat]
            [harja-laadunseuranta.ui.ilmoitukset :as ilmoitukset])
  (:require-macros
    [harja-laadunseuranta.macros :as m]
    [cljs.core.async.macros :refer [go go-loop]]
    [devcards.core :as dc :refer [defcard deftest]]))

(defn toggle-painike [otsikko ikoni tyyppi]
  (let [toggle-painike-painettu #(case tyyppi
                                  :piste
                                  (do
                                    ;; TODO Luo havainto
                                    (ilmoitukset/ilmoita
                                     (str "Pistemäinen havainto kirjattu: " otsikko)))
                                  :vali
                                  (do
                                    ;; TODO Aseta väli päälle
                                    ))]
    (fn []
      [:div.toggle-valintapainike {:on-click toggle-painike-painettu}
       [:div.toggle-valintapainike-ikoni
        (when ikoni
          [:img {:src (kuvat/havainto-ikoni ikoni)}])]
       [:div.toggle-valintapainike-otsikko
        otsikko]])))

(defn paatason-navigointi [valilehdet]
  (let [nakyvissa? (atom true)
        valittu (atom (:avain (first valilehdet)))
        aseta-valinta! (fn [uusi-valinta]
                         (.log js/console "Vaihdetaan tila: " (str uusi-valinta))
                         (reset! valittu uusi-valinta))
        piilotusnappi-klikattu (fn []
                                 (swap! nakyvissa? not))]
    (fn []
      [:div {:class (str "paatason-navigointi-container "
                         (if @nakyvissa?
                           "paatason-navigointilaatikko-nakyvissa"
                           "paatason-navigointilaatikko-piilossa"))}
       [:div.nayttonappi {:on-click piilotusnappi-klikattu}]
       [:div.paatason-navigointilaatikko
        [:div.piilotusnappi {:on-click piilotusnappi-klikattu}]

        [:header
         [:ul.valilehtilista
          (doall
            (for [{:keys [avain] :as valilehti} valilehdet]
              ^{:key avain}
              [:li {:class (str "valilehti "
                                (when (= avain
                                         @valittu)
                                  "valilehti-valittu"))
                    :on-click #(aseta-valinta! avain)}
               (:nimi valilehti)]))]]
        [:div.sisalto
         [:div.valintapainikkeet
          (let [{:keys [sisalto] :as valittu-valilehti}
                (first (filter
                         #(= (:avain %) @valittu)
                         valilehdet))]
            (doall (for [{:keys [nimi ikoni tyyppi]} (sort-by :nimi sisalto)]
                     ^{:key nimi}
                     [toggle-painike nimi ikoni tyyppi])))]]
        [:footer]]])))