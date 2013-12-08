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
DROP DATABASE IF EXISTS mgdc_pf;
CREATE DATABASE mgdc_pf
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

DROP DATABASE IF EXISTS mgdc_smb;
CREATE DATABASE mgdc_smb
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

CREATE DATABASE mgdc_gsm1
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

CREATE DATABASE mgdc_gsm2
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
       DROP DATABASE IF EXISTS mgdc_gsm2;

CREATE DATABASE mgdc_gsm3
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
