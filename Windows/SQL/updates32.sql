BEGIN TRANSACTION;
ALTER TABLE receiving_header RENAME COLUMN rr_id TO receiving_id;
ALTER TABLE receiving_header RENAME COLUMN rr_date TO receiving_date;
ALTER TABLE receiving_detail RENAME COLUMN rr_id TO receiving_id;

ALTER TABLE sales_detail DROP CONSTRAINT sales_detail_sales_id_fkey;
ALTER TABLE sales_detail
 ADD FOREIGN KEY (sales_id)
 REFERENCES sales_header (sales_id)
 ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE purchase_detail DROP CONSTRAINT purchase_detail_purchase_id_fkey;
ALTER TABLE purchase_detail
 ADD FOREIGN KEY (purchase_id)
 REFERENCES purchase_header (purchase_id)
 ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE receiving_detail DROP CONSTRAINT receiving_detail_rr_id_fkey;
ALTER TABLE receiving_detail
 ADD FOREIGN KEY (receiving_id)
 REFERENCES receiving_header (receiving_id)
 ON DELETE CASCADE ON UPDATE CASCADE;

END TRANSACTION;
