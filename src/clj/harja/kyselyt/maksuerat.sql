-- name: hae-urakan-maksuerien-summat
-- Hakee id:n perusteella maksuerien lähettämiseen tarvittavat tiedot.
-- Jokaiselle toimenpideinstanssille palautetaan id sekä sarakkeet kaikille
-- eri maksuerätyypeille.
SELECT
  tpi_id,
  :urakka_id as urakka_id,
  SUM(kokonaishintaisten_summa)  AS kokonaishintainen,
  SUM(yksikkohintaisten_summa)   AS yksikkohintainen,
  SUM(sakot_summa)               AS sakko,
  SUM(akilliset_hoitotyot_summa) AS "akillinen-hoitotyo",
  SUM(lisatyot_summa)            AS lisatyo,
  SUM(bonukset_summa)            AS bonus,
  SUM(indeksit_summa)            AS indeksi,
  SUM(muut_summa)                AS muu
FROM (SELECT
        SUM((laskutusyhteenveto).kht_laskutettu +
            (laskutusyhteenveto).kht_laskutetaan)                 AS kokonaishintaisten_summa,
        SUM((laskutusyhteenveto).yht_laskutettu +
            (laskutusyhteenveto).yht_laskutetaan)                 AS yksikkohintaisten_summa,
        SUM((laskutusyhteenveto).sakot_laskutettu +
            (laskutusyhteenveto).sakot_laskutetaan +
            (laskutusyhteenveto).suolasakot_laskutettu +
            (laskutusyhteenveto).suolasakot_laskutetaan)          AS sakot_summa,
        SUM((laskutusyhteenveto).akilliset_hoitotyot_laskutettu +
            (laskutusyhteenveto).akilliset_hoitotyot_laskutetaan) AS akilliset_hoitotyot_summa,
        SUM((laskutusyhteenveto).muutostyot_laskutettu +
            (laskutusyhteenveto).muutostyot_laskutetaan)          AS lisatyot_summa,
        SUM((laskutusyhteenveto).bonukset_laskutettu +
            (laskutusyhteenveto).bonukset_laskutetaan)            AS bonukset_summa,
        SUM((laskutusyhteenveto).kaikki_laskutettu_ind_korotus +
            (laskutusyhteenveto).kaikki_laskutetaan_ind_korotus)  AS indeksit_summa,
        SUM((laskutusyhteenveto).erilliskustannukset_laskutettu +
            (laskutusyhteenveto).erilliskustannukset_laskutetaan +
            (laskutusyhteenveto).vahinkojen_korjaukset_laskutettu +
            (laskutusyhteenveto).vahinkojen_korjaukset_laskutetaan) AS muut_summa,
        (laskutusyhteenveto).tpi                                  AS tpi_id,
        lyht.alkupvm,
        lyht.loppupvm
      FROM (-- laskutusyhteenvedot menneiden hoitokausien viimeisille kuukausille
             SELECT
               hk.alkupvm,
               hk.loppupvm,
               laskutusyhteenveto(hk.alkupvm, hk.loppupvm,
                                  date_trunc('month', hk.loppupvm) :: DATE,
                                  (date_trunc('month', hk.loppupvm) + INTERVAL '1 month') :: DATE,
                                  :urakka_id :: INTEGER)
             FROM (SELECT *
                   FROM urakan_hoitokaudet(:urakka_id :: INTEGER)
                   WHERE loppupvm < now()) AS hk
             UNION ALL -- laskutusyhteenvedot menneiden hoitokausien viimeisille kuukausille
             SELECT
               hk.alkupvm,
               hk.loppupvm,
               laskutusyhteenveto(hk.alkupvm, hk.loppupvm,
                                  date_trunc('month', now()) :: DATE,
                                  (date_trunc('month', now()) + INTERVAL '1 month') :: DATE, :urakka_id :: INTEGER)
             FROM (SELECT *
                   FROM urakan_hoitokaudet(:urakka_id :: INTEGER)
                   WHERE alkupvm < now() AND loppupvm > now()) AS hk
           ) AS lyht
      GROUP BY tpi_id, lyht.alkupvm, lyht.loppupvm) AS maksuerat
GROUP BY tpi_id;

-- name: hae-urakan-maksuerat
-- Hakee id:n perusteella maksueran lähettämiseen tarvittavat tiedot.
-- Huom! Maksuerän summat haetaan hae-urakan-maksueratiedot kyselyllä, joka
-- muodostaa ne laskutusyhteenvetoa kutsumalla.
SELECT
  m.numero                 AS numero,
  m.tyyppi                 AS maksuera_tyyppi,
  m.nimi                   AS maksuera_nimi,
  m.tila                   AS maksuera_tila,
  m.lahetetty              AS maksuera_lahetetty,
  tpi.id                   AS toimenpideinstanssi_id,
  tpi.nimi                 AS toimenpideinstanssi_nimi,
  tpi.alkupvm              AS toimenpideinstanssi_alkupvm,
  tpi.loppupvm             AS toimenpideinstanssi_loppupvm,
  s.sampoid                AS sopimus_sampoid,
  k.tila                   AS kustannussuunnitelma_tila,
  k.lahetetty              AS kustannussuunnitelma_lahetetty,
  -- Tuotenumero
  (SELECT emo.tuotenumero
   FROM toimenpidekoodi emo
   WHERE emo.id = tpk.emo) AS tuotenumero,

  -- Kustannussuunnitelman summa
  COALESCE(CASE WHEN m.tyyppi = 'kokonaishintainen'
    THEN (SELECT SUM(kht.summa)
          FROM kokonaishintainen_tyo kht
          WHERE kht.toimenpideinstanssi = tpi.id)
           WHEN m.tyyppi = 'yksikkohintainen'
             THEN (SELECT SUM(yht.maara * yht.yksikkohinta)
                   FROM yksikkohintainen_tyo yht
                   WHERE
                     yht.urakka = :urakkaid AND
                     yht.tehtava IN (SELECT id
                                     FROM toimenpidekoodi
                                     WHERE emo = tpk.id))
           ELSE 1
           END, 0)         AS kustannussuunnitelma_summa
FROM maksuera m
  JOIN toimenpideinstanssi tpi ON tpi.id = m.toimenpideinstanssi
  JOIN urakka u ON u.id = tpi.urakka
  JOIN sopimus s ON s.urakka = u.id AND s.paasopimus IS NULL
  JOIN kustannussuunnitelma k ON m.numero = k.maksuera
  JOIN toimenpidekoodi tpk ON tpi.toimenpide = tpk.id
WHERE tpi.urakka = :urakkaid;

-- name: hae-lahetettava-maksuera
-- Hakee numeron perusteella maksueran lähettämiseen tarvittavat tiedot
SELECT
  m.numero                 AS numero,
  m.tyyppi                 AS maksuera_tyyppi,
  m.nimi                   AS maksuera_nimi,
  m.tila                   AS maksuera_tila,
  m.lahetetty              AS maksuera_lahetetty,
  tpi.id                   AS toimenpideinstanssi_id,
  tpi.nimi                 AS toimenpideinstanssi_nimi,
  tpi.alkupvm              AS toimenpideinstanssi_alkupvm,
  tpi.loppupvm             AS toimenpideinstanssi_loppupvm,
  tpi.tuotepolku           AS toimenpideinstanssi_tuotepolku,
  tpi.vastuuhenkilo_id     AS toimenpideinstanssi_vastuuhenkilo,
  tpi.talousosasto_id      AS toimenpideinstanssi_talousosasto,
  tpi.talousosastopolku    AS toimenpideinstanssi_talousosastopolku,
  tpi.sampoid              AS toimenpideinstanssi_sampoid,
  tpk.koodi                AS toimenpidekoodi,
  s.sampoid                AS sopimus_sampoid,
  u.sampoid                AS urakka_sampoid,
  k.tila                   AS kustannussuunnitelma_tila,
  k.lahetetty              AS kustannussuunnitelma_lahetetty,
  tpi.urakka               AS "urakka-id",

  -- Tuotenumero
  (SELECT emo.tuotenumero
   FROM toimenpidekoodi emo
   WHERE emo.id = tpk.emo) AS tuotenumero,

  -- Kustannussuunnitelman summa
  CASE WHEN m.tyyppi = 'kokonaishintainen'
       THEN (SELECT SUM(kht.summa)
               FROM kokonaishintainen_tyo kht
              WHERE kht.sopimus = s.id AND kht.toimenpideinstanssi = tpi.id)
       WHEN m.tyyppi = 'yksikkohintainen'
       THEN (SELECT SUM(yht.maara * yht.yksikkohinta)
               FROM yksikkohintainen_tyo yht
              WHERE yht.urakka = u.id AND yht.tehtava IN (SELECT id FROM toimenpidekoodi WHERE emo = tpk.id))
       ELSE 1
  END AS kustannussuunnitelma_summa
FROM maksuera m
  JOIN toimenpideinstanssi tpi ON tpi.id = m.toimenpideinstanssi
  JOIN urakka u ON u.id = tpi.urakka
  JOIN sopimus s ON s.urakka = u.id AND s.paasopimus IS NULL
  JOIN kustannussuunnitelma k ON m.numero = k.maksuera
  JOIN toimenpidekoodi tpk ON tpi.toimenpide = tpk.id
WHERE m.numero = :numero;

-- name: hae-maksueran-ja-kustannussuunnitelman-tilat
-- Hakee maksueran ja kustannussuunnitelman tilat
SELECT
  m.tila      AS maksuera_tila,
  m.lahetetty AS maksuera_lahetetty,
  k.tila      AS kustannussuunnitelma_tila,
  k.lahetetty AS kustannussuunnitelma_lahetetty
FROM maksuera m
  JOIN kustannussuunnitelma k ON k.maksuera = m.numero
WHERE m.numero = :numero;

-- name: hae-maksueranumero-lahetys-idlla
-- Hakee maksueranumeron lahetys-id:llä
SELECT numero
FROM maksuera
WHERE lahetysid = :lahetysid;

-- name: hae-likaiset-maksuerat
-- Hakee maksuerät, jotka täytyy lähettää
SELECT
  m.numero,
  u.id   AS urakkaid,
  tpi.id AS tpi_id
FROM maksuera m
  JOIN toimenpideinstanssi tpi ON m.toimenpideinstanssi = tpi.id
  JOIN urakka u ON tpi.urakka = u.id
WHERE m.likainen = TRUE;

-- name: lukitse-maksuera!
-- Lukitsee maksuerän lähetyksen ajaksi
UPDATE maksuera
SET lukko = :lukko, lukittu = current_timestamp
WHERE numero = :numero AND (lukko IS NULL OR
                            (EXTRACT(EPOCH FROM (current_timestamp - lukittu)) > 300));

-- name: merkitse-maksuera-odottamaan-vastausta!
-- Merkitsee maksuerän lähetetyksi, kirjaa lähetyksen id:n, avaa lukon ja merkitsee puhtaaksi
UPDATE maksuera
SET lahetysid = :lahetysid, lukko = NULL, tila = 'odottaa_vastausta', likainen = FALSE, lahetetty = CURRENT_TIMESTAMP
WHERE numero = :numero;

-- name: merkitse-maksuera-lahetetyksi!
-- Merkitsee maksuerän lähetetyksi
UPDATE maksuera
SET tila = 'lahetetty'
WHERE numero = :numero;

-- name: merkitse-maksueralle-lahetysvirhe!
-- Merkitsee maksuerän lähetetyksi, kirjaa lähetyksen id:n ja avaa lukon
UPDATE maksuera
SET tila = 'virhe', lukko = NULL, lukittu = NULL
WHERE numero = :numero;

-- name: merkitse-tyypin-maksuerat-likaisiksi!
-- Merkitsee kaikki annetun tyypin mukaiset maksuerät likaisi
UPDATE maksuera
SET likainen = TRUE
WHERE tyyppi = :tyyppi :: maksueratyyppi;

-- name: luo-maksuera<!
-- Luo uuden maksuerän.
INSERT INTO maksuera (toimenpideinstanssi, tyyppi, nimi, likainen, luotu)
VALUES (:toimenpideinstanssi, :tyyppi :: maksueratyyppi, :nimi, TRUE, current_timestamp);

-- name: onko-olemassa?
-- single?: true
SELECT exists(SELECT numero
              FROM maksuera
              WHERE numero = :numero :: BIGINT);

-- name: hae-maksueran-urakka
-- single?: true
SELECT u.id
FROM urakka u
  JOIN toimenpideinstanssi tpi ON u.id = tpi.urakka
  JOIN maksuera m ON tpi.id = m.toimenpideinstanssi
WHERE m.numero = :numero;
