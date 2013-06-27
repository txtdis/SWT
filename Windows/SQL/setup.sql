----------------------
-- CREATE SUPERUSER --
----------------------

DROP ROLE IF EXISTS txtdis;
CREATE ROLE txtdis LOGIN
  ENCRYPTED PASSWORD 'md54b0d2e10ef9ae0bf4539f9d8a83ded8b'
  SUPERUSER INHERIT CREATEDB CREATEROLE;

---------------------
-- CREATE DATABASE --
---------------------
DROP DATABASE IF EXISTS magnum_sta_maria_30;
CREATE DATABASE magnum_sta_maria_30
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
