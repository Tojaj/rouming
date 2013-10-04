/* Schema */

CREATE TABLE metadata (_id INTEGER PRIMARY KEY AUTOINCREMENT,last_update INTEGER);

CREATE TABLE pictures (_id INTEGER PRIMARY KEY AUTOINCREMENT,time INTEGER,name TEXT UNIQUE ON CONFLICT REPLACE,detail_url TEXT,picture_url TEXT,size INTEGER,likes INTEGER,dislikes INTEGER,comments INTEGER);

/* Triggers */

CREATE TRIGGER trigger_metadata_keep_only_one_line AFTER INSERT ON metadata BEGIN
     DELETE FROM metadata WHERE last_update < (SELECT MAX(last_update) FROM metadata);
END;

CREATE TRIGGER trigger_picutres_keep_limited_number_of_records AFTER INSERT ON pictures BEGIN
     DELETE FROM pictures WHERE _id <= ((SELECT MAX(_id) FROM pictures) - 2);
END;

/* Example data */

INSERT INTO metadata VALUES (null, 11);
INSERT INTO metadata VALUES (null, 22);
INSERT INTO metadata VALUES (null, 33);

SELECT * FROM metadata;

INSERT INTO pictures VALUES (null, 111, "pic_1", "http://www.foo.cz/detail_1", "http://www.foo.cz/pic=1", 1234, 5, 6, 3);
INSERT INTO pictures VALUES (null, 111, "pic_2", "http://www.foo.cz/detail_2", "http://www.foo.cz/pic=2", 1234, 5, 6, 3);
INSERT INTO pictures VALUES (null, 111, "pic_3", "http://www.foo.cz/detail_3", "http://www.foo.cz/pic=3", 1234, 5, 6, 3);
INSERT INTO pictures VALUES (null, 111, "pic_4", "http://www.foo.cz/detail_4", "http://www.foo.cz/pic=4", 1234, 5, 6, 3);
insert into pictures(time,name,detail_url,picture_url,size,likes,dislikes,comments) values (111, "pic_5", "http://www.foo.cz/detail_5", "http://www.foo.cz/pic=5", 1234, 5, 6, 3);


SELECT * FROM pictures;
