ALTER TABLE `mail` ADD COLUMN `returnable` tinyint(4) NOT NULL DEFAULT '1' AFTER unread;
ALTER TABLE `mail` ADD COLUMN `systemMsg1` int(10) NOT NULL AFTER returnable;
ALTER TABLE `mail` ADD COLUMN `systemMsg2` int(10) NOT NULL AFTER systemMsg1;
ALTER TABLE `mail` DROP COLUMN `system`;