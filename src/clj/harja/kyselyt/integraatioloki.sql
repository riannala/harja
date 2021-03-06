-- name: luo-integraatiotapahtuma<!
-- Luo uuden hankkeen.
INSERT INTO integraatiotapahtuma (integraatio, alkanut, ulkoinenid)
VALUES ((SELECT id
         FROM integraatio
         WHERE jarjestelma = :jarjestelma AND nimi = :nimi),
        current_timestamp,
        :ulkoinenid);

-- name: merkitse-integraatiotapahtuma-paattyneeksi!
-- Paivittaa hankkeen Samposta saaduilla tiedoilla
UPDATE integraatiotapahtuma
SET paattynyt = current_timestamp, onnistunut = :onnistunut, lisatietoja = :lisatietoja
WHERE id = :id;

-- name: merkitse-integraatiotapahtuma-paattyneeksi-ulkoisella-idlla<!
-- Paivittaa hankkeen Samposta saaduilla tiedoilla
UPDATE integraatiotapahtuma
SET paattynyt = current_timestamp, onnistunut = :onnistunut, lisatietoja = :lisatietoja
WHERE ulkoinenid = :ulkoinen_id;

-- name: aseta-ulkoinen-id-integraatiotapahtumalle!
-- Asettaa ulkoisen id:n integraatiotapahtumalle
UPDATE integraatiotapahtuma
SET ulkoinenid = :ulkoinenid
WHERE id = :id;

-- name: luo-integraatioviesti<!
-- Luo uuden integraatioviestin
INSERT INTO integraatioviesti (integraatiotapahtuma, osoite, suunta, sisaltotyyppi, siirtotyyppi,
                               sisalto, otsikko, parametrit, kasitteleva_palvelin)
VALUES
  (:integraatiotapahtuma, :osoite, :suunta :: integraatiosuunta, :sisaltotyyppi, :siirtotyyppi,
   :sisalto, :otsikko, :parametrit, :kasittelevapalvelin);

-- name: hae-tapahtumaid-ulkoisella-idlla
-- Hakee tapahtuma id:n ulkoisella id:llä ja integraatiolla
SELECT id
FROM integraatiotapahtuma
WHERE ulkoinenid = :ulkoinenid AND
      integraatio = (SELECT id
                     FROM integraatio
                     WHERE jarjestelma = :jarjestelma AND nimi = :nimi);

-- name: poista-ennen-paivamaaraa-kirjatut-tapahtumat!
-- Poistaa ennen annettua päivämäärää kirjatut integraatiotapahtumat viesteineen
WITH
    poistettavat_tapahtumaidt AS (
      SELECT id
      FROM integraatiotapahtuma
      WHERE alkanut < :paivamaara),
    viestien_poisto AS (
    DELETE FROM integraatioviesti
    WHERE integraatiotapahtuma IN (
      SELECT id
      FROM poistettavat_tapahtumaidt))
DELETE FROM integraatiotapahtuma
WHERE id IN (SELECT id
             FROM poistettavat_tapahtumaidt);

-- name: hae-jarjestelmien-integraatiot
-- Hakee kaikki järjestelmien integraatiot
SELECT
  jarjestelma AS jarjestelma,
  nimi        AS integraatio
FROM integraatio;

-- name: hae-jarjestelman-integraatiotapahtumat-aikavalilla
-- Hakee annetun järjestelmän integraatiotapahtumat annetulla aikavälillä
SELECT it.id, it.ulkoinenid, it.lisatietoja, it.alkanut, it.paattynyt, it.onnistunut,
       i.id as integraatio_id,
       i.jarjestelma as integraatio_jarjestelma,
       i.nimi as integraatio_nimi
  FROM integraatiotapahtuma it
  JOIN integraatio i ON it.integraatio = i.id
 WHERE (:jarjestelma_annettu = false OR jarjestelma ILIKE :jarjestelma)
   AND (:integraatio_annettu = false OR nimi ILIKE :integraatio)
   AND alkanut >= :alkaen AND alkanut <= :paattyen;

-- name: hae-uusimmat-integraatiotapahtumat
-- Hakee uusimmat integraatiotapahtumat
SELECT it.id, it.ulkoinenid, it.lisatietoja, it.alkanut, it.paattynyt, it.onnistunut,
       i.id as integraatio_id,
       i.jarjestelma as integraatio_jarjestelma,
       i.nimi as integraatio_nimi
  FROM integraatiotapahtuma it
  JOIN integraatio i ON it.integraatio = i.id
 WHERE (:jarjestelma_annettu = false OR jarjestelma ILIKE :jarjestelma)
   AND (:integraatio_annettu = false OR nimi ILIKE :integraatio)
ORDER BY alkanut DESC
 LIMIT 50;

-- name: hae-integraatiotapahtuman-viestit
-- Hakee annetun integraatiotapahtuman viestit
SELECT
  id,
  integraatiotapahtuma,
  suunta,
  sisaltotyyppi,
  siirtotyyppi,
  sisalto,
  otsikko,
  parametrit,
  osoite,
  kasitteleva_palvelin as "kasitteleva-palvelin"
FROM integraatioviesti
WHERE integraatiotapahtuma = :integraatiotapahtumaid;

-- name: hae-integraatiotapahtumien-maarat
-- Hakee annetun integraation tapahtumien määrät päivittäin ryhmiteltynä
SELECT
  date_trunc('day', it.alkanut) as pvm,
  it.integraatio as integraatio,
  i.jarjestelma as jarjestelma,
  i.nimi as nimi,
  count(*) as maara
FROM integraatiotapahtuma it
  JOIN integraatio i ON it.integraatio = i.id
WHERE (:jarjestelma_annettu = false OR i.jarjestelma ILIKE :jarjestelma)
      AND (:integraatio_annettu = false OR i.nimi ILIKE :integraatio)
GROUP BY pvm, integraatio, jarjestelma, nimi
ORDER BY pvm;

-- name: hae-integraation-id
SELECT id
FROM integraatio
WHERE jarjestelma = :jarjestelma AND nimi = :integraatio;