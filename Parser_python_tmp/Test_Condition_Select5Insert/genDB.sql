DROP TABLE IF EXISTS ITEMS CASCADE;
DROP TABLE IF EXISTS BIDS CASCADE;

CREATE TABLE ITEMS (
	iId INTEGER PRIMARY KEY, 
	nbids INTEGER
);

CREATE TABLE BIDS (
	bId INTEGER PRIMARY KEY, 
	iId INTEGER 
);

CREATE TABLE TEST (
	iId INTEGER ,
	wId INTEGER ,
	PRIMARY KEY (iId,wId)
);
