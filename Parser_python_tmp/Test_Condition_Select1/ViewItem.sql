CREATE OR REPLACE FUNCTION viewitem(i_id INTEGER) {

RETURNS VOID AS $$
DECLARE
	nothing void;
BEGIN

IF (test bool)
	SELECT B.bId
	FROM BIDS B
	WHERE B.bId = i_id;
END IF;

	SELECT ITEMS.nbids
	FROM ITEMS I
	WHERE I.iId = i_id;
	
END;
}
