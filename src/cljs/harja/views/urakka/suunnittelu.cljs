(ns harja.views.urakka.suunnittelu
  "Päätason sivu Hallinta, josta kaikkeen ylläpitötyöhön pääsee käsiksi."
  (:require [reagent.core :refer [atom] :as r]
            [bootstrap :as bs]
            
            [harja.tiedot.urakka.suunnittelu :as s]
            [harja.views.urakka.yksikkohintaiset-tyot :as yksikkohintaiset-tyot]
            [harja.pvm :as pvm]
            [harja.loki :refer [log]]
            [harja.ui.yleiset :refer [ajax-loader kuuntelija linkki sisalla? alasveto-ei-loydoksia alasvetovalinta radiovalinta]])
  
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [reagent.ratom :refer [reaction run!]]
                   [harja.ui.yleiset :refer [deftk]]))


(def valittu-valilehti "Valittu välilehti" (atom 0))


(defn suunnittelu [ur]
  ;; suunnittelu-välilehtien yhteiset valinnat hoitokaudelle ja sopimusnumerolle
  (let [urakan-hoitokaudet (atom (s/hoitokaudet ur))]
    (s/valitse-sopimusnumero! (first (:sopimukset ur)))
    (s/valitse-hoitokausi! (first @urakan-hoitokaudet))
    
    (r/create-class
      {:component-will-receive-props
       (fn [this [_ ur]]
         (reset! urakan-hoitokaudet (s/hoitokaudet ur))
         (s/valitse-sopimusnumero! (first (:sopimukset ur)))
         (s/valitse-hoitokausi! (first @urakan-hoitokaudet)))
       
       
       :reagent-render 
       (fn [ur]
         [:span
          [:div.alasvetovalikot
           [:div.label-ja-alasveto 
            [:span.alasvedon-otsikko "Sopimusnumero"]
            [alasvetovalinta {:valinta @s/valittu-sopimusnumero
                              :format-fn second
                              :valitse-fn s/valitse-sopimusnumero!
                              :class "alasveto"
                              }
             (:sopimukset ur)
             ]]
           [:div.label-ja-alasveto
            [:span.alasvedon-otsikko "Hoitokausi"]
            [alasvetovalinta {:valinta @s/valittu-hoitokausi
                              ;;\u2014 on väliviivan unikoodi
                              :format-fn #(if % (str (pvm/pvm (:alkupvm %)) 
                                                     " \u2014 " (pvm/pvm (:loppupvm %))) "Valitse")
                              :valitse-fn s/valitse-hoitokausi!
                              :class "alasveto"
                              }
             @urakan-hoitokaudet
             ]]]
          
          ;; suunnittelun välilehdet
          [bs/tabs {:style :pills :active valittu-valilehti}
           
           "Yksikköhintaiset työt"
           ^{:key "yksikkohintaiset-tyot"}
           [yksikkohintaiset-tyot/yksikkohintaiset-tyot ur]
           
           "Kokonaishintaiset työt"
           [:div "Kokonaishintaiset työt"]
           ;;^{:key "kokonaishintaiset-tyot"}
           ;;[yht/yksikkohintaiset-tyot]
           
           "Materiaalit"
           [:div "Materiaalit"]
           ;;^{:key "materiaalit"}
           ]])
       
       })))


