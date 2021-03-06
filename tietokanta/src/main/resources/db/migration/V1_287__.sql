DROP FUNCTION laskutusyhteenveto(hk_alkupvm DATE, hk_loppupvm DATE,
aikavali_alkupvm DATE, aikavali_loppupvm DATE, ur INTEGER);
DROP TYPE laskutusyhteenveto_rivi;

CREATE TYPE laskutusyhteenveto_rivi
AS (nimi VARCHAR, tuotekoodi VARCHAR, tpi INTEGER, perusluku NUMERIC,
    kaikki_paitsi_kht_laskutettu_ind_korotus NUMERIC, kaikki_laskutettu_ind_korotus NUMERIC,
    kaikki_paitsi_kht_laskutetaan_ind_korotus NUMERIC, kaikki_laskutetaan_ind_korotus NUMERIC,
    kaikki_paitsi_kht_laskutettu NUMERIC, kaikki_laskutettu NUMERIC,
    kaikki_paitsi_kht_laskutetaan NUMERIC, kaikki_laskutetaan NUMERIC,
    kht_laskutettu  NUMERIC, kht_laskutettu_ind_korotettuna NUMERIC, kht_laskutettu_ind_korotus NUMERIC,
    kht_laskutetaan NUMERIC, kht_laskutetaan_ind_korotettuna NUMERIC, kht_laskutetaan_ind_korotus NUMERIC,
    yht_laskutettu  NUMERIC, yht_laskutettu_ind_korotettuna NUMERIC, yht_laskutettu_ind_korotus NUMERIC,
    yht_laskutetaan NUMERIC, yht_laskutetaan_ind_korotettuna NUMERIC, yht_laskutetaan_ind_korotus NUMERIC,
    sakot_laskutettu NUMERIC, sakot_laskutettu_ind_korotettuna NUMERIC, sakot_laskutettu_ind_korotus NUMERIC,
    sakot_laskutetaan NUMERIC, sakot_laskutetaan_ind_korotettuna NUMERIC, sakot_laskutetaan_ind_korotus NUMERIC,
    suolasakot_laskutettu NUMERIC, suolasakot_laskutettu_ind_korotettuna NUMERIC, suolasakot_laskutettu_ind_korotus NUMERIC,
    suolasakot_laskutetaan NUMERIC, suolasakot_laskutetaan_ind_korotettuna NUMERIC, suolasakot_laskutetaan_ind_korotus NUMERIC,
    muutostyot_laskutettu NUMERIC, muutostyot_laskutettu_ind_korotettuna NUMERIC, muutostyot_laskutettu_ind_korotus NUMERIC,
    muutostyot_laskutetaan NUMERIC, muutostyot_laskutetaan_ind_korotettuna NUMERIC, muutostyot_laskutetaan_ind_korotus NUMERIC,
    akilliset_hoitotyot_laskutettu NUMERIC, akilliset_hoitotyot_laskutettu_ind_korotettuna NUMERIC, akilliset_hoitotyot_laskutettu_ind_korotus NUMERIC,
    akilliset_hoitotyot_laskutetaan NUMERIC, akilliset_hoitotyot_laskutetaan_ind_korotettuna NUMERIC, akilliset_hoitotyot_laskutetaan_ind_korotus NUMERIC,
    erilliskustannukset_laskutettu NUMERIC, erilliskustannukset_laskutettu_ind_korotettuna NUMERIC, erilliskustannukset_laskutettu_ind_korotus NUMERIC,
    erilliskustannukset_laskutetaan NUMERIC, erilliskustannukset_laskutetaan_ind_korotettuna NUMERIC, erilliskustannukset_laskutetaan_ind_korotus NUMERIC,
    bonukset_laskutettu NUMERIC, bonukset_laskutettu_ind_korotettuna NUMERIC, bonukset_laskutettu_ind_korotus NUMERIC,
    bonukset_laskutetaan NuMERIC, bonukset_laskutetaan_ind_korotettuna NUMERIC, bonukset_laskutetaan_ind_korotus NUMERIC,
    suolasakko_kaytossa BOOLEAN, lampotila_puuttuu BOOLEAN);

CREATE OR REPLACE FUNCTION laskutusyhteenveto(
  hk_alkupvm DATE, hk_loppupvm DATE, aikavali_alkupvm DATE, aikavali_loppupvm DATE,
  ur         INTEGER)
  RETURNS SETOF laskutusyhteenveto_rivi AS $$
DECLARE
  t                                      RECORD;
  urakan_alkuvuosi INTEGER;
  ind VARCHAR; -- hoitourakassa käytettävä indeksi
  perusluku NUMERIC; -- urakan indeksilaskennan perusluku (urakkasopimusta edeltävän vuoden joulukuusta 3kk ka)

  kaikki_paitsi_kht_laskutettu_ind_korotus NUMERIC;
  kaikki_laskutettu_ind_korotus NUMERIC;
  kaikki_paitsi_kht_laskutetaan_ind_korotus NUMERIC;
  kaikki_laskutetaan_ind_korotus NUMERIC;

  kaikki_paitsi_kht_laskutettu NUMERIC;
  kaikki_laskutettu NUMERIC;
  kaikki_paitsi_kht_laskutetaan NUMERIC;
  kaikki_laskutetaan NUMERIC;

  kht_laskutettu                         NUMERIC;
  kht_laskutettu_ind_korotettuna         NUMERIC;
  kht_laskutettu_ind_korotus             NUMERIC;
  kht_laskutetaan                        NUMERIC;
  kht_laskutetaan_ind_korotettuna        NUMERIC;
  kht_laskutetaan_ind_korotus            NUMERIC;
  khti                                   RECORD;
  khti_laskutetaan                       RECORD;
  aikavalin_kht                          RECORD;
  kht_laskutetaan_rivi                   kuukauden_indeksikorotus_rivi;

  yht_laskutettu                         NUMERIC;
  yht_laskutettu_ind_korotettuna         NUMERIC;
  yht_laskutettu_ind_korotus             NUMERIC;
  yht_laskutettu_rivi                    kuukauden_indeksikorotus_rivi;
  yht_laskutetaan                        NUMERIC;
  yht_laskutetaan_ind_korotettuna        NUMERIC;
  yht_laskutetaan_ind_korotus            NUMERIC;
  yht_laskutetaan_rivi                   kuukauden_indeksikorotus_rivi;
  yhti                                   RECORD;
  yhti_laskutetaan                       RECORD;

  sakot_laskutettu                       NUMERIC;
  sakot_laskutettu_ind_korotettuna       NUMERIC;
  sakot_laskutettu_ind_korotus           NUMERIC;
  sakot_laskutettu_rivi                  RECORD;

  sakot_laskutetaan                      NUMERIC;
  sakot_laskutetaan_ind_korotettuna      NUMERIC;
  sakot_laskutetaan_ind_korotus          NUMERIC;
  sakot_laskutetaan_rivi                 RECORD;
  sanktiorivi                            RECORD;
  aikavalin_sanktio                      RECORD;

  suolasakot_laskutettu                  NUMERIC;
  suolasakot_laskutettu_ind_korotettuna  NUMERIC;
  suolasakot_laskutettu_ind_korotus      NUMERIC;
  suolasakot_laskutettu_rivi             RECORD;

  suolasakot_laskutetaan                 NUMERIC;
  suolasakot_laskutetaan_ind_korotettuna NUMERIC;
  suolasakot_laskutetaan_ind_korotus     NUMERIC;
  suolasakot_laskutetaan_rivi            RECORD;
  hoitokauden_suolasakko_rivi            RECORD;
  hoitokauden_laskettu_suolasakko_rivi            indeksitarkistettu_suolasakko_rivi;
  hoitokauden_laskettu_suolasakon_maara  NUMERIC;

  muutostyot_laskutettu                  NUMERIC;
  muutostyot_laskutettu_ind_korotettuna  NUMERIC;
  muutostyot_laskutettu_ind_korotus      NUMERIC;
  muutostyot_laskutettu_rivi             RECORD;
  muutostyot_laskutettu_paivanhinnalla             NUMERIC;

  muutostyot_laskutetaan                  NUMERIC;
  muutostyot_laskutetaan_ind_korotettuna  NUMERIC;
  muutostyot_laskutetaan_ind_korotus      NUMERIC;
  muutostyot_laskutetaan_rivi             RECORD;
  muutostyot_laskutetaan_paivanhinnalla             NUMERIC;
  mhti RECORD;
  mhti_aikavalilla RECORD;

  akilliset_hoitotyot_laskutettu                  NUMERIC;
  akilliset_hoitotyot_laskutettu_ind_korotettuna  NUMERIC;
  akilliset_hoitotyot_laskutettu_ind_korotus      NUMERIC;
  akilliset_hoitotyot_laskutettu_rivi             RECORD;
  akilliset_hoitotyot_laskutettu_paivanhinnalla             NUMERIC;

  akilliset_hoitotyot_laskutetaan                  NUMERIC;
  akilliset_hoitotyot_laskutetaan_ind_korotettuna  NUMERIC;
  akilliset_hoitotyot_laskutetaan_ind_korotus      NUMERIC;
  akilliset_hoitotyot_laskutetaan_rivi             RECORD;
  akilliset_hoitotyot_laskutetaan_paivanhinnalla             NUMERIC;
  akhti RECORD;
  akhti_aikavalilla RECORD;

  erilliskustannukset_laskutettu                  NUMERIC;
  erilliskustannukset_laskutettu_ind_korotettuna  NUMERIC;
  erilliskustannukset_laskutettu_ind_korotus      NUMERIC;
  erilliskustannukset_laskutettu_rivi                  RECORD;
  eki_laskutettu RECORD;
  erilliskustannukset_laskutetaan                  NUMERIC;
  erilliskustannukset_laskutetaan_ind_korotettuna  NUMERIC;
  erilliskustannukset_laskutetaan_ind_korotus      NUMERIC;
  erilliskustannukset_laskutetaan_rivi                  RECORD;
  eki_laskutetaan RECORD;

  bonukset_laskutettu NUMERIC;
  bonukset_laskutettu_ind_korotettuna NUMERIC;
  bonukset_laskutettu_ind_korotus NUMERIC;
  bonukset_laskutettu_rivi RECORD;
  bi_laskutettu RECORD;
  bonukset_laskutetaan NUMERIC;
  bonukset_laskutetaan_ind_korotettuna NUMERIC;
  bonukset_laskutetaan_ind_korotus NUMERIC;
  bonukset_laskutetaan_rivi RECORD;
  bi_laskutetaan RECORD;

  suolasakko_kaytossa BOOLEAN;
  lampotilat RECORD;
  lampotila_puuttuu BOOLEAN;

BEGIN
  -- Päätellään indeksilaskennan perustiedot
  SELECT INTO urakan_alkuvuosi EXTRACT(year from alkupvm) FROM urakka WHERE id = ur;

  IF urakan_alkuvuosi < 2017 THEN
    ind := 'MAKU 2005';
  ELSE
    ind := 'MAKU 2010';
  END IF;

  perusluku := hoitourakan_indeksilaskennan_perusluku(ur, ind);

  -- Loopataan urakan toimenpideinstanssien läpi
  FOR t IN SELECT
             tpk2.nimi AS nimi,
             tpk2.koodi AS tuotekoodi,
             tpi.id    AS tpi,
             tpk3.id   AS tpk3_id
           FROM toimenpideinstanssi tpi
             JOIN toimenpidekoodi tpk3 ON tpk3.id = tpi.toimenpide
             JOIN toimenpidekoodi tpk2 ON tpk3.emo = tpk2.id
           WHERE tpi.urakka = ur
  LOOP
    RAISE NOTICE '***** Laskutusyhteenvedon laskenta alkaa toimenpiteelle: % *****', t.nimi;
    kht_laskutettu := 0.0;
    kht_laskutettu_ind_korotettuna := 0.0;
    kht_laskutettu_ind_korotus := 0.0;

    -- Hoitokaudella ennen aikaväliä laskutetut kokonaishintaisten töiden kustannukset, myös indeksitarkistuksen kanssa
    FOR khti IN SELECT
                  (SELECT korotus
                   FROM laske_kuukauden_indeksikorotus(kht.vuosi, kht.kuukausi, ind,
                                                       kht.summa, perusluku)) AS ind,
                  (SELECT korotettuna
                   FROM laske_kuukauden_indeksikorotus(kht.vuosi, kht.kuukausi, ind,
                                                       kht.summa, perusluku)) AS kor,
                  kht.summa                                        AS kht_summa
                FROM kokonaishintainen_tyo kht
                WHERE toimenpideinstanssi = t.tpi
                      AND maksupvm >= hk_alkupvm
                      AND maksupvm <= hk_loppupvm
                      AND maksupvm < aikavali_alkupvm LOOP
      kht_laskutettu := kht_laskutettu + COALESCE(khti.kht_summa, 0.0);
      kht_laskutettu_ind_korotettuna := kht_laskutettu_ind_korotettuna + khti.kor;
      kht_laskutettu_ind_korotus := kht_laskutettu_ind_korotus + khti.ind;
    END LOOP;

    -- Kokonaishintaiset aikavälillä indeksikorotuksen kanssa
    kht_laskutetaan := 0.0;
    kht_laskutetaan_ind_korotettuna := 0.0;
    kht_laskutetaan_ind_korotus := 0.0;

    FOR khti_laskutetaan IN SELECT
                              (SELECT korotus
                               FROM laske_kuukauden_indeksikorotus(kht.vuosi, kht.kuukausi, ind,
                                                                   kht.summa, perusluku)) AS ind,
                              (SELECT korotettuna
                               FROM laske_kuukauden_indeksikorotus(kht.vuosi, kht.kuukausi, ind,
                                                                   kht.summa, perusluku)) AS kor,
                              kht.summa                                        AS kht_summa
                            FROM kokonaishintainen_tyo kht
                            WHERE toimenpideinstanssi = t.tpi
                                  AND maksupvm >= hk_alkupvm
                                  AND maksupvm <= hk_loppupvm
                                  AND maksupvm >= aikavali_alkupvm
                                  AND maksupvm <= aikavali_loppupvm LOOP
      kht_laskutetaan := kht_laskutetaan + COALESCE(khti_laskutetaan.kht_summa, 0.0);
      kht_laskutetaan_ind_korotettuna := kht_laskutetaan_ind_korotettuna + khti_laskutetaan.kor;
      kht_laskutetaan_ind_korotus := kht_laskutetaan_ind_korotus + khti_laskutetaan.ind;
    END LOOP;

    -- Hoitokaudella ennen aikaväliä laskutetut yksikköhintaisten töiden kustannukset, myös indeksitarkistuksen kanssa
    yht_laskutettu := 0.0;
    yht_laskutettu_ind_korotettuna := 0.0;
    yht_laskutettu_ind_korotus := 0.0;

    FOR yhti IN SELECT
                  SUM(tt.maara * yht.yksikkohinta) AS yht_summa,
                  tot.alkanut                      AS tot_alkanut,
                  tot.id,
                  tt.toimenpidekoodi
                FROM toteuma_tehtava tt
                  JOIN toteuma tot ON (tt.toteuma = tot.id AND tot.tyyppi = 'yksikkohintainen'::toteumatyyppi
                                       AND tot.poistettu IS NOT TRUE)
                  JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
                  JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                  JOIN yksikkohintainen_tyo yht ON (tt.toimenpidekoodi = yht.tehtava
                                                    AND yht.alkupvm <= tot.alkanut AND yht.loppupvm >= tot.alkanut
                                                    AND yht.yksikkohinta IS NOT NULL
                                                    AND tpk3.id = t.tpk3_id)
                WHERE yht.urakka = ur
                      AND tot.urakka = ur
                      AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
                      AND tot.alkanut < aikavali_alkupvm
                GROUP BY tot.alkanut, tt.toimenpidekoodi, tot.id
    LOOP
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM yhti.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM yhti.tot_alkanut) :: INTEGER),
                                          ind, yhti.yht_summa, perusluku)
      INTO yht_laskutettu_rivi;
      RAISE NOTICE 'yht_laskutettu_rivi: %', yht_laskutettu_rivi;
      yht_laskutettu :=  yht_laskutettu + yht_laskutettu_rivi.summa;
      yht_laskutettu_ind_korotettuna :=  yht_laskutettu_ind_korotettuna + yht_laskutettu_rivi.korotettuna;
      yht_laskutettu_ind_korotus :=  yht_laskutettu_ind_korotus + yht_laskutettu_rivi.korotus;


    END LOOP;


    -- Aikavälillä laskutettavat yksikköhintaisten töiden kustannukset indeksitarkistuksen kanssa
    yht_laskutetaan := 0.0;
    yht_laskutetaan_ind_korotettuna := 0.0;
    yht_laskutetaan_ind_korotus := 0.0;

    FOR yhti_laskutetaan IN
    SELECT
      tot.alkanut                      AS tot_alkanut,
      SUM(tt.maara * yht.yksikkohinta) AS yht_summa,
      tt.toimenpidekoodi,
      tot.id
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id AND tot.tyyppi = 'yksikkohintainen'::toteumatyyppi
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
      JOIN yksikkohintainen_tyo yht ON (tt.toimenpidekoodi = yht.tehtava
                                        AND yht.alkupvm <= tot.alkanut AND yht.loppupvm >= tot.alkanut
                                        AND yht.yksikkohinta IS NOT NULL
                                        AND tpk3.id = t.tpk3_id)
    WHERE yht.urakka = ur
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut >= aikavali_alkupvm AND tot.alkanut <= aikavali_loppupvm
    GROUP BY tot.alkanut, tt.toimenpidekoodi, tot.id
    LOOP
      RAISE NOTICE 'yhti_laskutetaan: %', yhti_laskutetaan;
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM yhti_laskutetaan.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM yhti_laskutetaan.tot_alkanut) :: INTEGER),
                                          ind, yhti_laskutetaan.yht_summa, perusluku)
      INTO yht_laskutetaan_rivi;
      RAISE NOTICE 'yht_laskutetaan_rivi: %', yht_laskutetaan_rivi;
      yht_laskutetaan := yht_laskutetaan + COALESCE(yht_laskutetaan_rivi.summa, 0.0);
      yht_laskutetaan_ind_korotettuna := yht_laskutetaan_ind_korotettuna + yht_laskutetaan_rivi.korotettuna;
      yht_laskutetaan_ind_korotus := yht_laskutetaan_ind_korotus + yht_laskutetaan_rivi.korotus;
    END LOOP;

    -- Hoitokaudella ennen aikaväliä laskutetut sanktiot
    sakot_laskutettu := 0.0;
    sakot_laskutettu_ind_korotettuna := 0.0;
    sakot_laskutettu_ind_korotus := 0.0;

    FOR sanktiorivi IN SELECT
                         maara,
                         perintapvm,
                         indeksi
                       FROM sanktio s
                       WHERE s.toimenpideinstanssi = t.tpi
                             AND s.perintapvm >= hk_alkupvm
                             AND s.perintapvm <= hk_loppupvm
                             AND s.perintapvm < aikavali_alkupvm
    LOOP

      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM sanktiorivi.perintapvm) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM sanktiorivi.perintapvm) :: INTEGER),
                                          sanktiorivi.indeksi, sanktiorivi.maara, perusluku)
      INTO sakot_laskutettu_rivi;
      sakot_laskutettu := sakot_laskutettu + COALESCE(sakot_laskutettu_rivi.summa, 0.0);
      sakot_laskutettu_ind_korotettuna := sakot_laskutettu_ind_korotettuna + sakot_laskutettu_rivi.korotettuna;
      sakot_laskutettu_ind_korotus := sakot_laskutettu_ind_korotus + sakot_laskutettu_rivi.korotus;


    END LOOP;


    -- Sanktiot aikavälillä
    sakot_laskutetaan := 0.0;
    sakot_laskutetaan_ind_korotettuna := 0.0;
    sakot_laskutetaan_ind_korotus := 0.0;

    FOR sanktiorivi IN SELECT
                         maara,
                         perintapvm,
                         indeksi
                       FROM sanktio s
                       WHERE s.toimenpideinstanssi = t.tpi
                             AND s.perintapvm >= hk_alkupvm
                             AND s.perintapvm <= hk_loppupvm
                             AND s.perintapvm >= aikavali_alkupvm
                             AND s.perintapvm <= aikavali_loppupvm
    LOOP

      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM sanktiorivi.perintapvm) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM sanktiorivi.perintapvm) :: INTEGER),
                                          sanktiorivi.indeksi, sanktiorivi.maara, perusluku)
      INTO sakot_laskutetaan_rivi;
      sakot_laskutetaan := sakot_laskutetaan + sakot_laskutetaan_rivi.summa;
      sakot_laskutetaan_ind_korotettuna := sakot_laskutetaan_ind_korotettuna + sakot_laskutetaan_rivi.korotettuna;
      sakot_laskutetaan_ind_korotus := sakot_laskutetaan_ind_korotus + sakot_laskutetaan_rivi.korotus;
    END LOOP;

    suolasakot_laskutettu := 0.0;
    suolasakot_laskutettu_ind_korotettuna := 0.0;
    suolasakot_laskutettu_ind_korotus := 0.0;
    suolasakot_laskutetaan := 0.0;
    suolasakot_laskutetaan_ind_korotettuna := 0.0;
    suolasakot_laskutetaan_ind_korotus := 0.0;

    SELECT *
    FROM suolasakko
    WHERE urakka = ur
          AND (SELECT EXTRACT(YEAR FROM hk_alkupvm) :: INTEGER) = hoitokauden_alkuvuosi
    INTO hoitokauden_suolasakko_rivi;

    hoitokauden_laskettu_suolasakon_maara = (SELECT hoitokauden_suolasakko(ur, hk_alkupvm, hk_loppupvm));


    -- Suolasakko lasketaan vain Talvihoito-toimenpiteelle (tuotekoodi '23100')
    IF t.tuotekoodi = '23100' THEN
      SELECT *
      FROM laske_suolasakon_indeksitarkistus(hoitokauden_suolasakko_rivi.hoitokauden_alkuvuosi,
                                             hoitokauden_suolasakko_rivi.indeksi,
                                             hoitokauden_laskettu_suolasakon_maara)
      INTO hoitokauden_laskettu_suolasakko_rivi;

      IF hoitokauden_suolasakko_rivi.maksukuukausi < (SELECT EXTRACT(MONTH FROM aikavali_alkupvm) :: INTEGER) THEN
        RAISE NOTICE 'Suolasakko on laskutettu aiemmin hoitokaudella kuukautena %', hoitokauden_suolasakko_rivi.maksukuukausi;
        suolasakot_laskutettu := hoitokauden_laskettu_suolasakko_rivi.summa;
        suolasakot_laskutettu_ind_korotettuna := hoitokauden_laskettu_suolasakko_rivi.korotettuna;
        suolasakot_laskutettu_ind_korotus := hoitokauden_laskettu_suolasakko_rivi.korotus;
      ELSIF hoitokauden_suolasakko_rivi.maksukuukausi = (SELECT EXTRACT(MONTH FROM aikavali_alkupvm) :: INTEGER) THEN
        RAISE NOTICE 'Suolasakko laskutetaan tässä kuussa %', hoitokauden_suolasakko_rivi.maksukuukausi;
        suolasakot_laskutetaan := hoitokauden_laskettu_suolasakko_rivi.summa;
        suolasakot_laskutetaan_ind_korotettuna := hoitokauden_laskettu_suolasakko_rivi.korotettuna;
        suolasakot_laskutetaan_ind_korotus := hoitokauden_laskettu_suolasakko_rivi.korotus;
      ELSE
        RAISE NOTICE 'Suolasakkoa ei vielä laskutettu, maksukuukauden arvo: %', hoitokauden_suolasakko_rivi.maksukuukausi;
      END IF;
    END IF;


    -- Muutos- ja lisätyöt hoitokaudella ennen aikaväliä
    muutostyot_laskutettu := 0.0;
    muutostyot_laskutettu_ind_korotettuna := 0.0;
    muutostyot_laskutettu_ind_korotus := 0.0;


    FOR mhti IN SELECT
                  SUM(tt.maara * mht.yksikkohinta) AS mht_summa,
                  tot.alkanut                      AS tot_alkanut
                FROM toteuma_tehtava tt
                  JOIN toteuma tot ON (tt.toteuma = tot.id
                                       AND tot.tyyppi IN ('muutostyo', 'lisatyo', 'vahinkojen-korjaukset')
                                       AND tot.poistettu IS NOT TRUE)
                  JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
                  JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                  JOIN muutoshintainen_tyo mht ON (tt.toimenpidekoodi = mht.tehtava
                                                   AND mht.alkupvm <= tot.alkanut AND mht.loppupvm >= tot.alkanut
                                                   AND mht.yksikkohinta IS NOT NULL
                                                   AND mht.poistettu IS NOT TRUE
                                                   AND tpk3.id = t.tpk3_id)
                WHERE mht.urakka = ur
                      AND tt.paivan_hinta IS NULL
                      AND tot.urakka = ur
                      AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
                      AND tot.alkanut < aikavali_alkupvm
                GROUP BY tot.alkanut
    LOOP
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM mhti.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM mhti.tot_alkanut) :: INTEGER),
                                          ind, mhti.mht_summa, perusluku)
      INTO muutostyot_laskutettu_rivi;
      muutostyot_laskutettu :=  muutostyot_laskutettu + COALESCE(muutostyot_laskutettu_rivi.summa, 0.0);
      muutostyot_laskutettu_ind_korotettuna :=  muutostyot_laskutettu_ind_korotettuna + muutostyot_laskutettu_rivi.korotettuna;
      muutostyot_laskutettu_ind_korotus :=  muutostyot_laskutettu_ind_korotus + muutostyot_laskutettu_rivi.korotus;
    END LOOP;

    -- Päivän hinnalla laskutetut muutostyöt hoitokaudella ennen aikaväliä
    muutostyot_laskutettu_paivanhinnalla := 0.0;
    SELECT SUM(tt.paivan_hinta)
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id
                           AND tot.tyyppi IN ('muutostyo', 'lisatyo', 'vahinkojen-korjaukset')
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                                   AND tpk3.id = t.tpk3_id
    WHERE tt.paivan_hinta IS NOT NULL
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut < aikavali_alkupvm

    INTO muutostyot_laskutettu_paivanhinnalla;
    muutostyot_laskutettu_paivanhinnalla := COALESCE(muutostyot_laskutettu_paivanhinnalla, 0.0);

    RAISE NOTICE 'Muutostöitä laskutettu päivän hinnalla %', muutostyot_laskutettu_paivanhinnalla;
    RAISE NOTICE 'Muutostöitä laskutettu listahinnalla %', muutostyot_laskutettu;

    -- Aikavälillä laskutettavat muutos- ja lisätyöt indeksitarkistuksen kanssa
    muutostyot_laskutetaan := 0.0;
    muutostyot_laskutetaan_ind_korotettuna := 0.0;
    muutostyot_laskutetaan_ind_korotus := 0.0;

    FOR mhti_aikavalilla IN
    SELECT
      tot.alkanut                      AS tot_alkanut,
      SUM(tt.maara * mht.yksikkohinta) AS mht_summa
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id AND tot.tyyppi IN ('muutostyo', 'lisatyo', 'vahinkojen-korjaukset')
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
      JOIN muutoshintainen_tyo mht ON (tt.toimenpidekoodi = mht.tehtava
                                       AND mht.alkupvm <= tot.alkanut AND mht.loppupvm >= tot.alkanut
                                       AND mht.yksikkohinta IS NOT NULL
                                       AND mht.poistettu IS NOT TRUE
                                       AND tpk3.id = t.tpk3_id)
    WHERE tt.paivan_hinta IS NULL
          AND mht.urakka = ur
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut >= aikavali_alkupvm AND tot.alkanut <= aikavali_loppupvm
    GROUP BY tot.alkanut
    LOOP

      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM mhti_aikavalilla.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM mhti_aikavalilla.tot_alkanut) :: INTEGER),
                                          ind, mhti_aikavalilla.mht_summa, perusluku)
      INTO muutostyot_laskutetaan_rivi;

      muutostyot_laskutetaan := muutostyot_laskutetaan + COALESCE(muutostyot_laskutetaan_rivi.summa, 0.0);
      muutostyot_laskutetaan_ind_korotettuna := muutostyot_laskutetaan_ind_korotettuna + muutostyot_laskutetaan_rivi.korotettuna;
      muutostyot_laskutetaan_ind_korotus := muutostyot_laskutetaan_ind_korotus + muutostyot_laskutetaan_rivi.korotus;
    END LOOP;

    -- Päivän hinnalla laskutetut muutostyöt aikavälillä
    muutostyot_laskutetaan_paivanhinnalla := 0.0;
    SELECT SUM(tt.paivan_hinta)
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id
                           AND tot.tyyppi IN ('muutostyo', 'lisatyo', 'vahinkojen-korjaukset')
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                                   AND tpk3.id = t.tpk3_id
    WHERE tt.paivan_hinta IS NOT NULL
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut >= aikavali_alkupvm AND tot.alkanut <= aikavali_loppupvm

    INTO muutostyot_laskutetaan_paivanhinnalla;
    muutostyot_laskutetaan_paivanhinnalla := COALESCE(muutostyot_laskutetaan_paivanhinnalla, 0.0);

    RAISE NOTICE 'Muutostöitä laskutetaan päivän hinnalla %', muutostyot_laskutetaan_paivanhinnalla;
    RAISE NOTICE 'Muutostöitä laskutetaan listahinnalla %', muutostyot_laskutetaan;

    -- Ynnätään muutostöiden molemmat hinnoittelutyypit
    muutostyot_laskutettu := muutostyot_laskutettu + muutostyot_laskutettu_paivanhinnalla;
    muutostyot_laskutettu_ind_korotettuna := muutostyot_laskutettu_ind_korotettuna + muutostyot_laskutettu_paivanhinnalla;
    muutostyot_laskutetaan := muutostyot_laskutetaan + muutostyot_laskutetaan_paivanhinnalla;
    muutostyot_laskutetaan_ind_korotettuna := muutostyot_laskutetaan_ind_korotettuna + muutostyot_laskutetaan_paivanhinnalla;


    -- Äkilliset hoitotyöt hoitokaudella ennen aikaväliä
    akilliset_hoitotyot_laskutettu := 0.0;
    akilliset_hoitotyot_laskutettu_ind_korotettuna := 0.0;
    akilliset_hoitotyot_laskutettu_ind_korotus := 0.0;


    FOR akhti IN SELECT
                   SUM(tt.maara * mht.yksikkohinta) AS mht_summa,
                   tot.alkanut                      AS tot_alkanut
                 FROM toteuma_tehtava tt
                   JOIN toteuma tot ON (tt.toteuma = tot.id
                                        AND tot.tyyppi IN ('akillinen-hoitotyo':: toteumatyyppi)
                                        AND tot.poistettu IS NOT TRUE)
                   JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
                   JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                   JOIN muutoshintainen_tyo mht ON (tt.toimenpidekoodi = mht.tehtava
                                                    AND mht.alkupvm <= tot.alkanut AND mht.loppupvm >= tot.alkanut
                                                    AND mht.yksikkohinta IS NOT NULL
                                                    AND mht.poistettu IS NOT TRUE
                                                    AND tpk3.id = t.tpk3_id)
                 WHERE mht.urakka = ur
                       AND tt.paivan_hinta IS NULL
                       AND tot.urakka = ur
                       AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
                       AND tot.alkanut < aikavali_alkupvm
                 GROUP BY tot.alkanut
    LOOP
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM akhti.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM akhti.tot_alkanut) :: INTEGER),
                                          ind, akhti.mht_summa, perusluku)
      INTO akilliset_hoitotyot_laskutettu_rivi;
      akilliset_hoitotyot_laskutettu :=  akilliset_hoitotyot_laskutettu + COALESCE(akilliset_hoitotyot_laskutettu_rivi.summa, 0.0);
      akilliset_hoitotyot_laskutettu_ind_korotettuna :=  akilliset_hoitotyot_laskutettu_ind_korotettuna + akilliset_hoitotyot_laskutettu_rivi.korotettuna;
      akilliset_hoitotyot_laskutettu_ind_korotus :=  akilliset_hoitotyot_laskutettu_ind_korotus + akilliset_hoitotyot_laskutettu_rivi.korotus;
    END LOOP;

    -- Päivän hinnalla laskutetut äkilliset hoitotyöt hoitokaudella ennen aikaväliä
    akilliset_hoitotyot_laskutettu_paivanhinnalla := 0.0;
    SELECT SUM(tt.paivan_hinta)
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id
                           AND tot.tyyppi IN ('akillinen-hoitotyo':: toteumatyyppi)
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                                   AND tpk3.id = t.tpk3_id
    WHERE tt.paivan_hinta IS NOT NULL
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut < aikavali_alkupvm

    INTO akilliset_hoitotyot_laskutettu_paivanhinnalla;
    akilliset_hoitotyot_laskutettu_paivanhinnalla := COALESCE(akilliset_hoitotyot_laskutettu_paivanhinnalla, 0.0);

    RAISE NOTICE 'Äkilliset hoitotyöt laskutettu päivän hinnalla %', akilliset_hoitotyot_laskutettu_paivanhinnalla;
    RAISE NOTICE 'Äkilliset hoitotyöt laskutettu listahinnalla %', akilliset_hoitotyot_laskutettu;

    -- Aikavälillä laskutettavat äkilliset hoitotyöt indeksitarkistuksen kanssa
    akilliset_hoitotyot_laskutetaan := 0.0;
    akilliset_hoitotyot_laskutetaan_ind_korotettuna := 0.0;
    akilliset_hoitotyot_laskutetaan_ind_korotus := 0.0;

    FOR akhti_aikavalilla IN
    SELECT
      tot.alkanut                      AS tot_alkanut,
      SUM(tt.maara * mht.yksikkohinta) AS mht_summa
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id AND tot.tyyppi IN ('akillinen-hoitotyo'::toteumatyyppi)
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
      JOIN muutoshintainen_tyo mht ON (tt.toimenpidekoodi = mht.tehtava
                                       AND mht.alkupvm <= tot.alkanut AND mht.loppupvm >= tot.alkanut
                                       AND mht.yksikkohinta IS NOT NULL
                                       AND mht.poistettu IS NOT TRUE
                                       AND tpk3.id = t.tpk3_id)
    WHERE tt.paivan_hinta IS NULL
          AND mht.urakka = ur
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut >= aikavali_alkupvm AND tot.alkanut <= aikavali_loppupvm
    GROUP BY tot.alkanut
    LOOP

      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM akhti_aikavalilla.tot_alkanut) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM akhti_aikavalilla.tot_alkanut) :: INTEGER),
                                          ind, akhti_aikavalilla.mht_summa, perusluku)
      INTO akilliset_hoitotyot_laskutetaan_rivi;

      akilliset_hoitotyot_laskutetaan := akilliset_hoitotyot_laskutetaan + COALESCE(akilliset_hoitotyot_laskutetaan_rivi.summa, 0.0);
      akilliset_hoitotyot_laskutetaan_ind_korotettuna := akilliset_hoitotyot_laskutetaan_ind_korotettuna + akilliset_hoitotyot_laskutetaan_rivi.korotettuna;
      akilliset_hoitotyot_laskutetaan_ind_korotus := akilliset_hoitotyot_laskutetaan_ind_korotus + akilliset_hoitotyot_laskutetaan_rivi.korotus;
    END LOOP;

    -- Päivän hinnalla laskutetut äkilliset hoitotyöt aikavälillä
    akilliset_hoitotyot_laskutetaan_paivanhinnalla := 0.0;
    SELECT SUM(tt.paivan_hinta)
    FROM toteuma_tehtava tt
      JOIN toteuma tot ON (tt.toteuma = tot.id
                           AND tot.tyyppi IN ('akillinen-hoitotyo':: toteumatyyppi)
                           AND tot.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk4 ON (tt.toimenpidekoodi = tpk4.id AND tpk4.poistettu IS NOT TRUE)
      JOIN toimenpidekoodi tpk3 ON tpk4.emo = tpk3.id
                                   AND tpk3.id = t.tpk3_id
    WHERE tt.paivan_hinta IS NOT NULL
          AND tot.urakka = ur
          AND tot.alkanut >= hk_alkupvm AND tot.alkanut <= hk_loppupvm
          AND tot.alkanut >= aikavali_alkupvm AND tot.alkanut <= aikavali_loppupvm

    INTO akilliset_hoitotyot_laskutetaan_paivanhinnalla;
    akilliset_hoitotyot_laskutetaan_paivanhinnalla := COALESCE(akilliset_hoitotyot_laskutetaan_paivanhinnalla, 0.0);

    RAISE NOTICE 'Äkilliset hoitotyöt laskutetaan päivän hinnalla %', akilliset_hoitotyot_laskutetaan_paivanhinnalla;
    RAISE NOTICE 'Äkilliset hoitotyöt laskutetaan listahinnalla %', akilliset_hoitotyot_laskutetaan;

    -- Ynnätään muutostöiden molemmat hinnoittelutyypit
    akilliset_hoitotyot_laskutettu := akilliset_hoitotyot_laskutettu + akilliset_hoitotyot_laskutettu_paivanhinnalla;
    akilliset_hoitotyot_laskutettu_ind_korotettuna := akilliset_hoitotyot_laskutettu_ind_korotettuna + akilliset_hoitotyot_laskutettu_paivanhinnalla;
    akilliset_hoitotyot_laskutetaan := akilliset_hoitotyot_laskutetaan + akilliset_hoitotyot_laskutetaan_paivanhinnalla;
    akilliset_hoitotyot_laskutetaan_ind_korotettuna := akilliset_hoitotyot_laskutetaan_ind_korotettuna + akilliset_hoitotyot_laskutetaan_paivanhinnalla;

    -- ERILLISKUSTANNUKSET (muut kuin asiakastyytyväisyysbonus)
    -- Hoitokaudella ennen aikaväliä laskutetut erilliskustannukset
    erilliskustannukset_laskutettu := 0.0;
    erilliskustannukset_laskutettu_ind_korotettuna := 0.0;
    erilliskustannukset_laskutettu_ind_korotus := 0.0;

    FOR eki_laskutettu
    IN SELECT
         ek.pvm,
         ek.rahasumma,
         ek.indeksin_nimi
       FROM erilliskustannus ek
       WHERE ek.tyyppi != 'asiakastyytyvaisyysbonus'
             AND ek.sopimus IN (SELECT id FROM sopimus WHERE urakka = ur)
             AND ek.toimenpideinstanssi = t.tpi
             AND ek.pvm >= hk_alkupvm AND ek.pvm <= hk_loppupvm
             AND ek.pvm < aikavali_alkupvm
             AND ek.poistettu IS NOT TRUE
    LOOP
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM eki_laskutettu.pvm) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM eki_laskutettu.pvm) :: INTEGER),
                                          eki_laskutettu.indeksin_nimi, eki_laskutettu.rahasumma, perusluku)
      INTO erilliskustannukset_laskutettu_rivi;
      erilliskustannukset_laskutettu :=  erilliskustannukset_laskutettu + COALESCE(erilliskustannukset_laskutettu_rivi.summa, 0.0);
      erilliskustannukset_laskutettu_ind_korotettuna :=  erilliskustannukset_laskutettu_ind_korotettuna + erilliskustannukset_laskutettu_rivi.korotettuna;
      erilliskustannukset_laskutettu_ind_korotus :=  erilliskustannukset_laskutettu_ind_korotus + erilliskustannukset_laskutettu_rivi.korotus;
    END LOOP;
    RAISE NOTICE 'Erilliskustannuksia laskutettu: %', erilliskustannukset_laskutettu;

    -- Erilliskustannukset aikavälillä
    erilliskustannukset_laskutetaan := 0.0;
    erilliskustannukset_laskutetaan_ind_korotettuna := 0.0;
    erilliskustannukset_laskutetaan_ind_korotus := 0.0;
    FOR eki_laskutetaan
    IN SELECT
         ek.pvm,
         ek.rahasumma,
         ek.indeksin_nimi
       FROM erilliskustannus ek
       WHERE ek.tyyppi != 'asiakastyytyvaisyysbonus'
             AND ek.sopimus IN (SELECT id FROM sopimus WHERE urakka = ur)
             AND ek.toimenpideinstanssi = t.tpi
             AND ek.pvm >= hk_alkupvm AND ek.pvm <= hk_loppupvm
             AND ek.pvm >= aikavali_alkupvm AND ek.pvm <= aikavali_loppupvm
             AND ek.poistettu IS NOT TRUE
    LOOP
      SELECT *
      FROM laske_kuukauden_indeksikorotus((SELECT EXTRACT(YEAR FROM eki_laskutetaan.pvm) :: INTEGER),
                                          (SELECT EXTRACT(MONTH FROM eki_laskutetaan.pvm) :: INTEGER),
                                          eki_laskutetaan.indeksin_nimi, eki_laskutetaan.rahasumma, perusluku)
      INTO erilliskustannukset_laskutetaan_rivi;
      erilliskustannukset_laskutetaan :=  erilliskustannukset_laskutetaan + COALESCE(erilliskustannukset_laskutetaan_rivi.summa, 0.0);
      erilliskustannukset_laskutetaan_ind_korotettuna :=  erilliskustannukset_laskutetaan_ind_korotettuna + erilliskustannukset_laskutetaan_rivi.korotettuna;
      erilliskustannukset_laskutetaan_ind_korotus :=  erilliskustannukset_laskutetaan_ind_korotus + erilliskustannukset_laskutetaan_rivi.korotus;
    END LOOP;
    RAISE NOTICE 'Erilliskustannuksia laskutetaan: %', erilliskustannukset_laskutetaan;

    -- BONUKSET
    -- Hoitokaudella ennen aikaväliä laskutetut bonukset
    bonukset_laskutettu := 0.0;
    bonukset_laskutettu_ind_korotettuna := 0.0;
    bonukset_laskutettu_ind_korotus := 0.0;

    FOR bi_laskutettu
    IN SELECT
         b.pvm,
         b.rahasumma,
         b.indeksin_nimi
       FROM erilliskustannus b
       WHERE b.tyyppi = 'asiakastyytyvaisyysbonus'
             AND b.sopimus IN (SELECT id FROM sopimus WHERE urakka = ur)
             AND b.toimenpideinstanssi = t.tpi
             AND b.pvm >= hk_alkupvm AND b.pvm <= hk_loppupvm
             AND b.pvm < aikavali_alkupvm
             AND b.poistettu IS NOT TRUE
    LOOP
      SELECT *
      FROM laske_hoitokauden_asiakastyytyvaisyysbonus(ur, bi_laskutettu.pvm, ind, bi_laskutettu.rahasumma)
      INTO bonukset_laskutettu_rivi;
      bonukset_laskutettu :=  bonukset_laskutettu + COALESCE(bonukset_laskutettu_rivi.summa, 0.0);
      bonukset_laskutettu_ind_korotettuna :=  bonukset_laskutettu_ind_korotettuna + bonukset_laskutettu_rivi.korotettuna;
      bonukset_laskutettu_ind_korotus :=  bonukset_laskutettu_ind_korotus + bonukset_laskutettu_rivi.korotus;
    END LOOP;
    RAISE NOTICE 'Bonuksia laskutettu: %', bonukset_laskutettu;

    -- Bonukset aikavälillä
    bonukset_laskutetaan := 0.0;
    bonukset_laskutetaan_ind_korotettuna := 0.0;
    bonukset_laskutetaan_ind_korotus := 0.0;
    FOR bi_laskutetaan
    IN SELECT
         b.pvm,
         b.rahasumma,
         b.indeksin_nimi
       FROM erilliskustannus b
       WHERE b.tyyppi = 'asiakastyytyvaisyysbonus'
             AND b.sopimus IN (SELECT id FROM sopimus WHERE urakka = ur)
             AND b.toimenpideinstanssi = t.tpi
             AND b.pvm >= hk_alkupvm AND b.pvm <= hk_loppupvm
             AND b.pvm >= aikavali_alkupvm AND b.pvm <= aikavali_loppupvm
             AND b.poistettu IS NOT TRUE
    LOOP
      SELECT *
      FROM laske_hoitokauden_asiakastyytyvaisyysbonus(ur, bi_laskutetaan.pvm, ind, bi_laskutetaan.rahasumma)
      INTO bonukset_laskutetaan_rivi;
      bonukset_laskutetaan :=  bonukset_laskutetaan + COALESCE(bonukset_laskutetaan_rivi.summa, 0.0);
      bonukset_laskutetaan_ind_korotettuna :=  bonukset_laskutetaan_ind_korotettuna + bonukset_laskutetaan_rivi.korotettuna;
      bonukset_laskutetaan_ind_korotus :=  bonukset_laskutetaan_ind_korotus + bonukset_laskutetaan_rivi.korotus;
    END LOOP;
    RAISE NOTICE 'Bonuksia laskutetaan: %', bonukset_laskutetaan;


    -- Onko suolasakko käytössä urakassa
    IF (select count(*) FROM suolasakko WHERE urakka = ur
                                              AND kaytossa
                                              AND hoitokauden_alkuvuosi = (SELECT EXTRACT(YEAR FROM hk_alkupvm) :: INTEGER)) > 0
    THEN suolasakko_kaytossa = TRUE;
    ELSE suolasakko_kaytossa = FALSE;
    END IF;

    -- Ovatko suolasakon tarvitsemat lämpötilat kannassa
    SELECT * INTO lampotilat FROM lampotilat
    WHERE urakka = ur AND alkupvm = hk_alkupvm AND loppupvm = hk_loppupvm;

    IF (lampotilat IS NULL OR lampotilat.keskilampotila IS NULL OR lampotilat.pitka_keskilampotila IS NULL)
    THEN
      RAISE NOTICE 'Urakalle % ei ole lämpötiloja hoitokaudelle % - %', ur, hk_alkupvm, hk_loppupvm;
      RAISE NOTICE 'Keskilämpötila hoitokaudella %, pitkän ajan keskilämpötila %', lampotilat.keskilampotila, lampotilat.pitka_keskilampotila;
      lampotila_puuttuu = TRUE;
    ELSE
      lampotila_puuttuu = FALSE;
    END IF;


    -- Indeksisummat
    kaikki_paitsi_kht_laskutettu_ind_korotus := 0.0;
    kaikki_laskutettu_ind_korotus := 0.0;
    kaikki_paitsi_kht_laskutetaan_ind_korotus := 0.0;
    kaikki_laskutetaan_ind_korotus := 0.0;

    kaikki_paitsi_kht_laskutettu_ind_korotus := yht_laskutettu_ind_korotus + sakot_laskutettu_ind_korotus + COALESCE(suolasakot_laskutettu_ind_korotus, 0.0) +
                                                muutostyot_laskutettu_ind_korotus + akilliset_hoitotyot_laskutettu_ind_korotus + erilliskustannukset_laskutettu_ind_korotus + bonukset_laskutettu_ind_korotus;
    kaikki_laskutettu_ind_korotus := kaikki_paitsi_kht_laskutettu_ind_korotus + kht_laskutettu_ind_korotus;

    kaikki_paitsi_kht_laskutetaan_ind_korotus := yht_laskutetaan_ind_korotus + sakot_laskutetaan_ind_korotus + COALESCE(suolasakot_laskutetaan_ind_korotus, 0.0) +
                                                 muutostyot_laskutetaan_ind_korotus + akilliset_hoitotyot_laskutetaan_ind_korotus  + erilliskustannukset_laskutetaan_ind_korotus + bonukset_laskutetaan_ind_korotus;
    kaikki_laskutetaan_ind_korotus := kaikki_paitsi_kht_laskutetaan_ind_korotus + kht_laskutetaan_ind_korotus;


    -- Kustannusten kokonaissummat
    kaikki_paitsi_kht_laskutettu := 0.0;
    kaikki_laskutettu := 0.0;
    kaikki_paitsi_kht_laskutetaan := 0.0;
    kaikki_laskutetaan := 0.0;

    kaikki_paitsi_kht_laskutettu := yht_laskutettu_ind_korotettuna + sakot_laskutettu_ind_korotettuna +
                                    COALESCE(suolasakot_laskutettu_ind_korotettuna, 0.0) + muutostyot_laskutettu_ind_korotettuna +
                                    akilliset_hoitotyot_laskutettu_ind_korotettuna + erilliskustannukset_laskutettu_ind_korotettuna +
                                    bonukset_laskutettu_ind_korotettuna
                                    --Aurasta: myös kok.hint. töiden indeksitarkistus laskettava tähän mukaan
                                    + kht_laskutettu_ind_korotus;

    kaikki_laskutettu := kaikki_paitsi_kht_laskutettu + kht_laskutettu;

    kaikki_paitsi_kht_laskutetaan := yht_laskutetaan_ind_korotettuna + sakot_laskutetaan_ind_korotettuna +
                                     COALESCE(suolasakot_laskutetaan_ind_korotettuna, 0.0) + muutostyot_laskutetaan_ind_korotettuna +
                                     akilliset_hoitotyot_laskutetaan_ind_korotettuna + erilliskustannukset_laskutetaan_ind_korotettuna +
                                     bonukset_laskutetaan_ind_korotettuna
                                     --Aurasta: myös kok.hint. töiden indeksitarkistus laskettava tähän mukaan
                                     + kht_laskutetaan_ind_korotus;
    kaikki_laskutetaan := kaikki_paitsi_kht_laskutetaan + kht_laskutetaan;

    RAISE NOTICE '
    Yhteenveto:';
    RAISE NOTICE 'LASKUTETTU ENNEN AIKAVÄLIÄ % - %:', aikavali_alkupvm, aikavali_loppupvm;
    RAISE NOTICE 'kht_laskutettu: %', kht_laskutettu;
    RAISE NOTICE 'kht_laskutettu_ind_korotettuna: %', kht_laskutettu_ind_korotettuna;
    RAISE NOTICE 'yht_laskutettu: %', yht_laskutettu;
    RAISE NOTICE 'yht_laskutettu_ind_korotettuna: %', yht_laskutettu_ind_korotettuna;
    RAISE NOTICE 'sakot_laskutettu: %', sakot_laskutettu;
    RAISE NOTICE 'sakot_laskutettu_ind_korotettuna: %', sakot_laskutettu_ind_korotettuna;
    RAISE NOTICE 'suolasakot_laskutettu: %', suolasakot_laskutettu;
    RAISE NOTICE 'suolasakot_laskutettu_ind_korotettuna: %', suolasakot_laskutettu_ind_korotettuna;
    RAISE NOTICE 'muutostyot_laskutettu: %', muutostyot_laskutettu;
    RAISE NOTICE 'muutostyot_laskutettu_ind_korotettuna: %', muutostyot_laskutettu_ind_korotettuna;
    RAISE NOTICE 'erilliskustannukset_laskutettu: %', erilliskustannukset_laskutettu;
    RAISE NOTICE 'erilliskustannukset_laskutettu_ind_korotettuna: %', erilliskustannukset_laskutettu_ind_korotettuna;
    RAISE NOTICE 'bonukset_laskutettu: %', bonukset_laskutettu;
    RAISE NOTICE 'bonukset_laskutettu_ind_korotettuna: %', bonukset_laskutettu_ind_korotettuna;

    RAISE NOTICE '
    LASKUTETAAN AIKAVÄLILLÄ % - %:', aikavali_alkupvm, aikavali_loppupvm;
    RAISE NOTICE 'kht_laskutetaan: %', kht_laskutetaan;
    RAISE NOTICE 'kht_laskutetaan_ind_korotettuna: %', kht_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'yht_laskutetaan: %', yht_laskutetaan;
    RAISE NOTICE 'yht_laskutetaan_ind_korotettuna: %', yht_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'sakot_laskutetaan: %', sakot_laskutetaan;
    RAISE NOTICE 'sakot_laskutetaan_ind_korotettuna: %', sakot_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'suolasakot_laskutetaan: %', suolasakot_laskutetaan;
    RAISE NOTICE 'suolasakot_laskutetaan_ind_korotettuna: %', suolasakot_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'muutostyot_laskutetaan: %', muutostyot_laskutetaan;
    RAISE NOTICE 'muutostyot_laskutetaan_ind_korotettuna: %', muutostyot_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'akilliset_hoitotyot_laskutetaan: %', akilliset_hoitotyot_laskutetaan;
    RAISE NOTICE 'akilliset_hoitotyot_laskutetaan_ind_korotettuna: %', akilliset_hoitotyot_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'erilliskustannukset_laskutetaan: %', erilliskustannukset_laskutetaan;
    RAISE NOTICE 'erilliskustannukset_laskutetaan_ind_korotettuna: %', erilliskustannukset_laskutetaan_ind_korotettuna;
    RAISE NOTICE 'bonukset_laskutetaan: %', bonukset_laskutetaan;
    RAISE NOTICE 'bonukset_laskutetaan_ind_korotettuna: %', bonukset_laskutetaan_ind_korotettuna;

    RAISE NOTICE 'kaikki_paitsi_kht_laskutettu_ind_korotus: %', kaikki_paitsi_kht_laskutettu_ind_korotus;
    RAISE NOTICE 'kaikki_laskutettu_ind_korotus: %', kaikki_laskutettu_ind_korotus;
    RAISE NOTICE 'kaikki_paitsi_kht_laskutetaan_ind_korotus: %', kaikki_paitsi_kht_laskutetaan_ind_korotus;
    RAISE NOTICE 'kaikki_laskutetaan_ind_korotus: %', kaikki_laskutetaan_ind_korotus;
    RAISE NOTICE 'kaikki_paitsi_kht_laskutettu: %', kaikki_paitsi_kht_laskutettu;
    RAISE NOTICE 'kaikki_laskutettu: %', kaikki_laskutettu;
    RAISE NOTICE 'kaikki_paitsi_kht_laskutetaan: %', kaikki_paitsi_kht_laskutetaan;
    RAISE NOTICE 'kaikki_laskutetaan: %', kaikki_laskutetaan;

    RAISE NOTICE 'suolasakko_kaytossa: %', suolasakko_kaytossa;
    RAISE NOTICE 'lampotila_puuttuu: %', lampotila_puuttuu;


    RAISE NOTICE '***** Käsitelly loppui toimenpiteelle: %  *****

    ', t.nimi;


    RETURN NEXT (t.nimi, t.tuotekoodi, t.tpi, perusluku,
                         kaikki_paitsi_kht_laskutettu_ind_korotus, kaikki_laskutettu_ind_korotus,
                         kaikki_paitsi_kht_laskutetaan_ind_korotus, kaikki_laskutetaan_ind_korotus,
                         kaikki_paitsi_kht_laskutettu, kaikki_laskutettu,
                         kaikki_paitsi_kht_laskutetaan, kaikki_laskutetaan,
                                                        kht_laskutettu, kht_laskutettu_ind_korotettuna, kht_laskutettu_ind_korotus,
                                                        kht_laskutetaan, kht_laskutetaan_ind_korotettuna, kht_laskutetaan_ind_korotus,
                                                        yht_laskutettu, yht_laskutettu_ind_korotettuna, yht_laskutettu_ind_korotus,
      yht_laskutetaan, yht_laskutetaan_ind_korotettuna, yht_laskutetaan_ind_korotus,
      sakot_laskutettu, sakot_laskutettu_ind_korotettuna, sakot_laskutettu_ind_korotus,
      sakot_laskutetaan, sakot_laskutetaan_ind_korotettuna, sakot_laskutetaan_ind_korotus,
      suolasakot_laskutettu, suolasakot_laskutettu_ind_korotettuna, suolasakot_laskutettu_ind_korotus,
                             suolasakot_laskutetaan, suolasakot_laskutetaan_ind_korotettuna, suolasakot_laskutetaan_ind_korotus,
                             muutostyot_laskutettu, muutostyot_laskutettu_ind_korotettuna, muutostyot_laskutettu_ind_korotus,
                             muutostyot_laskutetaan, muutostyot_laskutetaan_ind_korotettuna, muutostyot_laskutetaan_ind_korotus,
                                                                                             akilliset_hoitotyot_laskutettu, akilliset_hoitotyot_laskutettu_ind_korotettuna, akilliset_hoitotyot_laskutettu_ind_korotus,
                                                                                             akilliset_hoitotyot_laskutetaan, akilliset_hoitotyot_laskutetaan_ind_korotettuna, akilliset_hoitotyot_laskutetaan_ind_korotus,
                                                                                             erilliskustannukset_laskutettu, erilliskustannukset_laskutettu_ind_korotettuna, erilliskustannukset_laskutettu_ind_korotus,
                 erilliskustannukset_laskutetaan, erilliskustannukset_laskutetaan_ind_korotettuna, erilliskustannukset_laskutetaan_ind_korotus,
                 bonukset_laskutettu, bonukset_laskutettu_ind_korotettuna, bonukset_laskutettu_ind_korotus,
                 bonukset_laskutetaan, bonukset_laskutetaan_ind_korotettuna, bonukset_laskutetaan_ind_korotus,
                 suolasakko_kaytossa, lampotila_puuttuu
    );


  END LOOP;

END;
$$ LANGUAGE plpgsql;
