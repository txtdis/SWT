﻿INSERT INTO item_tier (id, name)
     VALUES (0, 'ROOT'),
            (DEFAULT, 'BUSINESS UNIT'),
            (DEFAULT, 'CATEGORY'),
            (DEFAULT, 'PRODUCT LINE'),
            (DEFAULT, 'BRAND'),
            (DEFAULT, 'VARIANT'),
            (DEFAULT, 'PRODUCT');

INSERT INTO uom (id, unit)
     VALUES (0, 'SU'),
            (DEFAULT, 'CS'),
            (DEFAULT, 'BT'),
            (DEFAULT, 'PC');

INSERT INTO item_type (name)
     VALUES ('RETURNABLE'),
            ('PURCHASED'),
            ('REPACKED'),
            ('BUNDLED'),
            ('MADE'),
            ('VIRTUAL'),
            ('MONETARY');

INSERT INTO location (name)
     VALUES ('HAVANA');

INSERT INTO channel (name)
     VALUES ('SELF'),
            ('RETAIL, SMALL'),
            ('RETAIL, MEDIUM'),
            ('RETAIL, LARGE'),
            ('WHOLESALE, SMALL'),
            ('WHOLESALE, MEDIUM'),
            ('WHOLESALE, LARGE'),
            ('GROCERY'),
            ('ROUTE'),
            ('BANK'),
            ('VIRTUAL'),
            ('VENDOR'),
            ('GOVERNMENT'),
            ('DISPOSAL'),
            ('OTHERS'),
            ('INTERNAL');


INSERT INTO channel_price_tier (channel_id, tier_id, family_id)
     VALUES (1, 0, -3),
            (2, 1, -3),
            (3, 1, -3),
            (4, 1, -3),
            (5, 1, -3),
            (6, 1, -3),
            (7, 1, -3),
            (8, 1, -3),
            (9, 1, -3),
            (10, 1, -3),
            (11, 1, -3),
            (12, 1, -3),
            (13, 1, -3),
            (14, 0, -3),
            (15, 1, -3),
            (16, 0, -3);
   

INSERT INTO route (name)
     VALUES ('ROUTE 1'),
            ('ROUTE 2'),
            ('ROUTE 3'),
            ('ROUTE 4'),
            ('ROUTE 5'),
            ('ROUTE 6'),
            ('WALK-IN'),
            ('BOOKED'),
            ('OTHERS');

INSERT INTO area_tier (name)
     VALUES ('CITY'), ('DISTRICT'), ('BARANGAY');

INSERT INTO area (name, tier_id)
     VALUES ('MANILA', 1),
            ('SAMPALOC', 2),
            ('409', 3),
            ('411', 3),
            ('405', 3),
            ('430', 3),
            ('458', 3),
            ('407', 3),
            ('401', 3),
            ('402', 3),
            ('400', 3),
            ('441', 3),
            ('399', 3),
            ('398', 3),
            ('413', 3),
            ('433', 3),
            ('410', 3),
            ('489', 3),
            ('487', 3),
            ('514', 3),
            ('509', 3),
            ('507', 3),
            ('513', 3),
            ('515', 3),
            ('517', 3),
            ('505', 3),
            ('518', 3),
            ('491', 3),
            ('476', 3),
            ('477', 3),
            ('520', 3),
            ('412', 3),
            ('486', 3),
            ('482', 3),
            ('496', 3),
            ('484', 3),
            ('483', 3),
            ('500', 3),
            ('497', 3),
            ('495', 3),
            ('496', 3),
            ('510', 3),
            ('551', 3),
            ('467', 3),
            ('463', 3),
            ('466', 3),
            ('667', 3),
            ('461', 3),
            ('463', 3),
            ('519', 3),
            ('399', 3),
            ('570', 3),
            ('555', 3),
            ('583', 3),
            ('420', 3),
            ('419', 3),
            ('430', 3),
            ('524', 3),
            ('436', 3),
            ('432', 3),
            ('601', 3),
            ('455', 3),
            ('454', 3),
            ('556', 3),
            ('441', 3),
            ('443', 3),
            ('522', 3),
            ('525', 3),
            ('547', 3),
            ('551', 3),
            ('423', 3),
            ('574', 3),
            ('424', 3),
            ('565', 3),
            ('559', 3),
            ('506', 3),
            ('562', 3),
            ('575', 3),
            ('581', 3),
            ('580', 3),
            ('530', 3),
            ('540', 3),
            ('559', 3),
            ('STA CRUZ', 2),
            ('311', 3),
            ('312', 3),
            ('316', 3),
            ('321', 3),
            ('337', 3),
            ('323', 3),
            ('336', 3),
            ('322', 3),
            ('317', 3),
            ('321', 3),
            ('338', 3),
            ('339', 3),
            ('407', 3),
            ('406', 3),
            ('318', 3),
            ('319', 3),
            ('329', 3),
            ('835', 3),
            ('330', 3),
            ('253', 3),
            ('258', 3),
            ('357', 3),
            ('344', 3),
            ('315', 3),
            ('255', 3),
            ('400', 3),
            ('637', 3),
            ('QUIAPO', 2),
            ('391', 3),
            ('394', 3),
            ('393', 3),
            ('387', 3),
            ('385', 3),
            ('384', 3),
            ('637', 3),
            ('408', 3),
            ('462', 3),
            ('393', 3),
            ('INTRAMUROS', 2),
            ('395', 3),
            ('396', 3),
            ('397', 3),
            ('398', 3),
            ('399', 3),
            ('400', 3),
            ('401', 3),
            ('402', 3),
            ('BASECO', 2),
            ('649', 3),
            ('650', 3),
            ('651', 3),
            ('652', 3),
            ('653', 3),
            ('PANDACAN', 2),
            ('828', 3),
            ('822', 3),
            ('866', 3),
            ('860', 3),
            ('836', 3),
            ('861', 3),
            ('833', 3),
            ('786', 3),
            ('781', 3),
            ('780', 3),
            ('STA ANA', 2),
            ('873', 3),
            ('877', 3),
            ('792', 3),
            ('790', 3),
            ('789', 3),
            ('813', 3),
            ('786', 3),
            ('881', 3),
            ('787', 3),
            ('879', 3),
            ('812', 3),
            ('883', 3),
            ('882', 3),
            ('788', 3),
            ('784', 3),
            ('791', 3),
            ('781', 3),
            ('891', 3),
            ('866', 3),
            ('819', 3),
            ('798', 3),
            ('810', 3),
            ('780', 3),
            ('813', 3),
            ('873', 3),
            ('792', 3),
            ('902', 3),
            ('905', 3),
            ('906', 3),
            ('900', 3),
            ('796', 3),
            ('903', 3),
            ('814', 3),
            ('898', 3),
            ('897', 3),
            ('790', 3),
            ('781', 3),
            ('798', 3),
            ('771', 3),
            ('SAN ANDRES', 2),
            ('798', 3),
            ('785', 3),
            ('788', 3),
            ('783', 3),
            ('789', 3),
            ('787', 3),
            ('775', 3),
            ('778', 3),
            ('866', 3),
            ('772', 3),
            ('774', 3),
            ('800', 3),
            ('779', 3),
            ('MALATE', 2),
            ('659', 3),
            ('659-A', 3),
            ('660', 3),
            ('660-A', 3),
            ('663', 3),
            ('664', 3),
            ('665', 3),
            ('666', 3),
            ('667', 3),
            ('668', 3),
            ('669', 3),
            ('670', 3),
            ('PACO', 2),
            ('838', 3),
            ('839', 3),
            ('840', 3),
            ('841', 3),
            ('842', 3),
            ('843', 3),
            ('844', 3),
            ('845', 3),
            ('846', 3),
            ('847', 3),
            ('848', 3),
            ('849', 3),
            ('850', 3),
            ('851', 3),
            ('852', 3),
            ('853', 3),
            ('855', 3),
            ('856', 3),
            ('857', 3),
            ('858', 3),
            ('859', 3),
            ('860', 3),
            ('861', 3),
            ('862', 3),
            ('863', 3),
            ('864', 3),
            ('865', 3),
            ('866', 3),
            ('867', 3),
            ('868', 3),
            ('869', 3),
            ('870', 3),
            ('871', 3),
            ('872', 3),
            ('STA MESA', 2),
            ('599', 3),
            ('600', 3),
            ('598', 3),
            ('597', 3),
            ('596', 3),
            ('602', 3),
            ('595', 3),
            ('587', 3),
            ('456', 3),
            ('594', 3),
            ('581', 3),
            ('570', 3),
            ('578', 3),
            ('594', 3),
            ('507', 3),
            ('588', 3),
            ('621', 3),
            ('607', 3),
            ('636', 3),
            ('627', 3),
            ('626', 3),
            ('628', 3),
            ('623', 3),
            ('629', 3),
            ('587', 3),
            ('525', 3),
            ('621', 3),
            ('601', 3),
            ('524', 3),
            ('590', 3),
            ('615', 3),
            ('594', 3),
            ('624', 3),
            ('625', 3),
            ('608', 3),
            ('602', 3),
            ('615', 3),
            ('613', 3);

INSERT INTO area (name, tier_id)
     VALUES ('METRO MANILA', 1),
            ('MAKATI', 2),
            ('BANGKAL', 3),
            ('BEL-AIR', 3),
            ('CARMONA', 3),
            ('CEMBO', 3),
            ('COMEMBO', 3),
            ('DASMARIÑAS', 3),
            ('EAST REMBO', 3),
            ('FORBES PARK', 3),
            ('GUADALUPE NUEVO', 3),
            ('GUADALUPE VIEJO', 3),
            ('KASILAWAN', 3),
            ('LA PAZ', 3),
            ('MAGALLANES', 3),
            ('OLYMPIA', 3),
            ('PALANAN', 3),
            ('PEMBO', 3),
            ('PINAGKAISAHAN', 3),
            ('PIO DEL PILAR', 3),
            ('PITOGO', 3),
            ('POBLACION', 3),
            ('POST PROPER NORTH', 3),
            ('POST PROPER SOUTH', 3),
            ('RIZAL', 3),
            ('SAN ANTONIO', 3),
            ('SAN ISIDRO', 3),
            ('SAN LORENZO', 3),
            ('SANTA CRUZ', 3),
            ('SINGKAMAS', 3),
            ('SOUTH CEMBO', 3),
            ('TEJEROS', 3),
            ('URDANETA', 3),
            ('VALENZUELA', 3),
            ('WEST REMBO', 3);

INSERT INTO area_tree (child_id, parent_id)
     VALUES (1, 0),
            (2, 1),
            (3, 2),
            (4, 2),
            (5, 2),
            (6, 2),
            (7, 2),
            (8, 2),
            (9, 2),
            (10, 2),
            (11, 2),
            (12, 2),
            (13, 2),
            (14, 2),
            (15, 2),
            (16, 2),
            (17, 2),
            (18, 2),
            (19, 2),
            (20, 2),
            (21, 2),
            (22, 2),
            (23, 2),
            (24, 2),
            (25, 2),
            (26, 2),
            (27, 2),
            (28, 2),
            (29, 2),
            (30, 2),
            (31, 2),
            (32, 2),
            (33, 2),
            (34, 2),
            (35, 2),
            (36, 2),
            (37, 2),
            (38, 2),
            (39, 2),
            (40, 2),
            (41, 2),
            (42, 2),
            (43, 2),
            (44, 2),
            (45, 2),
            (46, 2),
            (47, 2),
            (48, 2),
            (49, 2),
            (50, 2),
            (51, 2),
            (52, 2),
            (53, 2),
            (54, 2),
            (55, 2),
            (56, 2),
            (57, 2),
            (58, 2),
            (59, 2),
            (60, 2),
            (61, 2),
            (62, 2),
            (63, 2),
            (64, 2),
            (65, 2),
            (66, 2),
            (67, 2),
            (68, 2),
            (69, 2),
            (70, 2),
            (71, 2),
            (72, 2),
            (73, 2),
            (74, 2),
            (75, 2),
            (76, 2),
            (77, 2),
            (78, 2),
            (79, 2),
            (80, 2),
            (81, 2),
            (82, 2),
            (83, 2),
            (84, 1),
            (85, 84),
            (86, 84),
            (87, 84),
            (88, 84),
            (89, 84),
            (90, 84),
            (91, 84),
            (92, 84),
            (93, 84),
            (94, 84),
            (95, 84),
            (96, 84),
            (97, 84),
            (98, 84),
            (99, 84),
            (100, 84),
            (101, 84),
            (102, 84),
            (103, 84),
            (104, 84),
            (105, 84),
            (106, 84),
            (107, 84),
            (108, 84),
            (109, 84),
            (110, 84),
            (111, 84),
            (112, 1),
            (113, 112),
            (114, 112),
            (115, 112),
            (116, 112),
            (117, 112),
            (118, 112),
            (119, 112),
            (120, 112),
            (121, 112),
            (122, 112),
            (123, 1),
            (124, 123),
            (125, 123),
            (126, 123),
            (127, 123),
            (128, 123),
            (129, 123),
            (130, 123),
            (131, 123),
            (132, 1),
            (133, 132),
            (134, 132),
            (135, 132),
            (136, 132),
            (137, 132),
            (138, 1),
            (139, 138),
            (140, 138),
            (141, 138),
            (142, 138),
            (143, 138),
            (144, 138),
            (145, 138),
            (146, 138),
            (147, 138),
            (148, 138),
            (149, 1),
            (150, 149),
            (151, 149),
            (152, 149),
            (153, 149),
            (154, 149),
            (155, 149),
            (156, 149),
            (157, 149),
            (158, 149),
            (159, 149),
            (160, 149),
            (161, 149),
            (162, 149),
            (163, 149),
            (164, 149),
            (165, 149),
            (166, 149),
            (167, 149),
            (168, 149),
            (169, 149),
            (170, 149),
            (171, 149),
            (172, 149),
            (173, 149),
            (174, 149),
            (175, 149),
            (176, 149),
            (177, 149),
            (178, 149),
            (179, 149),
            (180, 149),
            (181, 149),
            (182, 149),
            (183, 149),
            (184, 149),
            (185, 149),
            (186, 149),
            (187, 149),
            (188, 149),
            (189, 1),
            (190, 189),
            (191, 189),
            (192, 189),
            (193, 189),
            (194, 189),
            (195, 189),
            (196, 189),
            (197, 189),
            (198, 189),
            (199, 189),
            (200, 189),
            (201, 189),
            (202, 189),
            (203, 1),
            (204, 203),
            (205, 203),
            (206, 203),
            (207, 203),
            (208, 203),
            (209, 203),
            (210, 203),
            (211, 203),
            (212, 203),
            (213, 203),
            (214, 203),
            (215, 203),
            (216, 1),
            (217, 216),
            (218, 216),
            (219, 216),
            (220, 216),
            (221, 216),
            (222, 216),
            (223, 216),
            (224, 216),
            (225, 216),
            (226, 216),
            (227, 216),
            (228, 216),
            (229, 216),
            (230, 216),
            (231, 216),
            (232, 216),
            (233, 216),
            (234, 216),
            (235, 216),
            (236, 216),
            (237, 216),
            (238, 216),
            (239, 216),
            (240, 216),
            (241, 216),
            (242, 216),
            (243, 216),
            (244, 216),
            (245, 216),
            (246, 216),
            (247, 216),
            (248, 216),
            (249, 216),
            (250, 216),
            (251, 1),
            (252, 251),
            (253, 251),
            (254, 251),
            (255, 251),
            (256, 251),
            (257, 251),
            (258, 251),
            (259, 251),
            (260, 251),
            (261, 251),
            (262, 251),
            (263, 251),
            (264, 251),
            (265, 251),
            (266, 251),
            (267, 251),
            (268, 251),
            (269, 251),
            (270, 251),
            (271, 251),
            (272, 251),
            (273, 251),
            (274, 251),
            (275, 251),
            (276, 251),
            (277, 251),
            (278, 251),
            (279, 251),
            (280, 251),
            (281, 251),
            (282, 251),
            (283, 251),
            (284, 251),
            (285, 251),
            (286, 251),
            (287, 251),
            (288, 251),
            (289, 251);

INSERT INTO default_number (name, value, start_date)
     VALUES ($$VAT$$, 0.12, 'epoch');

INSERT INTO default_text (name, value, start_date)
     VALUES ($$ITEM FAMILY$$, $$BEER$$, 'epoch'),
	    ($$PARTNER DISCOUNT$$, $$AMOUNT$$, 'epoch'),
	    ($$VOLUME DISCOUNT$$, $$SINGLE$$, 'epoch'),
            ($$CURRENCY$$, $$₱$$, 'epoch');

INSERT INTO quality (id, name)
     VALUES (0, $$GOOD$$), (DEFAULT, $$HOLD$$), (DEFAULT, $$BAD$$);

INSERT INTO item_family (name, tier_id)
     VALUES ('SAN MIGUEL', 1), 
            ('SMB', 2), 
            ('BEER', 3),
            ('GSM', 2), 
            ('FRUIT DRINK', 3);

     
