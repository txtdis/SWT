---------------------------
-- POPULATE IINTIAL DATA --
---------------------------

INSERT INTO item_tier (id, name)
     VALUES (0,
             'ROOT'),
            (DEFAULT,
             'BUSINESS UNIT'),
            (DEFAULT,
             'CATEGORY'),
            (DEFAULT,
             'PRODUCT LINE'),
            (DEFAULT,
             'BRAND'),
            (DEFAULT,
             'VARIANT'),
            (DEFAULT,
             'PRODUCT');

INSERT INTO uom (id, unit)
     VALUES (0,
             'PC'),
            (DEFAULT,
             'CS'),
            (DEFAULT,
             'KG'),
            (DEFAULT,
             'L'),
            (DEFAULT,
             'PK');

INSERT INTO item_type (name)
     VALUES ('PURCHASED'),
            ('REPACKED'),
            ('BUNDLED'),
            ('MADE'),
            ('VIRTUAL'),
            ('MONETARY'),
            ('DERIVED');

INSERT INTO location (id, name)
     VALUES (0,
             'REEFER'),
            (1,
             'CONTAINER'),
            (2,
             'OFFICE'),
            (3,
             'LOMA DE GATO'),
            (4,
             'KOOL HAUZ'),
            (5,
             'RJT818'),
            (6,
             'RJS966'),
            (7,
             'RKR359');

INSERT INTO channel (name)
     VALUES ('SELF'),
            ('WAREHOUSE SALES'),
            ('BANK'),
            ('VIRTUAL'),
            ('VENDOR'),
            ('GOVERNMENT'),
            ('DISPOSAL'),
            ('OTHERS'),
            ('INTERNAL');

INSERT INTO route (name)
     VALUES ('WAREHOUSE SALES'),
            ('OTHERS');

INSERT INTO area_tier (name)
     VALUES ('COUNTRY'),
            ('PROVINCE'),
            ('CITY'),
            ('DISTRICT');

INSERT INTO area (name, tier_id)
     VALUES ('BULACAN',
             2),
            ('SAN JOSE DEL MONTE',
             3),
            ('ASSUMPTION',
             4),
            ('BAGONG BUHAY',
             4),
            ('CITRUS',
             4),
            ('CIUDAD REAL',
             4),
            ('DULONG BAYAN',
             4),
            ('FATIMA',
             4),
            ('FRANCISCO HOMES',
             4),
            ('GAYA-GAYA',
             4),
            ('GRACEVILLE',
             4),
            ('GUMAOC',
             4),
            ('KAYBANBAN',
             4),
            ('KAYPIAN',
             4),
            ('LAWANG PARE',
             4),
            ('MAHARLIKA',
             4),
            ('MINUYAN',
             4),
            ('MUZON',
             4),
            ('PARADISE',
             4),
            ('POBLACION',
             4),
            ('SAN ISIDRO',
             4),
            ('SAN MANUEL',
             4),
            ('SAN MARTIN',
             4),
            ('SAN PEDRO',
             4),
            ('SAN RAFAEL',
             4),
            ('SAN ROQUE',
             4),
            ('SAPANG PALAY',
             4),
            ('SAINT MARTIN DE PORRES',
             4),
            ('SANTA CRUZ',
             4),
            ('SANTO CRISTO',
             4),
            ('SANTO NINO',
             4),
            ('TUNGKONG MANGGA',
             4),
            ('SANTA MARIA',
             3),
            ('BAGBAGUIN',
             4),
            ('BALASING',
             4),
            ('BUENAVISTA',
             4),
            ('BULAC',
             4),
            ('CAMANGYANAN',
             4),
            ('CATMON',
             4),
            ('CAYPOMBO',
             4),
            ('CAYSIO',
             4),
            ('GUYONG',
             4),
            ('LALAKHAN',
             4),
            ('MAG-ASAWANG SAPA',
             4),
            ('MAHABANG PARANG',
             4),
            ('MANGGAHAN',
             4),
            ('PARADA',
             4),
            ('PULONG BUHANGIN',
             4),
            ('SAN GABRIEL',
             4),
            ('SAN JOSE PATAG',
             4),
            ('SAN VICENTE',
             4),
            ('SANTA CLARA',
             4),
            ('SANTO TOMAS',
             4),
            ('SILANGAN',
             4),
            ('TUMANA',
             4);

INSERT INTO area_tree (parent_id, child_id)
     VALUES (1,
             2),
            --SAN FRANCISCO DEL MONTE
            (2,
             3),
            --ASSUMPTION
            (2,
             4),
            --BAGONG BUHAY
            (2,
             5),
            --CITRUS
            (2,
             6),
            --CIUDAD REAL
            (2,
             7),
            --DULONG BAYAN
            (2,
             8),
            --FATIMA
            (2,
             9),
            --FRANCISCO HOMES
            (2,
             10),
            --GAYA-GAYA
            (2,
             11),
            --GRACEVILLE
            (2,
             12),
            --GUMAOC
            (2,
             13),
            --KAYBANBAN
            (2,
             14),
            --KAYPIAN
            (2,
             15),
            --LAWANG PARE
            (2,
             16),
            --MAHARLIKA
            (2,
             17),
            --MINUYAN
            (2,
             18),
            --MUZON
            (2,
             19),
            --PARADISE
            (2,
             20),
            --POBLACION
            (2,
             21),
            --SAN ISIDRO
            (2,
             22),
            --SAN MANUEL
            (2,
             23),
            --SAN MARTIN
            (2,
             24),
            --SAN PEDRO
            (2,
             25),
            --SAN RAFAEL
            (2,
             26),
            --SAN ROQUE
            (2,
             27),
            --SAPANG PALAY
            (2,
             28),
            --SAINT MARTIN DE PORRES
            (2,
             29),
            --SANTA CRUZ
            (2,
             30),
            --SANTO CRISTO
            (2,
             31),
            --SANTO NINO
            (2,
             32),
            --TUNGKONG MANGGA
            (1,
             33),
            --SANTA MARIA
            (33,
             34),
            --BAGBAGUIN
            (33,
             35),
            --BALASING
            (33,
             36),
            --BUENAVISTA
            (33,
             37),
            --BULAC
            (33,
             38),
            --CAMANGYANAN
            (33,
             39),
            --CATMON
            (33,
             40),
            --CAYPOMBO
            (33,
             41),
            --CAYSIO
            (33,
             42),
            --GUYONG
            (33,
             43),
            --LALAKHAN
            (33,
             44),
            --MAG-ASAWANG SAPA
            (33,
             45),
            --MAHABANG PARANG
            (33,
             46),
            --MANGGAHAN
            (33,
             47),
            --PARADA
            (33,
             20),
            --POBLACION
            (33,
             48),
            --PULONG BUHANGIN
            (33,
             49),
            --SAN GABRIEL
            (33,
             50),
            --SAN JOSE PATAG
            (33,
             51),
            --SAN VICENTE
            (33,
             52),
            --SANTA CLARA
            (33,
             29),
            --SANTA CRUZ
            (33,
             53),
            --SANTO TOMAS
            (33,
             54),
            --SILANGAN
            (33,
             55)
--TUMANA
;
INSERT INTO price_tier (name)
     VALUES ('PURCHASE'),
            ('WHOLESALE'),
            ('RETAIL');

INSERT INTO channel_price_tier (channel_id, tier_id, start_date)
     VALUES (0,
             0,
             'epoch'),
            --SELF, PURCHASE
            (1,
             1,
             'epoch'),
            --BAKERY, WHOLESALE
            (2,
             1,
             'epoch'),
            --CPP, WHOLESALE
            (3,
             1,
             'epoch'),
            --DRUGSTORE, WHOLESALE
            (4,
             1,
             'epoch'),
            --FOOD OUTLET, WHOLESALE
            (5,
             1,
             'epoch'),
            --GROCERY, WHOLESALE
            (6,
             1,
             'epoch'),
            --MARKET STALL, WHOLESALE
            (7,
             1,
             'epoch'),
            --SARI-SARI STORE, WHOLESALE
            (8,
             3,
             'epoch'),
            --SUPERMARKET, LIST
            (9,
             1,
             'epoch');

--WAREHOUSE SALE, WHOLESALE;

INSERT INTO target_stock_days (item_family_id, days)
     -- RM, DRY
     VALUES (-1,
             7),
            (-2,
             15);

INSERT INTO vendor_specific (vendor_id, lead_time, self_id, note)
     VALUES (488,
             2,
             $$116718$$,
             $$between 9:00AM to 12:00NN$$);

INSERT INTO target_type (name)
     VALUES ($$DEALERS' INCENTIVE$$),
            ($$MONTHLY OPERATION$$);

INSERT INTO purchase_category
     VALUES ($$BMC CHILLED$$),
            ($$BMC DRY$$),
            ($$CHEESEBALLS$$),
            ($$CHRISTMAS HAM$$),
            ($$COF$$),
            ($$GP$$),
            ($$ICE CREAM$$),
            ($$JA$$),
            ($$OIL$$),
            ($$PANCAKE$$),
            ($$RM$$),
            ($$RTD$$);

INSERT INTO default_number (name, value, start_date)
     VALUES ($$VAT$$,
             0.12,
             'epoch');

INSERT INTO default_text (name, value, start_date)
     VALUES ($$CURRENCY$$,
             $$₱$$,
             'epoch');

INSERT INTO quality (id, name)
     VALUES (0,
             $$GOOD$$),
            (DEFAULT,
             $$HOLD$$),
            (DEFAULT,
             $$BAD$$);
