BEGIN TRANSACTION;

ALTER TABLE target_rebate DROP CONSTRAINT target_rebate_target_id_fkey;
ALTER TABLE target_rebate
 ADD FOREIGN KEY (target_id)
 REFERENCES target_header (target_id)
 ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE target_outlet DROP CONSTRAINT target_outlet_target_id_fkey;
ALTER TABLE target_outlet
 ADD FOREIGN KEY (target_id)
 REFERENCES target_header (target_id)
 ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX ON invoice_header (invoice_date);

END TRANSACTION;
