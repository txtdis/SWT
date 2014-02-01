-------------------
-- CREATE TABLES --
-------------------

CREATE TABLE item_tier
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE uom
(
	id			 serial PRIMARY KEY,
	unit		 text,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE item_family
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	tier_id 	 int	 REFERENCES item_tier ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

ALTER SEQUENCE item_family_id_seq
 INCREMENT BY -1
 NO MINVALUE
 RESTART WITH -1;

CREATE TABLE item_type
(
	id			 serial PRIMARY KEY,
	name		 text,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

INSERT INTO item_type (name)
	 VALUES ('PURCHASED'),
			('BUNDLED'),
			('REPACKED'),
			('MADE'),
			('FREEBIE'),
			('MONETARY'),
			('FOR C/M'),
			('INCENTIVE'),
			('RETURNABLE');

CREATE TABLE item_header
(
	id				 serial PRIMARY KEY,
	short_id		 varchar (16) UNIQUE,
	name			 text UNIQUE,
	not_discounted	 boolean,
	unspsc_id		 bigint,
	type_id 		 int	 REFERENCES item_type ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 		 text DEFAULT current_user,
	time_stamp		 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE bom
(
	item_id   int	  REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	part_id   int	  REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	qty 	  numeric (8, 4),
	uom 	  int	  REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY (item_id, part_id)
);

CREATE TABLE qty_per
(
	item_id   int	  REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	qty 	  numeric (8, 4),
	uom 	  int	  REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	buy 	  boolean,
	sell	  boolean,
	report	  boolean,
	PRIMARY KEY (item_id, uom)
);

CREATE TABLE item_tree
(
	child_id	 int PRIMARY KEY,
	parent_id	 int	 REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE location
(
	id			 smallserial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE channel
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

INSERT INTO channel (id, name)
	 VALUES (0, 'ALL');

CREATE TABLE customer_header
(
	id			 serial PRIMARY KEY,
	sms_id		 text UNIQUE,
	name		 text,
	type_id 	 int	 REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
	branch_of	 int,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

INSERT INTO customer_header (sms_id, name, type_id)
	 VALUES ('MGDC', 'MAGNUM GROWTH', 1);

CREATE TABLE contact_detail
(
	id			  serial PRIMARY KEY,
	name		  text,
	surname 	  text,
	designation   text,
	customer_id   int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE count_header
(
	count_id	  serial PRIMARY KEY,
	location_id   smallint	   REFERENCES location ON UPDATE CASCADE ON DELETE CASCADE,
	taker_id	  int	  REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
	checker_id	  int	  REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
	count_date	  date,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);


CREATE TABLE quality
(
	id	   smallserial PRIMARY KEY,
	name   text UNIQUE
);


CREATE TABLE count_detail
(
	line_id    smallint,
	count_id   int	   REFERENCES count_header ON UPDATE CASCADE ON DELETE CASCADE,
	item_id    int	   REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	qc_id	   int	   REFERENCES quality ON UPDATE CASCADE ON DELETE CASCADE,
	uom 	   int	   REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	qty 	   numeric (7, 2),
	PRIMARY KEY (count_id, line_id)
);

CREATE TABLE count_completion
(
	count_date	 date PRIMARY KEY,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE count_closure
(
	count_date	 date
					 PRIMARY KEY
						  REFERENCES count_completion ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE count_adjustment
(
	count_date	 date	  REFERENCES count_completion ON UPDATE CASCADE ON DELETE CASCADE,
	item_id 	 int	 REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	qc_id		 smallint	  REFERENCES quality ON UPDATE CASCADE ON DELETE CASCADE,
	qty 		 numeric (10, 4) NOT NULL,
	reason		 text NOT NULL,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (count_date, item_id, qc_id)
);



CREATE TABLE area_tier
(
	id	   serial PRIMARY KEY,
	name   text
);

INSERT INTO area_tier (id, name)
	 VALUES (0, $$COUNTRY$$);


CREATE TABLE area
(
	id		  serial PRIMARY KEY,
	name	  text,
	tier_id   int	  REFERENCES area_tier ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO area (id, name, tier_id)
	 VALUES (0, $$PHILIPPINES$$, 0);

CREATE TABLE area_tree
(
	parent_id	int 	REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
	child_id	int 	REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE address
(
	customer_id   int
					  PRIMARY KEY
						   REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	street		  text,
	district	  int	  REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
	city		  int	  REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
	province	  int	  REFERENCES area ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE price_tier
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	start_date	 date DEFAULT current_date,
	end_date	 date,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

INSERT INTO price_tier (id, name)
	 VALUES (0, 'PURCHASE'), (DEFAULT, 'WHOLESALE'), (DEFAULT, 'RETAIL');

CREATE TABLE channel_price_tier
(
	channel_id	 int	 REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
	tier_id 	 int	 REFERENCES price_tier ON UPDATE CASCADE ON DELETE CASCADE,
	family_id	 int	 REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	start_date	 date DEFAULT current_date,
	end_date	 date,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY
		(channel_id,
		 tier_id,
		 family_id,
		 start_date)
);

CREATE TABLE price
(
	item_id 	 int	 REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	tier_id 	 int	 REFERENCES price_tier ON UPDATE CASCADE ON DELETE CASCADE,
	price		 numeric (10, 2) NOT NULL,
	start_date	 date DEFAULT current_date,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (item_id, tier_id, start_date)
);

CREATE TABLE credit
(
	customer_id    int	   REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	credit_limit   int,
	term		   int,
	grace_period   int,
	start_date	   date DEFAULT current_date,
	user_id 	   text DEFAULT current_user,
	time_stamp	   timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (customer_id, start_date)
);

CREATE TABLE discount
(
	id			  int PRIMARY KEY,
	customer_id   int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	family_id	  int	  REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	level_1 	  numeric (5, 4) NOT NULL,
	level_2 	  numeric (5, 4),
	start_date	  date DEFAULT current_date,
	end_date	  date,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE volume_discount
(
	item_id 	 int	 REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	uom 		 int	 REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	per_qty 	 int,
	less		 numeric (10, 2),
	channel_id	 int	 REFERENCES channel ON UPDATE CASCADE ON DELETE CASCADE,
	start_date	 date DEFAULT current_date,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY
		(item_id,
		 per_qty,
		 channel_id,
		 start_date)
);

CREATE TABLE target_type
(
	id	   serial PRIMARY KEY,
	name   text
);

CREATE TABLE target_header
(
	target_id	  serial PRIMARY KEY,
	type_id 	  int	  REFERENCES target_type ON UPDATE CASCADE ON DELETE CASCADE,
	category_id   int	  REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	start_date	  date NOT NULL,
	end_date	  date NOT NULL,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE target_rebate
(
	target_id		  int	  REFERENCES target_header ON UPDATE CASCADE ON DELETE CASCADE,
	product_line_id   int	  REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	value			  numeric (7, 2) NOT NULL,
	PRIMARY KEY (target_id, product_line_id)
);

CREATE TABLE target_outlet
(
	target_id		  int	  REFERENCES target_header ON UPDATE CASCADE ON DELETE CASCADE,
	outlet_id		  int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	product_line_id   int	  REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	qty 			  numeric (7, 2) NOT NULL,
	PRIMARY KEY (target_id, outlet_id, product_line_id)
);

CREATE TABLE target_siv
(
	target_date 	  date,
	product_line_id   int	  REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	qty 			  numeric (7, 2) NOT NULL,
	PRIMARY KEY (target_date, product_line_id)
);

CREATE TABLE target_stock_days
(
	item_family_id	 int PRIMARY KEY	 REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	days			 int NOT NULL
);

CREATE TABLE vendor_specific
(
	vendor_id	int PRIMARY KEY 	REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	lead_time	int NOT NULL,
	self_id 	text,
	note		text
);

CREATE TABLE invoice_header
(
	invoice_id	   int,
	series		   text DEFAULT ' ',
	ref_id		   int,
	invoice_date   date DEFAULT current_date,
	customer_id    int	   REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	actual		   numeric (10, 2),
	payment 	   numeric (10, 2),
	user_id 	   text DEFAULT current_user,
	time_stamp	   timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (invoice_id, series)
);

CREATE TABLE invoice_detail
(
	invoice_id	 int,
	series		 text DEFAULT ' ',
	line_id 	 smallint NOT NULL,
	item_id 	 int NOT NULL,
	qty 		 numeric (7, 2),
	uom 		 int	 REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE DEFAULT 0,
	PRIMARY KEY (invoice_id, item_id, series),
	FOREIGN KEY
		(invoice_id, series)
		 REFERENCES invoice_header (invoice_id, series) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE invoice_booklet
(
	start_id	 int,
	end_id		 int,
	series		 text DEFAULT ' ',
	out_date	 date DEFAULT current_date,
	rep_id		 int	 REFERENCES contact_detail ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (start_id, end_id, series)
);

CREATE TABLE sales_header
(
	sales_id	  serial PRIMARY KEY,
	sales_date	  date DEFAULT current_date,
	customer_id   int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE sales_detail
(
	sales_id   int	   REFERENCES sales_header ON UPDATE CASCADE ON DELETE CASCADE,
	line_id    smallint NOT NULL,
	item_id    int NOT NULL,
	qty 	   numeric (7, 2) NOT NULL,
	uom 	   int	   REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE DEFAULT 0,
	PRIMARY KEY (sales_id, item_id)
);

CREATE TABLE purchase_header
(
	purchase_id   serial,
	rev_id		  smallint DEFAULT 0,
	sales_date	  date DEFAULT current_date,
	customer_id   int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (purchase_id, rev_id)
);

CREATE TABLE purchase_detail
(
	sales_id   int,
	line_id    smallint NOT NULL,
	item_id    int NOT NULL,
	qty 	   numeric (7, 2) NOT NULL,
	uom 	   int DEFAULT 0	 REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY (sales_id, item_id)
);


CREATE TABLE delivery_header
(
	delivery_id 	serial,
	rev_id			smallint DEFAULT 0,
	ref_id			int,
	delivery_date	date DEFAULT current_date,
	customer_id 	int 	REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	actual			numeric (10, 2) NOT NULL,
	payment 		numeric (10, 2),
	user_id 		text DEFAULT current_user,
	time_stamp		timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (delivery_id, rev_id)
);

CREATE TABLE delivery_detail
(
	delivery_id   int,
	rev_id		  smallint DEFAULT 0,
	line_id 	  smallint NOT NULL,
	item_id 	  int	  REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	qty 		  numeric (7, 2) NOT NULL,
	uom 		  int DEFAULT 0 	REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	PRIMARY KEY (delivery_id, rev_id, item_id),
	FOREIGN KEY
		(delivery_id, rev_id)
		 REFERENCES delivery_header (delivery_id, rev_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE receiving_header
(
	receiving_id	 serial PRIMARY KEY,
	partner_id		 int	 REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	receiving_date	 date DEFAULT current_date,
	ref_id			 int,
	user_id 		 text DEFAULT current_user,
	time_stamp		 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE receiving_detail
(
	receiving_id   int	   REFERENCES receiving_header ON UPDATE CASCADE ON DELETE CASCADE,
	line_id 	   int,
	item_id 	   int	   REFERENCES item_header ON UPDATE CASCADE ON DELETE CASCADE,
	expiry		   date,
	qc_id		   int,
	uom 		   int	   REFERENCES uom ON UPDATE CASCADE ON DELETE CASCADE,
	qty 		   numeric (7, 2),
	PRIMARY KEY (receiving_id, line_id)
);

CREATE TABLE remit_header
(
	remit_id	 serial UNIQUE,
	bank_id 	 int	 REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	remit_date	 date,
	remit_time	 time WITH TIME ZONE,
	ref_id		 int,
	or_id		 int,
	total		 numeric (10, 2),
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY
		(bank_id,
		 remit_date,
		 remit_time,
		 ref_id)
);

CREATE TABLE remit_detail
(
	remit_id   int	   REFERENCES remit_header (remit_id) ON UPDATE CASCADE ON DELETE CASCADE,
	line_id    smallint,
	order_id   int,
	series	   text DEFAULT ' ',
	payment    numeric (10, 2),
	PRIMARY KEY (remit_id, order_id, series)
);

CREATE TABLE transmittal_header
(
	transmit_date	date PRIMARY KEY,
	total			numeric (10, 2),
	user_id 		text DEFAULT current_user,
	time_stamp		timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE transmittal_detail
(
	transmit_date	date	 REFERENCES transmittal_header ON UPDATE CASCADE ON DELETE CASCADE,
	line_id 		smallint NOT NULL,
	remit_id		int

							REFERENCES remit_header (remit_id) ON UPDATE CASCADE ON DELETE CASCADE,
	remit_date		date NOT NULL,
	customer_id 	int 	REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	type			text NOT NULL,
	reference_id	int NOT NULL,
	amount			numeric (10, 2),
	PRIMARY KEY
		(transmit_date,
		 remit_id,
		 customer_id,
		 type,
		 reference_id)
);

CREATE TABLE remit_cancellation
(
	remit_id	 int
					 PRIMARY KEY
						  REFERENCES remit_header (remit_id) ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE route
(
	id			 smallserial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE account
(
	customer_id   int	  REFERENCES customer_header ON UPDATE CASCADE ON DELETE CASCADE,
	route_id	  int	  REFERENCES route ON UPDATE CASCADE ON DELETE CASCADE,
	start_date	  date DEFAULT current_date,
	user_id 	  text DEFAULT current_user,
	time_stamp	  timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (customer_id, route_id, start_date)
);

CREATE TABLE sales_print_out
(
	sales_id	 int PRIMARY KEY	 REFERENCES sales_header ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE template
(
	name		 text,
	file		 bytea,
	start_date	 date DEFAULT current_date,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (name, start_date)
);

CREATE TABLE default_number
(
	name		 text,
	value		 numeric (5, 4) NOT NULL,
	start_date	 date DEFAULT 'epoch',
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (name, start_date)
);

CREATE TABLE default_text
(
	name		 text,
	value		 text NOT NULL,
	start_date	 date DEFAULT 'epoch',
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (name, start_date)
);

INSERT INTO default_text (name, value)
	 VALUES ($$VERSION$$, $$0.9.4.3$$), ($$CURRENCY$$, $$₱$$);

CREATE TABLE default_date
(
	name		 text,
	value		 date NOT NULL,
	start_date	 date DEFAULT 'epoch',
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp,
	PRIMARY KEY (name, start_date)
);

INSERT INTO default_date (name, value)
		 VALUES ($$NO-S/O-WITH-OVERDUE CUTOFF$$, '2013-05-01'),
				($$S/I-MUST-HAVE-S/O CUTOFF$$, '2013-06-30'),
				($$CLOSED-DSR-BEFORE-S/O CUTOFF$$, '2013-08-13');

CREATE TABLE target_extra_rebate
(
	target_id				   int	   REFERENCES target_header ON UPDATE CASCADE ON DELETE CASCADE,
	trigger_product_line_id    int	   REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	affected_product_line_id   int	   REFERENCES item_family ON UPDATE CASCADE ON DELETE CASCADE,
	value					   numeric (7, 2) NOT NULL,
	PRIMARY KEY (target_id, trigger_product_line_id, affected_product_line_id)
);

CREATE TABLE phone_type
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

INSERT INTO phone_type (name)
	 VALUES ('CELL'),
			('WORK'),
			('FAX'),
			('HOME');

CREATE TABLE phone_number
(
	number		 bigint NOT NULL PRIMARY KEY,
	contact_id	 integer,
	type_id 	 integer DEFAULT 1	   REFERENCES phone_type ON UPDATE CASCADE ON DELETE CASCADE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE delivery
(
	id			 serial PRIMARY KEY,
	name		 text UNIQUE,
	user_id 	 text DEFAULT current_user,
	time_stamp	 timestamp WITH TIME ZONE DEFAULT current_timestamp
);

CREATE TABLE version
(
    latest       text PRIMARY KEY,
    user_id      text DEFAULT current_user,
    time_stamp   timestamp WITH TIME ZONE DEFAULT current_timestamp
);


INSERT INTO version (latest)
     VALUES ('0.9.5.2');

CREATE TABLE monetary
(
    id       serial PRIMARY KEY,
    name     text UNIQUE NOT NULL,
    user_id      text DEFAULT current_user,
    time_stamp   timestamp WITH TIME ZONE DEFAULT current_timestamp
);


INSERT INTO monetary (name)
     VALUES ('CREDIT MEMO'),
            ('EXPENSE'),
            ('SALARY CREDIT'),
            ('SALARY DEDUCTION');

--------------------
-- CREATE INDEXES --
--------------------

CREATE INDEX ON remit_detail(order_id, series);
CREATE INDEX ON receiving_detail(receiving_id, qc_id, item_id, uom);
CREATE INDEX ON invoice_detail(invoice_id, series, item_id, uom);
CREATE INDEX ON delivery_detail(delivery_id, item_id, uom);
CREATE INDEX ON receiving_header(receiving_id, receiving_date);
CREATE INDEX ON invoice_header(invoice_id, series, customer_id, invoice_date);
CREATE INDEX ON delivery_header(delivery_id, delivery_date);
