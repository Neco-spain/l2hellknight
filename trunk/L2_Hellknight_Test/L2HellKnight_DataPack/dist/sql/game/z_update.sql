UPDATE `npc` SET `matkspd`='333' WHERE 1;
UPDATE npc SET exp = exp * 1.2, sp = sp * 1.2 WHERE level >= 76 AND type LIKE 'L2Monster';
UPDATE npc SET exp = exp * 1.2, sp = sp * 1.2 WHERE id >= 20809 AND id <= 20829;
UPDATE npc SET exp = exp * 1.2, sp = sp * 1.2 WHERE id >= 20858 AND id <= 20860;
UPDATE npc SET exp = exp * 1.2, sp = sp * 1.2 WHERE id >= 21061 AND id <= 21083;
DELETE FROM `droplist` WHERE `itemId`='9912' and `mobId` NOT IN (SELECT id FROM npc WHERE type='L2RaidBoss');
SET FOREIGN_KEY_CHECKS=0;