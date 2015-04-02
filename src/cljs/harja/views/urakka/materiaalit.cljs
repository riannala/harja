(ns harja.views.urakka.materiaalit
  (:require [reagent.core :refer [atom] :as r]
            [harja.ui.yleiset :refer [kuuntelija raksiboksi] :refer-macros [deftk]]
            [harja.tiedot.urakka.materiaalit :as t]
            [harja.loki :refer [log]]
            [harja.pvm :as pvm]
            [harja.tiedot.urakka.suunnittelu :as s]
            [harja.ui.grid :as grid]
            [harja.ui.ikonit :as ikonit]
            
            [harja.views.kartta.tasot :as tasot]
            [harja.views.kartta.pohjavesialueet :refer [hallintayksikon-pohjavesialueet-haku]]
            [harja.tiedot.navigaatio :as nav]
            [harja.tiedot.istunto :as istunto]
            [cljs-time.coerce :as tc]
            
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn aseta-hoitokausi [rivi]
  (let [[alkupvm loppupvm] @s/valittu-hoitokausi]
    ;; lisätään kaikkiin riveihin valittu hoitokausi
    (assoc rivi :alkupvm alkupvm :loppupvm loppupvm)))

  
(defn pohjavesialueiden-materiaalit-grid
  "Listaa pohjavesialueiden materiaalit ja mahdollistaa kartalta valinnan."
  [opts materiaalikoodit materiaalit]
  
  (let [karttavalinta? (atom false)
        valitse-kartalta (fn [e]
                           (.preventDefault e)
                           (if @karttavalinta?
                             (do (tasot/taso-pois! :pohjavesialueet)
                                 (reset! karttavalinta? false)
                                 (reset! nav/kartan-koko :S))
                             (do (tasot/taso-paalle! :pohjavesialueet)
                                 (reset! karttavalinta? true)
                                 (reset! nav/kartan-koko :M))))
        g (grid/grid-ohjaus)
        ]
    (kuuntelija
     {}
     (fn [{:keys [virheet voi-muokata?]} materiaalikoodit materiaalit]
        (log "MATSKUJA: " (pr-str materiaalit))
        [:div.pohjavesialueet
         [grid/muokkaus-grid
          {:otsikko "Pohjavesialueiden materiaalit"
           :tyhja "Ei pohjavesialueille kirjattuja materiaaleja."
           :voi-muokata? voi-muokata?
           :voi-poistaa? (constantly voi-muokata?)
           :ohjaus g
           :uusi-rivi aseta-hoitokausi                                
           :muokkaa-footer (fn [g]
                             [:button.btn.btn-default {:on-click valitse-kartalta}
                              (if @karttavalinta?
                                "Piilota kartta"
                                "Valitse kartalta")])
           
           :muutos (when virheet
                     #(reset! virheet (grid/hae-virheet %)))
           }
          [{:otsikko "Pohjavesialue"
            :tyyppi :haku :lahde hallintayksikon-pohjavesialueet-haku :nayta :nimi
            :muokattava? (constantly voi-muokata?)
            :nimi :pohjavesialue :fmt :nimi :leveys "40%"
            :validoi [[:ei-tyhja "Valitse pohjavesialue"]]}
           {:otsikko "Materiaali"
            :tyyppi :valinta :valinnat materiaalikoodit :valinta-nayta #(or (:nimi %) "- materiaali -")
            :muokattava? (constantly voi-muokata?)
            :nimi :materiaali :fmt :nimi :leveys "35%"
            :validoi [[:ei-tyhja "Valitse materiaali"]]}
           {:otsikko "Määrä" :nimi :maara :leveys "15%" :tyyppi :numero
            :muokattava? (constantly voi-muokata?)
            :validoi [[:ei-tyhja "Kirjoita määrä"]]}
           {:otsikko "Yks." :nimi :yksikko :hae (comp :yksikko :materiaali)  :leveys "5%"
            :tyyppi :string :muokattava? (constantly false)}]
          
          materiaalit
          ]])
     :pohjavesialue-klikattu (fn [this pohjavesialue]
                               (grid/lisaa-rivi! g {:pohjavesialue (dissoc pohjavesialue :type :aihe)})
                               (log "hei klikkasit pohjavesialuetta: " (dissoc pohjavesialue :alue))))))
      
     
(defn yleiset-materiaalit-grid [{:keys [virheet voi-muokata?]} materiaalikoodit yleiset-materiaalit-muokattu]
  [grid/muokkaus-grid
   {:otsikko "Materiaalit"
    :voi-muokata? voi-muokata?
    :voi-poistaa? (constantly voi-muokata?)
    :tyhja "Ei kirjattuja materiaaleja."
    :uusi-rivi aseta-hoitokausi
    :muutos (when virheet
              #(reset! virheet (grid/hae-virheet %)))
    }
   
   [{:otsikko "Materiaali" :nimi :materiaali :fmt :nimi :leveys "60%"
     :muokattava? (constantly voi-muokata?)
     :tyyppi :valinta :valinnat materiaalikoodit :valinta-nayta #(or (:nimi %) "- materiaali -")
     :validoi [[:ei-tyhja "Valitse materiaali"]]
     }
    {:otsikko "Määrä" :nimi :maara :leveys "30%"
     :muokattava? (constantly voi-muokata?)
     :tyyppi :numero}
    {:otsikko "Yks." :nimi :yksikko :hae (comp :yksikko :materiaali) :leveys "5%"
     :tyyppi :string :muokattava? (constantly false)}]
   
   yleiset-materiaalit-muokattu])


  
(deftk materiaalit [ur]
  [;; haetaan kaikki materiaalit urakalle
   urakan-materiaalit (<! (t/hae-urakan-materiaalit (:id ur)))

   ;; ryhmitellään valitun sopimusnumeron materiaalit hoitokausittain
   sopimuksen-materiaalit-hoitokausittain
   :reaction (let [[sopimus-id _] @s/valittu-sopimusnumero]
               (log "URAKAN MATSKUI::: " @urakan-materiaalit)
               (log "SOPIMUS: " sopimus-id " MATSKUI:: " (filter #(= sopimus-id (:sopimus %))
                                 @urakan-materiaalit))
               (group-by (juxt :alkupvm :loppupvm)
                         (filter #(= sopimus-id (:sopimus %))
                                 @urakan-materiaalit)))
                   
   
   ;; valitaan materiaaleista vain valitun hoitokauden
   materiaalit :reaction (let [hk @s/valittu-hoitokausi]
                           (get @sopimuksen-materiaalit-hoitokausittain hk))
   
   uusi-id (atom 0)
   
   ;; luokitellaan yleiset materiaalit ja pohjavesialueiden materiaalit
   yleiset-materiaalit :reaction (into {}
                                       (comp (filter #(not (contains? % :pohjavesialue)))
                                             (map (juxt :id identity)))
                                       @materiaalit)
   pohjavesialue-materiaalit :reaction (into {}
                                             (comp (filter #(contains? % :pohjavesialue))
                                                   (map (juxt :id identity)))
                                             @materiaalit)

   yleiset-materiaalit-virheet nil
   yleiset-materiaalit-muokattu :reaction @yleiset-materiaalit

   pohjavesialue-materiaalit-virheet nil
   pohjavesialue-materiaalit-muokattu :reaction @pohjavesialue-materiaalit

   ;; kopioidaanko myös tuleville kausille (oletuksena false, vaarallinen)
   tuleville? false

   ;; jos tulevaisuudessa on dataa, joka poikkeaa tämän hoitokauden materiaaleista, varoita ylikirjoituksesta
   varoita-ylikirjoituksesta? 
   :reaction (let [kopioi? @tuleville?
                   hoitokausi @s/valittu-hoitokausi
                   hoitokausi-alku (tc/to-long (first hoitokausi))
                   vertailumuoto (fn [materiaalit]
                                   ;; vertailtaessa "samuutta" eri hoitokausien välillä poistetaan pvm:t ja id:t
                                   (into #{}
                                         (map #(dissoc % :alkupvm :loppupvm :id))
                                         materiaalit))

                   [tama-kausi & tulevat-kaudet] (into []
                                                       (comp (drop-while #(> hoitokausi-alku (ffirst %)))
                                                             (map second)
                                                             (map vertailumuoto))
                                                       (sort-by ffirst @sopimuksen-materiaalit-hoitokausittain))]
               
               ;;(doseq [tk tulevat-kaudet]
               ;;  (log "ONKO tämä kausi " tama-kausi " SAMA kuin tuleva " tk "? " (= tama-kausi tk)))
               (if-not kopioi?
                 false
                 (some #(not= tama-kausi %) tulevat-kaudet)))
       
   ]
  
  (let [materiaalikoodit @(t/hae-materiaalikoodit)
        muokattu? (or (not= @yleiset-materiaalit @yleiset-materiaalit-muokattu)
                      (not= @pohjavesialue-materiaalit @pohjavesialue-materiaalit-muokattu))
        virheita? (or (not (empty? @yleiset-materiaalit-virheet))
                      (not (empty? @pohjavesialue-materiaalit-virheet))) 
        voi-tallentaa? (and muokattu? (not virheita?))
        voi-muokata? false ;(istunto/kirjoitusoikeus-urakkaan? ur)
        ]
    
    [:div.materiaalit
     [yleiset-materiaalit-grid {:voi-muokata? voi-muokata?
                                :virheet yleiset-materiaalit-virheet}
      materiaalikoodit yleiset-materiaalit-muokattu]
     
     (when (= (:tyyppi ur) :hoito)
       [pohjavesialueiden-materiaalit-grid {:voi-muokata? voi-muokata?
                                            :virheet pohjavesialue-materiaalit-virheet}
        materiaalikoodit pohjavesialue-materiaalit-muokattu])

     (when voi-muokata?
       [raksiboksi "Tallenna tulevillekin hoitokausille" @tuleville?
        #(swap! tuleville? not)
        [:div.raksiboksin-info (ikonit/warning-sign) "Tulevilla hoitokausilla eri tietoa, jonka tallennus ylikirjoittaa."]
        (and @tuleville? @varoita-ylikirjoituksesta?)
        ])

     (when voi-muokata?
       [:div.toiminnot
        [:button.btn.btn-primary
         {:disabled (not voi-tallentaa?)
          :on-click #(do (.preventDefault %)
                         (go 
                           (let [rivit (concat (vals @yleiset-materiaalit-muokattu)
                                               (vals @pohjavesialue-materiaalit-muokattu))
                                 rivit (if @tuleville?
                                         (s/rivit-tulevillekin-kausille ur rivit @s/valittu-hoitokausi)
                                         rivit)
                                 uudet-materiaalit (<! (t/tallenna (:id ur)
                                                                   (first @s/valittu-sopimusnumero)
                                                                   rivit))]
                             (reset! urakan-materiaalit uudet-materiaalit))))}
         "Tallenna materiaalit"]])
     ])) 
