tCREATE OR REPLACE FUNCTION stocklevel(w_id INTEGER,d_id INTEGER,threshold) {
RETURNS INTEGER AS $$

DECLARE
  o_id INTEGER;
  stock_count INTEGER;
BEGIN
  SELECT nextOrdId INTO o_id
  FROM DISTRICT D
  WHERE D.wId = w_id AND D.dId = d_id;

  SELECT COUNT(DISTINCT(S.iId)) INTO stock_count
  FROM ORDERLINE OL, STOCK S 
  WHERE OL.wId = w_id AND OL.dId = d_id AND OL.oId < o_id AND OL.oId >= o_id - 20 
  AND S.wId = w_id AND S.iId = OL.iId AND S.quantity < threshold;

  RETURN 0;
END;
$$ LANGUAGE PLpgSQL;