-- апдейты, которые можно применять многократно без побочных действий
DELETE FROM `npc` WHERE (`id`='33201');

UPDATE IGNORE `character_variables` SET `name` = 'KamalokaHall' WHERE `name` = 'LastEnterInstance';
DELETE FROM `character_variables` WHERE `name` = 'LastEnterInstance';
DELETE FROM `character_variables` WHERE `name` = 'HellboundConfidence';
DELETE FROM `character_quests` WHERE `name`='_1003_Valakas';

DELETE FROM `character_variables` WHERE `name` IN ('q211','q212','q213','q214','q215','q216','q217','q218','q219','q220','q221','q222','q223','q224','q225','q226','q227','q228','q229','q230','q231','q232','q233','q281','dd');

UPDATE `items` SET `item_id`=12602 WHERE `item_id`=12609;

DROP TABLE IF EXISTS zone;