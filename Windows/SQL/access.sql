----------
-- LIST --
----------

-- TABLES

--  SELECT tablename
--	FROM pg_tables
--   WHERE schemaname = 'public'
--ORDER BY tablename;
--
--GRANT SELECT
--   ON account,
--	  address,
--	  area,
--	  area_tier,
--	  area_tree,
--	  bom,
--	  channel,
--	  channel_price_tier,
--	  contact_detail,
--	  count_adjustment,
--	  count_closure,
--	  count_detail,
--	  count_header,
--	  credit_detail,
--	  customer_master,
--	  default_number,
--	  default_text,
--	  delivery_detail,
--	  delivery_header,
--	  discount,
--	  invoice_booklet,
--	  invoice_detail,
--	  invoice_header,
--	  irregular_log,
--	  item_family,
--	  item_master,
--	  item_tier,
--	  item_tree,
--	  item_type,
--	  location,
--	  phone_number,
--	  phone_type,
--	  price,
--	  price_tier,
--	  purchase_category,
--	  purchase_detail,
--	  purchase_header,
--	  qty_per,
--	  quality,
--	  receiving_detail,
--	  receiving_header,
--	  remittance_detail,
--	  remittance_header,
--	  route,
--	  sales_detail,
--	  sales_header,
--	  sales_print_out,
--	  settings,
--	  sms_log,
--	  system_user,
--	  target_header,
--	  target_outlet,
--	  target_rebate,
--	  target_siv,
--	  target_stock_days,
--	  target_type,
--	  template,
--	  uom,
--	  vendor_specific,
--	  volume_discount
--   TO all;
--
---- SEQUENCES
--
--SELECT relname
--  FROM pg_class
-- WHERE relkind = 'S';
--
--GRANT USAGE
--   ON SEQUENCE count_header_count_id_seq,
--	  remittance_header_remit_id_seq,
--	  irregular_log_id_seq,
--	  customer_master_id_seq,
--	  sales_header_sales_id_seq,
--	  purchase_header_purchase_id_seq,
--	  delivery_header_delivery_id_seq,
--	  item_master_id_seq,
--	  item_family_id_seq,
--	  area_id_seq,
--	  area_tier_id_seq,
--	  channel_id_seq,
--	  contact_detail_id_seq,
--	  item_tier_id_seq,
--	  item_type_id_seq,
--	  location_id_seq,
--	  receiving_header_rr_id_seq,
--	  phone_type_id_seq,
--	  price_tier_id_seq,
--	  quality_id_seq,
--	  route_id_seq,
--	  sms_log_id_seq,
--	  target_header_target_id_seq,
--	  target_type_id_seq,
--	  uom_id_seq
--   TO all;

-----------------------------------
-- SELECT ON ALL TABLES & VIEWS  --
-----------------------------------

GRANT SELECT
   ON ALL TABLES IN SCHEMA public
   TO user_sales,
      user_supply,
      user_finance;
	  
--------------------------------
-- INSERT ON SPECIFIC TABLES  --
--------------------------------

GRANT INSERT
   ON count_adjustment,
      count_closure,
      count_detail,
      count_header,
      receiving_detail,
      receiving_header,
      template
   TO user_supply;

GRANT INSERT
   ON item_master,
      item_tree,
      qty_per,
      price,
      volume_discount
   TO super_supply;

GRANT INSERT
   ON account,
      address,
      contact_detail,
      credit_detail,
      customer_master,
      delivery_detail,
      delivery_header,
      discount,
      invoice_booklet,
      invoice_detail,
      invoice_header,
      phone_number,
      remittance_detail,
      remittance_header,
      sales_detail,
      sales_header,
      sales_print_out,
      template
   TO user_sales;

------------------------
-- USAGE ON SEQUENCES --
------------------------

GRANT USAGE ON SEQUENCE
      count_header_count_id_seq,
      receiving_header_rr_id_seq
   TO user_supply;

GRANT USAGE ON SEQUENCE item_master_id_seq TO super_supply;

GRANT USAGE ON SEQUENCE
      contact_detail_id_seq,
      customer_master_id_seq,
      delivery_header_delivery_id_seq,
      remittance_header_remit_id_seq,
      sales_header_sales_id_seq
   TO user_sales;
