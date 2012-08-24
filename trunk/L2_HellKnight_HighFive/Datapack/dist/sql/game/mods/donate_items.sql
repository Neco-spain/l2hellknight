-- --------------------------------------------
-- Table structure for donate_items
-- ---------------------------------------------
DROP TABLE IF EXISTS `donate_items`;
CREATE TABLE IF NOT EXISTS `donate_items` (
  `char_name` varchar(255) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_count` bigint(20) NOT NULL,
  PRIMARY KEY  (`char_name`,`item_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;