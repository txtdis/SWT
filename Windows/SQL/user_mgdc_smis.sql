﻿DROP ROLE IF EXISTS	jun;
CREATE ROLE "jun" LOGIN PASSWORD 'sniper' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO jun;

DROP ROLE IF EXISTS	noel;
CREATE ROLE "noel" LOGIN PASSWORD 'nash24' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO noel;

DROP ROLE IF EXISTS	kimberly;
CREATE ROLE "kimberly" LOGIN PASSWORD '070188' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT guest TO kimberly;

DROP ROLE IF EXISTS	roland;
CREATE ROLE "roland" LOGIN PASSWORD 'TIPON' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO roland;

DROP ROLE IF EXISTS	marivic;
CREATE ROLE "marivic" LOGIN PASSWORD 'marvic' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO marivic;

DROP ROLE IF EXISTS	badette;
CREATE ROLE "badette" LOGIN PASSWORD '013094' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO badette;

DROP ROLE IF EXISTS	sheryl;
CREATE ROLE "sheryl" LOGIN PASSWORD '10-8-91' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO sheryl;

DROP ROLE IF EXISTS	jayson;
CREATE ROLE "jayson" LOGIN PASSWORD '1430' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT user_sales TO jayson;

DROP ROLE IF EXISTS	jackie;
CREATE ROLE "jackie" LOGIN PASSWORD 'robbie' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT sys_admin TO jackie;

DROP ROLE IF EXISTS	ronald;
CREATE ROLE "ronald" LOGIN PASSWORD 'alpha' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT sys_admin TO ronald;

DROP ROLE IF EXISTS	butch;
CREATE ROLE "butch" LOGIN PASSWORD 'attila' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT sys_admin TO butch;

DROP ROLE IF EXISTS	lorna;
CREATE ROLE "lorna" LOGIN PASSWORD 'rod' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
GRANT sys_admin TO lorna;
