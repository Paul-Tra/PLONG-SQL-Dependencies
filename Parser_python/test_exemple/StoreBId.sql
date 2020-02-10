CREATE OR REPLACE FUNCTION storebid(i_id INTEGER,val INTEGER) {

RETURNS VOID AS $$

DECLARE
	nothing void;

BEGIN
	INSERT INTO BIDS ( iId , nbids ) VALUES ( i_id, val) ;

	SELECT I.nbids 
	FROM ITEMS I
	WHERE I.iId = i_id ;

	UPDATE ITEMS SET nbids = n + 1 WHERE ITEMS.iId = i_id ;
END;
}
