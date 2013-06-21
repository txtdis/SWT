-------------------
--- CREATE ROLES --
-------------------

CREATE ROLE user_finance NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOLOGIN;

CREATE ROLE user_sales NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT NOLOGIN;

CREATE ROLE user_supply NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT NOLOGIN;

CREATE ROLE super_finance NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOLOGIN;

CREATE ROLE super_sales NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOLOGIN;

CREATE ROLE super_supply NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOLOGIN;

CREATE ROLE sys_admin NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOLOGIN;

GRANT user_finance
   TO super_finance;

GRANT user_sales
   TO super_sales;

GRANT user_supply
   TO super_supply;

GRANT super_sales,
      super_supply,
      super_finance
   TO sys_admin;