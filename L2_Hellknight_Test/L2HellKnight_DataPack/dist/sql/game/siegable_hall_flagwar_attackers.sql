CREATE TABLE IF NOT EXISTS `siegable_hall_flagwar_attackers` (
`hall_id` tinyint(2) unsigned NOT NULL DEFAULT '0',
`flag` int(10) unsigned NOT NULL DEFAULT '0',
`npc` int(10) unsigned NOT NULL DEFAULT '0',
`clan_id` int(10) unsigned NOT NULL DEFAULT '0',
PRIMARY KEY (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;