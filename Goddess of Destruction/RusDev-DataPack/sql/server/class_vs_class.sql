CREATE TABLE IF NOT EXISTS class_vs_class (
	class_id_01  int(10) NOT NULL ,
	class_id_02  int(10) NOT NULL ,
	power  decimal(5,2) NOT NULL DEFAULT 1.0 ,
	PRIMARY KEY (class_id_01, class_id_02)
);

INSERT IGNORE INTO class_vs_class 
	(SELECT c1.id as class_id_01, c2.id class_id_02, 1.0 as power 
		FROM class_list c2, class_list c1);