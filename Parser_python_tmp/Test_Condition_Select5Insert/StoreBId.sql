CREATE OR REPLACE FUNCTION storebid( i_id INTEGER, val INTEGER) {
RETURNS VOID AS $$
DECLARE
	nothing void;

BEGIN

IF (test insert1)

	INSERT INTO BIDS ( bId , iId ) VALUES ( i_id, val) ;

 END IF;

 IF (Blbalblab)
 
	SELECT I.nbids 
	FROM ITEMS I
	WHERE I.iId = i_id ;
	
END IF;

	UPDATE ITEMS SET nbids = n + 1 WHERE ITEMS.iId = i_id ;


END;
}
