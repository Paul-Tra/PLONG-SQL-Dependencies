CREATE OR REPLACE FUNCTION neworder (w_id INTEGER,d_id INTEGER,
c_Id INTEGER, ol_cnt NUMERIC(2), ol_i_id INTEGER, ol_supply_w_id,
ol_quantity NUMERIC(2))

RETURNS VOID AS $$

DECLARE
  c_discount NUMERIC(4,4);
  c_last VARCHAR(16);
  c_credit CHAR(2);
  w_tax NUMERIC(4,4);
  d_next_o_id INTEGER;
  d_tax NUMERIC(4,4), 
  i_price NUMERIC(5,2); 
  i_name VARCHAR(24);
  i_data VARCHAR(50);
  o_id INTEGER;
  ol_amount NUMERIC(4,4);

BEGIN
  SELECT C.discount, C.last, C.credit, W.tax 
  INTO c_discount, c_last, c_credit, w_tax
  FROM CUSTOMER C, WAREHOUSE W
  WHERE W.wId = w_id AND C.wId = W.wId AND C.dId = d_id AND 
  C.cId = c_Id;

  SELECT nextOrdId, tax
  INTO d_next_o_id, d_tax
  FROM DISTRICT D
  WHERE D.dId = d_id AND D.wId = w_id;

  UPDATE DISTRICT SET nextOrdId = d_next_o_id + 1
  WHERE DISTRICT.dId = d_id AND DISTRICT.wId = w_id;

  o_id := d_next_o_id;

  INSERT INTO ORDERS(oId, dId, wId, cId, entryDate, oLCnt, allLocal)
  VALUES (o_id, d_Id, w_Id, c_Id, date_time, o_ol_cnt, o_all_local);

  INSERT INTO NEW_ORDER(oId, dId, wId)
  VALUES (o_id, d_Id, w_Id);

  FOR ol_number in 1 .. o_ol_cnt LOOP
    ol_supply_w_id := supplywarehouse[ol_number]; -- modified from original code since Postgres array index starts from 1
    -- get supply warehouse id
    IF (ol_supply_w_id != w_id) THEN 
      o_all_local := 0; 
      -- if supplying warehouse != home warehouse, then not local
    END IF;
    ol_i_id := itemid[ol_number];
    ol_quantity := qty[ol_number];

    SELECT I.price, I.name, I.data
    INTO i_price, i_name, i_data
    FROM ITEM I
    WHERE I.iId = ol_i_id;

    price[ol_number] := i_price;
    iname[ol_number] := i_name;

    SELECT quantity, data, dist01, dist02, dist03, dist04, dist05, 
    dist06, dist07, dist08, dist09, dist10
    INTO s_quantity, s_data, s_dist01, s_dist02, s_dist03, s_dist04, 
    s_dist05, s_dist06, s_dist07, s_dist08, s_dist09, s_dist10
    FROM STOCK
    WHERE STOCK.iId = ol_i_id AND STOCK.wId = ol_supply_w_id;

    -- pick correct s_dist_xx
    pick_dist_info(ol_dist_info, ol_w_id); 
    stock[ol_number - 1] := s_quantity;

    IF (i_data LIKE '%original' AND s_data LIKE '%original') THEN
      bg[ol_number] := 'B';
    ELSE
      bg[ol_number] := 'G';
    END IF;

    IF (s_quantity > ol_quantity) THEN
      s_quantity := s_quantity - ol_quantity;
    ELSE
      s_quantity := s_quantity - ol_quantity + 91;
    END IF;

    UPDATE STOCK SET quantity = s_quantity
    WHERE STOCK.iId = ol_i_id
    AND STOCK.wId = ol_supply_w_id;

    ol_amount := ol_quantity * i_price * (1 + w_tax + d_tax) * 
    (1 - c_discount);
    amt[ol_number] := ol_amount;
    total :+= ol_amount;

    INSERT INTO ORDERLINE(oId, dId, wId, number, iId, supplyWId, 
    quantity, amount, distInfo)
    VALUES (o_id, d_id, w_id, ol_number, ol_i_id, ol_supply_w_id, 
    ol_quantity, ol_amount, ol_dist_info);
  END LOOP;
END;

$$ LANGUAGE PLpgSQL;
