(ns harja.tiedot.urakka.toteumat.kokonaishintaiset-toteumat
  (:require [reagent.core :refer [atom]]
            [cljs.core.async :refer [<!]]
            [harja.loki :refer [log tarkkaile!]]
            [harja.tiedot.urakka :as urakka]
            [harja.tiedot.urakka.toteumat :as toteumat]
            [harja.tiedot.navigaatio :as nav])
  (:require-macros [harja.atom :refer [reaction<!]]
                   [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]))

;; todo: miten tämä asetetaan oikein
(def nakymassa? (atom true))
(defonce valittu-toteuma (atom nil))


(defonce haetut-toteumat
         (reaction<!
           [urakka-id (:id @nav/valittu-urakka)
            sopimus-id (first @urakka/valittu-sopimusnumero)
            hoitokausi @urakka/valittu-hoitokausi
            toimenpide @urakka/valittu-toimenpideinstanssi
            nakymassa? @nakymassa?]
           (when nakymassa?
             (toteumat/hae-urakan-toteumat urakka-id sopimus-id hoitokausi "kokonaishintainen"))))

(tarkkaile! "---- TOTEUMAT: " haetut-toteumat)
