CREATE OR REPLACE FUNCTION payment(w_id INTEGER, d_id INTEGER, c_id INTEGER, c_last VARCHAR(16),h_amount NUMERIC(12,2),by_name BOOLEAN)

RETURNS INTEGER AS $$

DECLARE
  w_str1 VARCHAR(20);
  w_str2 VARCHAR(20);
  w_city VARCHAR(20); 
  w_state CHAR(2);
  w_zip CHAR(9);
  w_name VARCHAR(10);
  d_str1 VARCHAR(20);
  d_str2 VARCHAR(20);
  d_city VARCHAR(20); 
  d_state CHAR(2);
  d_zip CHAR(9);
  d_name VARCHAR(10);
  name_cnt INTEGER;
  c_first VARCHAR(16);
  c_middle CHAR(2);
  c_id INTEGER;
  c_str1 VARCHAR(20);
  c_str2 VARCHAR(20);
  c_city VARCHAR(20);
  c_state CHAR(2);
  c_zip CHAR(9);
  c_phone CHAR(16);
  c_credit CHAR(2);
  c_creditLim NUMERIC(12,2);
  c_discount NUMERIC(4,4);
  c_balance NUMERIC(12,2);
  c_since DATE;
  n INTEGER := 0;
BEGIN
  UPDATE WAREHOUSE SET wYTD = wYTD + h_amount
  WHERE WAREHOUSE.wId = w_id;

  SELECT str1, str2, city, state, zip, name
  INTO w_str1, w_str2, w_city, w_state, w_zip, w_name
  FROM WAREHOUSE W
  WHERE W.wId = w_id;

  UPDATE DISTRICT SET dYTD = dYTD + h_amount
  WHERE DISTRICT.wId = w_id AND DISTRICT.dId = d_id;

  SELECT str1, str2, city, state, zip, name
  INTO d_str1, d_str2, d_city, d_state, d_zip, d_name
  FROM DISTRICT D
  WHERE D.wId = w_id AND D.dId = d_id;

  IF (by_name) THEN
    SELECT COUNT(cId)
    INTO name_cnt
    FROM CUSTOMER C
    WHERE C.last = c_last AND C.dId = c_d_id AND 
    C.wId = c_w_id;
    
    DECLARE
    c_byname CURSOR FOR
      SELECT first, middle, cId, str1, str2, city, state, zip, 
      phone, credit, creditLim, discount,balance, since
      FROM CUSTOMER C
      WHERE C.wId = c_w_id AND C.dId = c_d_id AND 
      C.last = c_last
      ORDER BY C.first;

    OPEN c_byname;

    -- Locate midpoint customer
    IF (name_cnt%2 = 1) THEN 
      name_cnt := name_cnt + 1; -- 
    END IF;
    FOR n in 0 ..namecnt/2 LOOP
      FETCH c_byname 
      INTO c_first,c_middle,c_id, c_str1, c_str2, c_city, c_state,c_zip, c_phone, c_credit, c_creditLim, c_discount,c_balance,c_since
    END LOOP;

    CLOSE c_byname;

  ELSE
    SELECT first, middle, last, str1, str2, city, state, zip, phone, 
    credit, creditLim, discount,balance, since
    INTO c_first,c_middle,c_last, c_str1, c_str2, c_city, c_state, c_zip, c_phone, c_credit, c_creditLim, c_discount,c_balance,c_since
    FROM CUSTOMER C
    WHERE C.wId = c_w_id AND C.dId = c_d_id AND 
    C.cId = c_id;
  END IF;

  c_balance := c_balance - h_amount;
  c_credit[2] := '\0'; -- insert null char at the end

  UPDATE CUSTOMER SET YTDPaymt = YTDPaymt + h_amount
  WHERE CUSTOMER.wId = c_w_id AND CUSTOMER.dId = c_d_id AND CUSTOMER.cId = c_id;

  UPDATE CUSTOMER SET paymtCnt = paymtCnt + 1
  WHERE CUSTOMER.wId = c_w_id AND CUSTOMER.dId = c_d_id AND CUSTOMER.cId = c_id;

  IF (c_credit LIKE '%BC%') THEN 
    SELECT data into c_data
    FROM CUSTOMER C
    WHERE C.wId = c_w_id AND C.dId = c_d_id AND C.cId = c_id;

    EXEC FORMAT(c_new_data,"| %s %s %s %s %s $%s %s %s",
    c_id,c_d_id,c_w_id,d_id,w_id,h_amount,h_date, h_data);
    c_new_data := CONCAT(c_new_data,c_data);
  
    UPDATE CUSTOMER SET balance = c_balance, data = c_new_data
    WHERE CUSTOMER.wId = c_w_id AND CUSTOMER.dId = c_d_id AND 
    CUSTOMER.cId = c_id;
  ELSE
    UPDATE CUSTOMER SET balance = c_balance
    WHERE CUSTOMER.wId = c_w_id AND CUSTOMER.dId = c_d_id AND 
    CUSTOMER.cId = c_id;
  END IF;

  h_data := w_name
  h_data[10] := '\0' 
  h_data = CONCAT(h_data,d_name)
  -- delete end of string after shifting
  h_data[20]=' '; 
  h_data[21]=' ';
  h_data[22]=' '; 
  h_data[23]=' '; 

  INSERT INTO HISTORY(cDId, cWId, cId, dId, wId, date, amount, data)
  VALUES (c_d_id, c_w_id, c_id, c_d_id, c_w_id, datetime, c_amount, c_data);

  RETURN 0;
END;

$$ LANGUAGE PLpgSQL;