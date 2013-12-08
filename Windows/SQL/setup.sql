----------------------
-- CREATE SUPERUSER --
----------------------

CREATE ROLE txtdis LOGIN
  ENCRYPTED PASSWORD 'md54b0d2e10ef9ae0bf4539f9d8a83ded8b'
  SUPERUSER INHERIT CREATEDB CREATEROLE;

---------------------
-- CREATE DATABASE --
---------------------
CREATE DATABASE mgdc_smis
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

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
       
CREATE DATABASE mgdc_gsm3
  WITH OWNER = txtdis
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
