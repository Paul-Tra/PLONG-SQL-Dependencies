CREATE OR REPLACE FUNCTION osstat(by_name BOOLEAN, c_last VARCHAR(16), d_Id INTEGER, w_Id INTEGER, c_id INTEGER) 

RETURNS INTEGER AS $$

DECLARE
  name_cnt INTEGER;
  o_id INTEGER;
  o_carrier_id INTEGER;
  ent_date DATE;
  c_balance NUMERIC(12,2);
  c_first VARCHAR(16);
  c_middle CHAR(2);
  c_last VARCHAR(16);
  i INTEGER := 0;

BEGIN
  IF (by_name) THEN
    SELECT COUNT(cId) INTO name_cnt
    FROM CUSTOMER C
    WHERE C.last = c_last AND C.dId = d_Id AND C.wId = w_Id;

    DECLARE 
    c_name CURSOR FOR 
      SELECT balance, first, middle, cId
      FROM CUSTOMER C
      WHERE C.last = c_last AND C.dId = d_id AND C.wId = w_id
      ORDER BY C.first;

    OPEN c_name;

    -- locate midpoint customer
    IF (name_cnt%2 = 1) THEN
      name_cnt := name_cnt + 1;
    END IF;
    FOR n in 0 ..name_cnt/2 LOOP
      FETCH c_name 
      INTO c_balance, c_first, c_middle, c_id
    END LOOP;

    CLOSE c_name;

  ELSE
    SELECT balance, first, middle, last
    INTO c_balance, c_first, c_middle, c_last
    FROM CUSTOMER C
    WHERE C.cId = c_id AND C.dId = d_id AND C.wId = w_Id;
  END IF;

  SELECT oId, carrierId, entryDate
  INTO o_id, o_carrier_id, ent_date
  FROM ORDERS O
  ORDER BY O.oId DESC;

  DECLARE
  c_line CURSOR FOR
    SELECT iId, supplyWId, quantity, amount, deliveryDate
    FROM ORDERLINE OL
    WHERE OL.oId = o_id AND OL.dId = d_id AND OL.wId = w_id;

  OPEN c_line;
  
  WHILE (sql_notfound = FALSE) 
    i := i + 1;
    FETCH c_line 
    INTO ol_i_id[i], ol_supply_w_id[i], ol_quantity[i], ol_amount[i], ol_delivery_d[i]
  END WHILE;

  CLOSE c_line;
  RETURN 0;
END;
$$ LANGUAGE PLpgSQL;
