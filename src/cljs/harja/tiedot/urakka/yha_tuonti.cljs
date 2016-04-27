(ns harja.tiedot.urakka.yhatuonti
  (:require [reagent.core :refer [atom] :as reagent]
            [harja.ui.yleiset :refer [ajax-loader vihje]]
            [harja.loki :refer [log logt tarkkaile!]]
            [cljs.core.async :refer [<! >! chan timeout]]
            [harja.ui.lomake :refer [lomake]]
            [harja.ui.grid :refer [grid]]
            [harja.asiakas.kommunikaatio :as k])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [harja.atom :refer [reaction<!]]
                   [reagent.ratom :refer [reaction]]))

(defn hae-yha-urakat [{:keys [nimi tunniste vuosi] :as hakuparametrit}]
  (log "[YHA] Hae YHA-urakat...")
  #_(k/post! :hae-yha-urakat {:nimi nimi
                            :tunniste tunniste
                            :vuosi vuosi})
  ;; FIXME Palauta toistaiseksi vain testidata
  (go (do
        [{:tunnus "YHA1" :nimi "YHA-urakka" :elyt "Pohjois-Pohjanmaa" :vuodet 2010}])))

(def hakulomake-data (atom nil))

(tarkkaile! "[YHA] Hakutiedot " hakulomake-data)

(def hakutulokset-data
  (reaction<! [hakulomake-data @hakulomake-data]
              {:nil-kun-haku-kaynnissa? true
               :odota 500}
              (hae-yha-urakat hakulomake-data)))

(defn- sido-yha-urakka-harja-urakkaan [harja-urakka-id yha-tiedot]
  (log "[YHA] Sidotaan YHA-urakka Harja-urakkaan...")
  (k/post! :sido-yha-urakka-harja-urakkaan {:harja-urakka-id harja-urakka-id
                                            :yha-tiedot yha-tiedot}))