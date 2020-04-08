CREATE OR REPLACE FUNCTION viewitem(i_id INTEGER) {

RETURNS VOID AS $$
DECLARE
	nothing void;
BEGIN

	SELECT B.bId
	FROM BIDS B
	WHERE B.bId = i_id;

IF (test bool)

	SELECT ITEMS.nbids
	FROM ITEMS I
	WHERE I.iId = i_id;

END IF;
	
END;
}
