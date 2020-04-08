CREATE OR REPLACE FUNCTION delivery (w_id INTEGER,d_id INTEGER, o_carrier_id INTEGER)
RETURNS INTEGER AS $$

DECLARE
  no_o_id INTEGER;
  c_id INTEGER;
  ol_total NUMERIC(6,2);
  date_time TIMESTAMP := current_timestamp;

BEGIN

  FOR d_id in 1 ..DIST_PER_WARE LOOP -- constant DIST_PER_WARE = 10
    
    DECLARE
    c_no CURSOR FOR 
      SELECT oId
      FROM NEW_ORDER NO
      WHERE NO.dId = d_id AND NO.wId = w_id
      ORDER BY NO.oId ASC;
  
    OPEN c_no;
    
    FETCH c_no INTO no_o_id;

    DELETE FROM NEW_ORDER WHERE CURRENT OF c_no;
    CLOSE c_no;

    SELECT cId INTO c_id FROM ORDERS O
    WHERE O.oId = no_o_id AND O.dId = d_id AND O.wId = w_id;

    UPDATE ORDERS SET carrierId = o_carrier_id
    WHERE ORDERS.oId = no_o_id AND ORDERS.dId = d_id AND ORDERS.wId = w_id;

    UPDATE ORDERLINE SET deliveryDate = date_time
    WHERE ORDERLINE.oId = no_o_id AND ORDERLINE.dId = d_id AND ORDERLINE.wId = w_id;

    SELECT SUM(amount) INTO ol_total
    FROM ORDERLINE OL
    WHERE OL.oId = no_o_id AND OL.dId = d_id AND OL.wId = w_id;

    UPDATE CUSTOMER SET balance = balance + ol_total
    WHERE CUSTOMER.cId = c_id AND CUSTOMER.dId = d_id AND CUSTOMER.wId = w_Id;

    -- added per transaction profile
    UPDATE CUSTOMER SET deliveryCnt = deliveryCnt + 1
    WHERE CUSTOMER.cId = c_id AND CUSTOMER.dId = d_id AND CUSTOMER.wId = w_Id;

  END LOOP;
  RETURN 0;
END;

$$ LANGUAGE PLpgSQL;
  
