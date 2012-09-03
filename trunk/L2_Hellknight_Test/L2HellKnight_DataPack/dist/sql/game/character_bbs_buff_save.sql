DROP TABLE IF EXISTS `character_bbs_buff_save`;
CREATE TABLE `character_bbs_buff_save` (
  `charId` int(10) default NULL,
  `skill_id` int(5) default NULL,
  `skill_level` int(2) default NULL,
  `for_pet` int(1) default NULL
) DEFAULT CHARSET=utf8;

