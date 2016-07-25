
CREATE OR REPLACE FUNCTION etsi_jatkopatka(viiva geometry, jatkopatkat geometry) RETURNS geometry AS $$
DECLARE
  tmp geometry;
  jatkettu geometry;
BEGIN
  jatkettu := viiva;
  FOR i IN 1..ST_NumGeometries(jatkopatkat) LOOP
     tmp := ST_GeometryN(jatkopatkat, i);
     IF ST_DWithin(ST_EndPoint(jatkettu), ST_StartPoint(tmp), 5) THEN
        jatkettu := ST_MakeLine(jatkettu,tmp);
     ELSEIF ST_DWithin(ST_EndPoint(tmp), ST_StartPoint(jatkettu), 5) THEN
        RETURN ST_MakeLine(tmp,jatkettu);
     END IF;
  END LOOP;
  RETURN jatkettu;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION yhdista_viivat_jarjestyksessa(viiva geometry) RETURNS geometry AS $$
DECLARE
  jarjestetty geometry;
BEGIN
    jarjestetty := ST_GeometryN(viiva,1);
    FOR i IN 1..ST_NumGeometries(viiva) LOOP
        jarjestetty := etsi_jatkopatka(jarjestetty, viiva);
    END LOOP;
  RETURN jarjestetty;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION geometrian_pituus(g geometry, apoint geometry) RETURNS float8 AS $$
DECLARE
  lahinosa INTEGER;
  pituus float8;
  pala geometry;
BEGIN
  IF ST_NumGeometries(g) > 1 THEN
     SELECT path[1]
       FROM ST_Dump(g)
      WHERE ST_DWithin(geom, apoint, 1000)
      ORDER BY ST_Length(ST_ShortestLine(geom, apoint)) ASC
      LIMIT 1
       INTO lahinosa;

     pituus := 0;
     
     FOR i IN 1 .. lahinosa LOOP
        pala := ST_GeometryN(g,i);
	
        IF i=lahinosa THEN
	   pituus := pituus + ST_Length(ST_Line_Substring(pala, 0, ST_LineLocatePoint(pala, apoint)));
	ELSE
	   pituus := pituus + ST_Length(pala);
	END IF;
     END LOOP;

     RETURN pituus;
  ELSE
     RETURN ST_Length(ST_Line_Substring(g,0,ST_LineLocatePoint(ST_LineMerge(g), apoint)));
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION tr_osan_etaisyys(
  piste geometry, tienro INTEGER, treshold INTEGER)
  RETURNS INTEGER
AS $$
DECLARE
   alkuosa RECORD;
   alkuet NUMERIC;
   kaikki_ajoradat geometry;   
BEGIN
   SELECT osoite3, tie, ajorata, osa, tiepiiri, geom
      FROM tieverkko_paloina
      WHERE ST_DWithin(geom, piste, treshold)
        AND tie=tienro
      ORDER BY ST_Length(ST_ShortestLine(geom, piste)) ASC
      LIMIT 1
   INTO alkuosa;

  SELECT yhdista_viivat_jarjestyksessa(ST_Collect(geom))
    FROM tieverkko_paloina
   WHERE tie=alkuosa.tie AND osa=alkuosa.osa AND (ajorata=0 OR ajorata=alkuosa.ajorata) INTO kaikki_ajoradat;

   SELECT ST_Length(ST_Line_Substring(kaikki_ajoradat, 0, ST_LineLocatePoint(kaikki_ajoradat, piste))) INTO alkuet;
   
   RETURN alkuet::INTEGER;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION multiline_project_point(amultils geometry, apoint geometry)
  RETURNS float8 AS
$$
DECLARE
  adist float8;
  bdist float8;
  totallen float8;
BEGIN
  -- kokonaispituus
  totallen := ST_Length(amultils);

  adist := ST_LineLocatePoint(ST_GeometryN(amultils, 1), apoint);
  bdist := ST_LineLocatePoint(ST_GeometryN(amultils, 2), apoint);

  IF adist>=1 THEN
    RETURN (ST_Length(ST_GeometryN(amultils,1)) + ST_Length(ST_Line_Substring(ST_GeometryN(amultils,2),0,bdist)))/totallen;
  ELSE
    RETURN ST_Length(ST_Line_Substring(ST_GeometryN(amultils,1), 0, adist)) / totallen;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION yrita_tierekisteriosoite_pisteelle(
  piste geometry, treshold INTEGER)
  RETURNS tr_osoite
AS $$
DECLARE
  alkuosa RECORD;
  alkuet NUMERIC;
  palojenpit NUMERIC;
  kaikki_ajoradat geometry;
BEGIN
  SELECT osoite3, tie, ajorata, osa, tiepiiri, geom
  FROM tieverkko_paloina
  WHERE ST_DWithin(geom, piste, treshold)
  ORDER BY ST_Length(ST_ShortestLine(geom, piste)) ASC
  LIMIT 1
  INTO alkuosa;

  IF alkuosa IS NULL THEN
    RETURN NULL;
  END IF;

  SELECT yhdista_viivat_jarjestyksessa(ST_Collect(geom))
    FROM tieverkko_paloina
   WHERE tie=alkuosa.tie AND osa=alkuosa.osa AND (ajorata=0 OR ajorata=alkuosa.ajorata) INTO kaikki_ajoradat;
   
  SELECT geometrian_pituus(kaikki_ajoradat, piste) INTO alkuet;

  RETURN ROW(alkuosa.tie, alkuosa.osa, alkuet::INTEGER, 0, 0, ST_ClosestPoint(piste, alkuosa.geom)::geometry);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION tierekisteriosoite_pisteelle(
  piste geometry, treshold INTEGER)
  RETURNS tr_osoite
AS $$
DECLARE
  osoite tr_osoite;
BEGIN
  osoite := yrita_tierekisteriosoite_pisteelle(piste, treshold);
  IF osoite IS NULL THEN
    RAISE EXCEPTION 'pisteelle ei löydy tietä';
  ELSE
    RETURN osoite;
  END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION yrita_tierekisteriosoite_pisteille(
  alkupiste geometry, loppupiste geometry, treshold INTEGER)
  RETURNS tr_osoite
AS $$
DECLARE
  reitti geometry;
  apiste geometry;
  bpiste geometry;
  aosa INTEGER;
  bosa INTEGER;
  ajoratavalinta INTEGER;
  tienosavali RECORD;
  ap NUMERIC;
  bp NUMERIC;
  alkuet INTEGER;
  loppuet INTEGER;
  itmp INTEGER;
  tmp geometry;
  atmp float8;
  btmp float8;
BEGIN
  -- valitaan se tie ja tienosaväli jota lähellä alku- ja loppupisteet ovat yhdessä lähimpänä
  SELECT a.tie,
    a.osa AS aosa,
    b.osa AS bosa,
    a.ajorata AS arata,
    b.ajorata AS brata,
    a.tr_pituus AS apituus,
    b.tr_pituus AS bpituus
  FROM tieverkko_paloina a,
    tieverkko_paloina b
  WHERE ST_DWithin(a.geom, alkupiste, treshold)
        AND ST_DWithin(b.geom, loppupiste, treshold)
        AND a.tie=b.tie
  ORDER BY ST_Length(ST_ShortestLine(alkupiste, a.geom)) +
           ST_Length(ST_ShortestLine(loppupiste, b.geom))
  LIMIT 1
  INTO tienosavali;

  alkuet := tr_osan_etaisyys(alkupiste, tienosavali.tie, treshold);
  loppuet := tr_osan_etaisyys(loppupiste, tienosavali.tie, treshold);

  IF tienosavali.aosa > tienosavali.bosa THEN
    aosa := tienosavali.bosa;
    bosa := tienosavali.aosa;
    apiste := loppupiste;
    bpiste := alkupiste;
    ajoratavalinta := 2;
  ELSEIF tienosavali.aosa = tienosavali.bosa THEN
    aosa := tienosavali.aosa;
    bosa := tienosavali.bosa;
    IF tienosavali.arata=tienosavali.brata AND alkuet>loppuet THEN
      ajoratavalinta := 2;
    ELSEIF tienosavali.arata=tienosavali.brata AND alkuet<=loppuet THEN
      ajoratavalinta := 1;
    ELSEIF tienosavali.arata!=tienosavali.brata AND alkuet>loppuet THEN
      ajoratavalinta := 1;
    ELSEIF tienosavali.arata!=tienosavali.brata AND alkuet<=loppuet THEN
      ajoratavalinta := 2;
    END IF;
  ELSE
    aosa := tienosavali.aosa;
    bosa := tienosavali.bosa;
    ajoratavalinta := 1;
    apiste := alkupiste;
    bpiste := loppupiste;
  END IF;

  IF aosa=bosa THEN
    SELECT ST_LineMerge(ST_Union(geom))
    FROM tieverkko_paloina tv
    WHERE tv.tie = tienosavali.tie
          AND tv.osa = aosa
          AND (tv.ajorata = ajoratavalinta OR tv.ajorata=0)
    INTO reitti;

    -- jos multilinestring, interpoloidaan eri tavalla
    IF ST_NumGeometries(reitti)>1 THEN
      atmp := multiline_project_point(reitti, alkupiste);
      btmp := multiline_project_point(reitti, loppupiste);
      SELECT ST_Line_Substring(reitti, LEAST(atmp, btmp),
                               GREATEST(atmp, btmp)) INTO reitti;
    ELSE
      SELECT ST_Line_Substring(reitti, LEAST(ST_LineLocatePoint(reitti, alkupiste), ST_LineLocatePoint(reitti, loppupiste)),
                               GREATEST(ST_LineLocatePoint(reitti, alkupiste),ST_LineLocatePoint(reitti, loppupiste))) INTO reitti;
    END IF;

  ELSE
    -- kootaan osien geometriat yhdeksi viivaksi
    SELECT yhdista_viivat_jarjestyksessa(ST_Collect((CASE
                                  WHEN tv.osa=aosa
                                    THEN ST_Line_Substring(tv.geom, ST_LineLocatePoint(tv.geom, apiste), 1)
                                  WHEN tv.osa=bosa
                                    THEN ST_Line_Substring(tv.geom, 0, ST_LineLocatePoint(tv.geom, bpiste))
                                  ELSE tv.geom
                                  END)
                                 ORDER BY tv.osa))
    FROM tieverkko_paloina tv
    WHERE tv.tie=tienosavali.tie
          AND tv.osa>=aosa
          AND tv.osa<=bosa
          AND (tv.ajorata = ajoratavalinta OR tv.ajorata = 0)
    INTO reitti;
  END IF;

  IF reitti IS NULL THEN
    RETURN NULL;
  END IF;

  RETURN ROW(tienosavali.tie,
         tienosavali.aosa,
         alkuet,
         tienosavali.bosa,
         loppuet,
         CASE WHEN ajoratavalinta=1 THEN reitti
         ELSE ST_Reverse(reitti)
         END);
END;
$$ LANGUAGE plpgsql;

-- kuvaus: Yritä hakea TR osoite pisteille. Heitä poikkeus, jos ei löydy.
CREATE OR REPLACE FUNCTION tierekisteriosoite_pisteille(
  alkupiste geometry,
  loppupiste geometry,
  treshold INTEGER) RETURNS tr_osoite AS $$
DECLARE
  osoite tr_osoite;
BEGIN
    osoite := yrita_tierekisteriosoite_pisteille(alkupiste, loppupiste, treshold);
    IF osoite IS NULL THEN
      RAISE EXCEPTION 'pisteillä ei yhteistä tietä';
    END IF;
    RETURN osoite;
END;
$$ LANGUAGE plpgsql;

-- Hakee annetuille pisteille (geometrycollection) viivan jokaiselle
-- pistevälille. Palauttaa jokaiselle välille alkupisteen, loppupisteen
-- ja tieverkolle projisoidun geometrian. Jos projisoitua geometriaa ei
-- löydy, palautetaan NULL.
CREATE OR REPLACE FUNCTION tieviivat_pisteille(
  pisteet geometry,
  threshold INTEGER) RETURNS SETOF RECORD AS $$
DECLARE
  alku geometry;
  loppu geometry;
  i INTEGER;
  pisteita INTEGER;
BEGIN
  i := 1;
  pisteita := ST_NumGeometries(pisteet);
  WHILE i < pisteita LOOP
    alku := ST_GeometryN(pisteet, i);
    loppu := ST_GeometryN(pisteet, i+1);
    --RAISE NOTICE 'alku: %, loppu: %', st_astext(alku), st_astext(loppu);
    RETURN NEXT (alku, loppu,
    	   	 (SELECT ytp.geometria
	            FROM yrita_tierekisteriosoite_pisteille(alku, loppu, threshold) ytp));
    i := i + 1;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

-- kuvaus: tierekisteriosoittelle_viiva, kaistan päättely
CREATE OR REPLACE FUNCTION tierekisteriosoitteelle_viiva(
  tie_ INTEGER, aosa_ INTEGER, aet_ INTEGER, losa_ INTEGER, let_ INTEGER)
  RETURNS SETOF geometry
AS $$
DECLARE
  ajoratavalinta INTEGER;
  aos INTEGER;
  los INTEGER;
  aet INTEGER;
  let INTEGER;
  koko_osa geometry;
  koko_pituus float8;
  alkuosan_pituus float8;
  loppuosan_pituus float8;
BEGIN
  IF aosa_ = losa_ THEN
    IF aet_ < let_ THEN
      ajoratavalinta := 1;
      aet := aet_;
      let := let_;
    ELSE
      ajoratavalinta := 2;
      aet := let_;
      let := aet_;
    END IF;
    IF ajoratavalinta=2 THEN
      RETURN QUERY SELECT ST_Reverse(ST_Line_Substring(geom, LEAST(1,aet/ST_Length(geom)::FLOAT), LEAST(1,let/ST_Length(geom)::FLOAT)))
                   FROM tieverkko_paloina
                   WHERE tie=tie_
                         AND osa=aosa_
                         AND (ajorata=0 OR ajorata=2);
    ELSE
      RETURN QUERY SELECT ST_Line_Substring(geom, aet/ST_Length(geom)::FLOAT, let/ST_Length(geom)::FLOAT)
                   FROM tieverkko_paloina
                   WHERE tie=tie_
                         AND osa=aosa_
                         AND (ajorata=0 OR ajorata=1);
    END IF;
  ELSE
    IF aosa_ < losa_ THEN
      ajoratavalinta := 1;
      aos := aosa_;
      los := losa_;
      aet := aet_;
      let := let_;
    ELSE
      ajoratavalinta := 2;
      aos := aosa_;
      los := losa_;
      aet := aet_;
      let := let_;
    END IF;
    IF ajoratavalinta=2 THEN
      SELECT ST_LineMerge(ST_Collect(geom)) FROM tieverkko_paloina WHERE tie=tie_ and osa>=los and osa<=aos and (ajorata=0 or ajorata=2) INTO koko_osa;
      koko_pituus := ST_Length(koko_osa);
      SELECT ST_Length(yhdista_viivat_jarjestyksessa(ST_Collect(geom))) FROM tieverkko_paloina WHERE tie=tie_ and osa=los INTO alkuosan_pituus;
      SELECT ST_Length(yhdista_viivat_jarjestyksessa(ST_Collect(geom))) FROM tieverkko_paloina WHERE tie=tie_ and osa=aos INTO loppuosan_pituus;
      RETURN QUERY SELECT ST_Line_Substring(koko_osa, (alkuosan_pituus-let)/koko_pituus, (koko_pituus-loppuosan_pituus+aet)/koko_pituus);
    ELSE
      SELECT yhdista_viivat_jarjestyksessa(ST_Collect(geom)) FROM tieverkko_paloina WHERE tie=tie_ and osa>=aos and osa<=los and (ajorata=0 or ajorata=1) INTO koko_osa;
      koko_pituus := ST_Length(koko_osa);
      SELECT ST_Length(yhdista_viivat_jarjestyksessa(ST_Collect(geom))) FROM tieverkko_paloina WHERE tie=tie_ and osa=aos INTO alkuosan_pituus;
      SELECT ST_Length(yhdista_viivat_jarjestyksessa(ST_Collect(geom))) FROM tieverkko_paloina WHERE tie=tie_ and osa=los INTO loppuosan_pituus;
      RETURN QUERY SELECT ST_Line_Substring(koko_osa, (alkuosan_pituus-aet)/koko_pituus, (koko_pituus-loppuosan_pituus+let)/koko_pituus);
    END IF;
  END IF;
END;
$$ LANGUAGE plpgsql;
