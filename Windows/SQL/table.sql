-------------------
-- CREATE TABLES --
-------------------

CREATE TABLE item_tier
(
   id           SERIAL PRIMARY KEY,
   name         TEXT UNIQUE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE uom
(
   id           SERIAL PRIMARY KEY,
   unit         TEXT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE item_family
(
   id           SERIAL PRIMARY KEY,
   name         TEXT UNIQUE,
   uom          INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   tier_id      INT    REFERENCES item_tier ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE item_type
(
   id           SERIAL PRIMARY KEY,
   name         TEXT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE item_master
(
   id               INT PRIMARY KEY,
   short_id         VARCHAR (16) UNIQUE,
   name             TEXT UNIQUE,
   not_discounted   BOOLEAN,
   unspsc_id        BIGINT,
   type_id          INT

                          REFERENCES item_type ON UPDATE CASCADE ON DELETE CASCADE,
   user_id          TEXT DEFAULT CURRENT_USER,
   time_stamp       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bom
(
   item_id   INT

                   REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   part_id   INT

                   REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   qty       NUMERIC (8, 4),
   uom       INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   is_free   BOOLEAN,
   PRIMARY KEY (item_id, part_id)
);

CREATE TABLE qty_per
(
   item_id   INT

                   REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   qty       NUMERIC (8, 4),
   uom       INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   buy       BOOLEAN,
   sell      BOOLEAN,
   report    BOOLEAN,
   PRIMARY KEY (item_id, uom)
);

CREATE TABLE item_tree
(
   child_id     INT PRIMARY KEY,
   parent_id    INT

                      REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE location
(
   id           smallserial PRIMARY KEY,
   name         TEXT UNIQUE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE channel
(
   id           SERIAL PRIMARY KEY,
   name         TEXT UNIQUE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_master
(
   id           SERIAL PRIMARY KEY,
   sms_id       TEXT UNIQUE,
   name         TEXT,
   type_id      INT    REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
   branch_of    INT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contact_detail
(
   id            SERIAL PRIMARY KEY,
   name          TEXT,
   surname       TEXT,
   designation   TEXT,
   customer_id   INT

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE count_header
(
   count_id      SERIAL PRIMARY KEY,
   location_id   SMALLINT

                       REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
   taker_id      INT

                       REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
   checker_id    INT

                       REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
   count_date    DATE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE count_detail
(
   count_id   INT

                    REFERENCES count_header ON UPDATE CASCADE ON DELETE CASCADE,
   loc        TEXT

                    REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   qc_id      INT    REFERENCES quality ON UPDATE CASCADE ON DELETE CASCADE,
   uom        INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   qty        NUMERIC (7, 2),
   PRIMARY KEY (count_id, line_id)
);

CREATE TABLE count_closure
(
   count_date   DATE PRIMARY KEY,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE count_adjustment
(
   count_date      DATE

                         REFERENCES count_closure ON UPDATE CASCADE ON DELETE CASCADE,
   item_id         INT,
   location_id     INT

                         REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
   qc_id           SMALLINT

                         REFERENCES quality ON UPDATE CASCADE ON DELETE CASCADE,
   expiry          DATE,
   qty             NUMERIC (10, 4),
   reason          TEXT,
   approved_by     TEXT

                         REFERENCES system_user ON UPDATE CASCADE ON DELETE CASCADE,
   approval_date   DATE,
   user_id         TEXT DEFAULT CURRENT_USER,
   time_stamp      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (count_date, item_id, qc_id, expiry)
);

CREATE TABLE area_tier
(
   id     SERIAL PRIMARY KEY,
   name   TEXT
);

CREATE TABLE area
(
   id        SERIAL PRIMARY KEY,
   name      TEXT,
   tier_id   INT    REFERENCES area_tier ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE area_tree
(
   parent_id   INT    REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
   child_id    INT    REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
   PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE address
(
   customer_id   INT
                    PRIMARY KEY

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   street        TEXT,
   district      INT    REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
   city          INT    REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
   province      INT    REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE SYSTEM_USER
(
   system_id    TEXT PRIMARY KEY,
   PASSWORD     TEXT,
   contact_id   INT

                      REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE price_tier
(
   id           SERIAL PRIMARY KEY,
   name         TEXT UNIQUE,
   start_date   DATE DEFAULT CURRENT_DATE,
   end_date     DATE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE channel_price_tier
(
   channel_id   INT    REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
   tier_id      INT

                      REFERENCES price_tier ON UPDATE CASCADE ON DELETE CASCADE,
   start_date   DATE DEFAULT CURRENT_DATE,
   end_date     DATE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (channel_id, tier_id, start_date)
);

CREATE TABLE price
(
   item_id      INT

                      REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   tier_id      INT    REFERENCES price_tier ON UPDATE CASCADE ON DELETE CASCADE,
   price        NUMERIC (10, 2) NOT NULL,
   start_date   DATE DEFAULT CURRENT_DATE,
   end_date     DATE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (item_id, tier_id, start_date)
);

CREATE TABLE credit_detail
(
   customer_id    INT

                        REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   credit_limit   INT,
   term           INT,
   grace_period   INT,
   start_date     DATE DEFAULT CURRENT_DATE,
   user_id        TEXT DEFAULT CURRENT_USER,
   time_stamp     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (customer_id, start_date)
);

CREATE TABLE discount
(
   id            INT PRIMARY KEY,
   customer_id   INT

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   family_id     INT

                       REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   level_1       NUMERIC (5, 4) NOT NULL,
   level_2       NUMERIC (5, 4),
   start_date    DATE DEFAULT CURRENT_DATE,
   end_date      DATE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE volume_discount
(
   item_id      INT

                      REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   uom          INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   per_qty      INT,
   less         NUMERIC (10, 2),
   channel_id   INT    REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
   start_date   DATE DEFAULT CURRENT_DATE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (item_id, per_qty, channel_id, start_date)
);

CREATE TABLE target_header
(
   target_id     SERIAL PRIMARY KEY,
   type_id       INT

                       REFERENCES target_type ON UPDATE CASCADE ON DELETE CASCADE,
   category_id   INT

                       REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   start_date    DATE NOT NULL,
   end_date      DATE NOT NULL,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE target_rebate
(
   target_id         INT

                           REFERENCES target_header ON UPDATE CASCADE ON DELETE CASCADE,
   product_line_id   INT

                           REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   value             NUMERIC (7, 2) NOT NULL,
   PRIMARY KEY (target_id, product_line_id)
);

CREATE TABLE target_outlet
(
   target_id         INT

                           REFERENCES target_header ON UPDATE CASCADE ON DELETE CASCADE,
   outlet_id         INT

                           REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   product_line_id   INT

                           REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   qty               NUMERIC (7, 2) NOT NULL,
   PRIMARY KEY (target_id, outlet_id, product_line_id)
);

CREATE TABLE target_type
(
   id     SERIAL PRIMARY KEY,
   name   TEXT
);

CREATE TABLE target_siv
(
   target_date       DATE,
   product_line_id   INT

                           REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   qty               NUMERIC (7, 2) NOT NULL,
   PRIMARY KEY (target_date, product_line_id)
);

CREATE TABLE target_stock_days
(
   item_family_id   INT
                       PRIMARY KEY

                          REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
   days             INT NOT NULL
);

CREATE TABLE vendor_specific
(
   vendor_id   INT
                  PRIMARY KEY

                     REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   lead_time   INT NOT NULL,
   self_id     TEXT,
   note        TEXT
);

CREATE TABLE invoice_header
(
   invoice_id     INT,
   series         TEXT DEFAULT ' ',
   ref_id         INT,
   invoice_date   DATE DEFAULT CURRENT_DATE,
   customer_id    INT

                        REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   actual         NUMERIC (10, 2),
   payment        NUMERIC (10, 2),
   user_id        TEXT DEFAULT CURRENT_USER,
   time_stamp     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (invoice_id, series)
);

CREATE TABLE invoice_detail
(
   invoice_id   INT,
   series       TEXT DEFAULT ' ',
   line_id      SMALLINT NOT NULL,
   item_id      INT NOT NULL,
   qty          NUMERIC (7, 2),
   uom          INT
                       REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE
                   DEFAULT 0,
   PRIMARY KEY (invoice_id, item_id, series),
   FOREIGN KEY
      (invoice_id, series)

      REFERENCES invoice_header (invoice_id, series) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE invoice_booklet
(
   start_id     INT,
   end_id       INT,
   series       TEXT DEFAULT ' ',
   out_date     DATE DEFAULT CURRENT_DATE,
   rep_id       INT

                      REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (start_id, end_id, series)
);

CREATE TABLE sales_header
(
   sales_id      SERIAL PRIMARY KEY,
   sales_date    DATE DEFAULT CURRENT_DATE,
   customer_id   INT

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_detail
(
   sales_id   INT

                    REFERENCES sales_header ON UPDATE CASCADE ON DELETE CASCADE,
   line_id    SMALLINT NOT NULL,
   item_id    INT NOT NULL,
   qty        NUMERIC (7, 2) NOT NULL,
   uom        INT
                     REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE
                 DEFAULT 0,
   PRIMARY KEY (sales_id, item_id)
);

CREATE TABLE sales_cancellation
(
   sales_id                 INT
                               PRIMARY KEY

                                  REFERENCES sales_header ON UPDATE CASCADE ON DELETE CASCADE,
   reason                   TEXT NOT NULL,
   canceller                TEXT DEFAULT CURRENT_USER,
   cancellation_timestamp   TIMESTAMP WITH TIME ZONE
                               DEFAULT CURRENT_TIMESTAMP,
   confirmer                TEXT,
   confirmation_timestamp   TIMESTAMP WITH TIME ZONE
);


CREATE TABLE purchase_header
(
   sales_id      SERIAL,
   rev_id        SMALLINT DEFAULT 0,
   sales_date    DATE DEFAULT CURRENT_DATE,
   customer_id   INT

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (sales_id, rev_id)
);

CREATE TABLE purchase_detail
(
   sales_id   INT,
   rev_id     SMALLINT DEFAULT 0,
   line_id    SMALLINT NOT NULL,
   item_id    INT NOT NULL,
   qty        NUMERIC (7, 2) NOT NULL,
   uom        INT
                     REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE
                 DEFAULT 0,
   PRIMARY KEY (sales_id, rev_id, item_id),
   FOREIGN KEY
      (sales_id, rev_id)

      REFERENCES sales_header (sales_id, rev_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE delivery_header
(
   delivery_id     SERIAL,
   rev_id          SMALLINT DEFAULT 0,
   ref_id          INT,
   delivery_date   DATE DEFAULT CURRENT_DATE,
   customer_id     INT

                         REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   actual          NUMERIC (10, 2) NOT NULL,
   payment         NUMERIC (10, 2),
   user_id         TEXT DEFAULT CURRENT_USER,
   time_stamp      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (delivery_id, rev_id)
);

CREATE TABLE delivery_detail
(
   delivery_id   INT,
   rev_id        SMALLINT DEFAULT 0,
   line_id       SMALLINT NOT NULL,
   item_id       INT

                       REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   qty           NUMERIC (7, 2) NOT NULL,
   uom           INT
                        REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE
                    DEFAULT 0,
   PRIMARY KEY (delivery_id, rev_id, item_id),
   FOREIGN KEY
      (delivery_id, rev_id)

      REFERENCES delivery_header (delivery_id, rev_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE issuance_header
(
   is_id        SERIAL PRIMARY KEY,
   loc_id       INT    REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
   is_date      DATE,
   ref_id       INT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issuance_detail
(
   is_id     INT

                   REFERENCES receiving_header ON UPDATE CASCADE ON DELETE CASCADE,
   line_id   INT,
   item_id   INT

                   REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   expiry    DATE,
   qc_id     INT,
   loc_id    INT    REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
   uom       INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   qty       NUMERIC (7, 2),
   PRIMARY KEY (is_id, line_id)
);

CREATE TABLE receiving_header
(
   rr_id        SERIAL PRIMARY KEY,
   partner_id   INT

                      REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   rr_date      DATE DEFAULT CURRENT_DATE,
   ref_id       INT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE receiving_detail
(
   rr_id     INT

                   REFERENCES receiving_header ON UPDATE CASCADE ON DELETE CASCADE,
   line_id   INT,
   item_id   INT

                   REFERENCES item_master ON UPDATE CASCADE ON DELETE CASCADE,
   expiry    DATE,
   qc_id     INT,
   loc_id    INT    REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
   uom       INT    REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
   qty       NUMERIC (7, 2),
   PRIMARY KEY (rr_id, line_id)
);

CREATE TABLE remittance_header
(
   remit_id     SERIAL UNIQUE,
   bank_id      INT

                      REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   remit_date   DATE,
   remit_time   TIME WITH TIME ZONE,
   ref_id       INT,
   or_id        INT,
   total        NUMERIC (10, 2),
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (bank_id, remit_date, remit_time, ref_id)
);

CREATE TABLE remittance_detail
(
   remit_id   INT

                    REFERENCES remittance_header (remit_id) ON UPDATE CASCADE ON DELETE CASCADE,
   line_id    SMALLINT,
   order_id   INT,
   series     TEXT DEFAULT ' ',
   payment    NUMERIC (10, 2),
   PRIMARY KEY (remit_id, order_id, series)
);

CREATE TABLE bounced_check
(
   remit_id     INT
                   PRIMARY KEY

                      REFERENCES remittance_header (remit_id) ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ON remittance_detail(order_id, series)



CREATE TABLE account
(
   rep_id        INT

                       REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
   customer_id   INT

                       REFERENCES customer_master ON UPDATE CASCADE ON DELETE CASCADE,
   start_date    DATE DEFAULT CURRENT_DATE,
   user_id       TEXT DEFAULT CURRENT_USER,
   time_stamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (rep_id, customer_id, start_date)
)



CREATE TABLE sales_print_out
(
   sales_id     INT
                   PRIMARY KEY

                      REFERENCES sales_header ON UPDATE CASCADE ON DELETE CASCADE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TEMPLATE
(
   name         TEXT,
   file         bytea,
   start_date   DATE DEFAULT CURRENT_DATE,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (name, start_date)
);

CREATE TABLE irregular_log
(
   id           SERIAL PRIMARY KEY,
   activity     TEXT,
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE default_number
(
   name         TEXT,
   value        NUMERIC (5, 4) NOT NULL,
   start_date   DATE DEFAULT 'epoch',
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (name, start_date)
);

CREATE TABLE default_text
(
   name         TEXT,
   value        TEXT NOT NULL,
   start_date   DATE DEFAULT 'epoch',
   user_id      TEXT DEFAULT CURRENT_USER,
   time_stamp   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (name, start_date)
);

CREATE TABLE purchase_category (name TEXT PRIMARY KEY);

CREATE TABLE quality
(
   id     SMALLSERIAL PRIMARY KEY,
   name   TEXT UNIQUE
);
