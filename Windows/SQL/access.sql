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
      target_header,
      target_outlet,
      target_rebate,
      template,
      item_master,
      item_tree,
      qty_per,
      price,
      volume_discount
   TO user_supply;

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
      phone_type,
      phone_number,
      purchase_detail,
      purchase_header,
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
      receiving_header_rr_id_seq,
      item_master_id_seq
   TO user_supply;

GRANT USAGE ON SEQUENCE
      contact_detail_id_seq,
      customer_master_id_seq,
      delivery_header_delivery_id_seq,
      phone_type_id_seq,
      purchase_header_purchase_id_seq,
      remittance_header_remit_id_seq,
      sales_header_sales_id_seq
   TO user_sales;

GRANT user_finance TO super_finance;
GRANT user_sales TO super_sales;
GRANT user_supply TO super_supply;
