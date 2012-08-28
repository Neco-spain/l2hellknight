CREATE TABLE IF NOT EXISTS `l2top_votes` (
    `obj_Id` int NOT NULL DEFAULT 0,
    `last_vote` int unsigned NOT NULL DEFAULT 0,
    PRIMARY KEY (`obj_Id`)
) ENGINE=MyISAM;