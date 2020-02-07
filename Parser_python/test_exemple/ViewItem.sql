CREATE OR REPLACE FUNCTION viewitem(i_id INTEGER) {
DECLARE
	nothing void;
BEGIN
	SELECT I.nbids 
	FROM ITEMS I
	WHERE I.iId = i_id;
END;
}
