ALTER TABLE `mods_buffer_skill`
ADD COLUMN `skill_comp`  varchar(35) NULL AFTER `skill_fee_amount`,
MODIFY COLUMN `skill_desc`  longtext NULL DEFAULT NULL AFTER `skill_comp`;
