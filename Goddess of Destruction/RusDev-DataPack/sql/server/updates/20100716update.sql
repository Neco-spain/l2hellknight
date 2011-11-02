ALTER TABLE `hitman_list`
MODIFY COLUMN `target_name`  varchar(30) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `clientId`,
ADD COLUMN `itemId`  int NOT NULL DEFAULT 57 AFTER `target_name`,
MODIFY COLUMN `bounty`  bigint UNSIGNED NOT NULL DEFAULT 0 AFTER `itemId`;