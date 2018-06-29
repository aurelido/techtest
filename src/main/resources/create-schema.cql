CREATE KEYSPACE if not exists payment_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : '1'};

USE payment_keyspace;

DROP TABLE if exists paymentevent;

CREATE TABLE if not exists paymentevent(
    iban text,
    insertdate timestamp,
    paymenteventtype text,
    amount double,
    PRIMARY KEY((iban), insertdate)
) WITH CLUSTERING ORDER BY (insertdate desc);