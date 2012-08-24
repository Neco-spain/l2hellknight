DROP TABLE IF EXISTS `product_item_components`;
CREATE TABLE `product_item_components` (
  `product_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`product_id`,`item_id`)
);

INSERT INTO `product_item_components` VALUES
-- dynasty sets
('1010001', '9425', '1'),
('1010002', '9428', '1'),
('1010003', '9429', '1'),
('1010004', '9430', '1'),
('1010005', '9431', '1'),
('1010006', '9416', '1'),
('1010007', '9421', '1'),
('1010008', '9422', '1'),
('1010009', '9423', '1'),
('1010010', '9424', '1'),
('1010011', '9441', '1'),
('1010012', '9432', '1'),
('1010013', '9437', '1'),
('1010014', '9438', '1'),
('1010015', '9439', '1'),
('1010016', '9440', '1');
INSERT INTO `product_item_components` VALUES
-- moirai sets
('1010017', '15609', '1'),
('1010018', '15612', '1'),
('1010019', '15606', '1'),
('1010020', '15615', '1'),
('1010021', '15618', '1'),
('1010022', '15621', '1'),
('1010023', '15610', '1'),
('1010024', '15613', '1'),
('1010025', '15607', '1'),
('1010026', '15616', '1'),
('1010027', '15619', '1'),
('1010028', '15611', '1'),
('1010029', '15614', '1'),
('1010030', '15608', '1'),
('1010031', '15617', '1'),
('1010032', '15620', '1');

INSERT INTO `product_item_components` VALUES
('1050021', '22025', '1'),
('1050022', '22026', '1'),
('1080001', '22000', '1'),
('1080002', '22001', '1'),
('1080003', '22002', '1'),
('1080004', '22003', '1'),
('1080005', '22004', '1'),
('1080006', '22005', '1'),
('1080009', '22027', '1'),
('1080010', '22028', '1'),
('1080011', '22029', '1'),
('1080012', '22030', '1'),
('1080013', '22031', '1'),
('1080014', '22032', '1'),
('1080015', '22033', '1'),
('1080016', '22034', '1'),
('1080017', '22035', '1'),
('1080018', '22036', '1'),
('1080019', '22037', '1'),
('1080021', '22039', '1'),
('1080022', '22040', '1'),
('1080023', '22041', '1'),
('1080024', '22042', '1'),
('1080025', '22043', '1'),
('1080026', '22044', '1'),
('1080027', '22045', '1'),
('1080028', '22046', '1'),
('1080029', '22047', '1'),
('1080030', '22048', '1'),
('1080031', '22049', '1'),
('1080032', '22050', '1'),
('1080033', '22060', '1'),
('1080034', '22061', '1'),
('1080035', '22062', '1'),
('1080048', '22066', '1'),
('1080049', '22087', '1'),
('1080050', '22088', '1'),
('1080051', '22089', '1'),
('1080052', '22090', '1'),
('1080053', '22091', '1'),
('1080054', '22092', '1'),
('1080055', '22093', '1'),
('1080056', '22149', '1'),
('1080057', '22150', '1'),
('1080058', '22151', '1'),
('1080059', '22152', '1'),
('1080060', '22153', '1'),
('1080061', '22094', '1'),
('1080062', '22095', '1'),
('1080063', '22096', '1'),
('1080064', '22097', '1'),
('1080065', '22098', '1'),
('1080066', '22099', '1'),
('1080067', '22100', '1'),
('1080068', '22101', '1'),
('1080069', '22102', '1'),
('1080070', '22103', '1'),
('1080071', '22104', '1'),
('1080072', '22105', '1'),
('1080073', '22106', '1'),
('1080074', '22107', '1'),
('1080075', '22108', '1'),
('1080076', '22109', '1'),
('1080077', '22110', '1'),
('1080078', '22111', '1'),
('1080079', '22112', '1'),
('1080080', '22113', '1'),
('1080081', '22114', '1'),
('1080082', '22115', '1'),
('1080083', '22116', '1'),
('1080084', '22117', '1'),
('1080085', '22118', '1'),
('1080086', '22119', '1'),
('1080087', '22120', '1'),
('1080088', '22121', '1'),
('1080089', '22122', '1'),
('1080090', '22123', '1'),
('1080091', '22124', '1'),
('1080092', '22125', '1'),
('1080093', '22126', '1'),
('1080094', '22127', '1'),
('1080095', '22128', '1'),
('1080096', '22129', '1'),
('1080097', '22130', '1'),
('1080098', '22131', '1'),
('1080099', '22132', '1'),
('1080100', '22133', '1'),
('1080101', '22134', '1'),
('1080102', '22135', '1'),
('1080103', '22136', '1'),
('1080104', '22137', '1'),
('1080105', '22138', '1'),
('1080106', '22139', '1'),
('1080107', '22140', '1'),
('1080112', '20335', '1'),
('1080113', '20336', '1'),
('1080114', '20337', '1'),
('1080115', '20338', '1'),
('1080116', '20339', '1'),
('1080117', '20340', '1'),
('1080118', '20341', '1'),
('1080119', '20342', '1'),
('1080120', '20343', '1'),
('1080121', '20344', '1'),
('1080122', '20345', '1'),
('1080123', '20346', '1'),
('1080124', '20347', '1'),
('1080125', '20348', '1'),
('1080126', '20349', '1'),
('1080127', '20350', '1'),
('1080128', '20351', '1'),
('1080129', '20352', '1'),
('1080130', '12362', '1'),
('1080131', '12363', '1'),
('1080132', '12364', '1'),
('1080133', '12365', '1'),
('1080134', '12366', '1'),
('1080135', '12367', '1'),
('1080136', '12368', '1'),
('1080137', '12369', '1'),
('1080138', '12370', '1'),
('1080139', '12371', '1'),
('1080140', '20326', '1'),
('1080141', '20327', '1'),
('1080142', '20328', '1'),
('1080143', '20329', '1'),
('1080144', '20330', '1'),
('1080145', '20331', '1'),
('1080146', '20364', '1'),
('1080147', '20365', '1'),
('1080148', '20366', '1'),
('1080149', '20367', '1'),
('1080150', '20368', '1'),
('1080151', '20369', '1'),
('1080152', '20370', '1'),
('1080153', '20371', '1'),
('1080154', '20372', '1'),
('1080155', '20373', '1'),
('1080156', '20374', '1'),
('1080157', '20375', '1'),
('1080158', '20376', '1'),
('1080159', '20377', '1'),
('1080160', '20378', '1'),
('1080161', '20379', '1'),
('1080162', '20380', '1'),
('1080163', '20381', '1'),
('1080164', '20382', '1'),
('1080165', '20383', '1'),
('1080166', '20384', '1'),
('1080167', '20385', '1'),
('1080168', '20386', '1'),
('1080169', '20387', '1'),
('1080170', '20388', '1'),
('1080171', '20389', '1'),
('1080172', '20390', '1'),
('1080173', '13015', '1'),
('1080174', '13016', '5'),
('1080175', '13016', '10'),
('1080176', '20033', '5'),
('1080177', '20033', '10'),
('1080178', '13010', '5'),
('1080179', '13010', '10'),
('1080180', '13011', '5'),
('1080181', '13011', '10'),
('1080182', '13012', '5'),
('1080183', '13012', '10'),
('1080185', '13021', '1'),
('1080186', '5592', '1'),
('1080197', '20391', '1'),
('1080198', '20392', '1'),
('1080199', '20393', '1'),
('1080200', '20394', '1'),
('1080201', '139', '1'),
('1080202', '140', '1'),
('1080203', '141', '1'),
('1080205', '13370', '1'),
('1080206', '13371', '1'),
('1080207', '13372', '1'),
('1080208', '13373', '1'),
('1080209', '13374', '1'),
('1080210', '13375', '1'),
('1080211', '13376', '1'),
('1080212', '13377', '1'),
('1080213', '13378', '1'),
('1080214', '13379', '1'),
('1080229', '13380', '1'),
('1080230', '13381', '1'),
('1080236', '17019', '1'),
('1080238', '14054', '1'),
('1080239', '13022', '1'),
('1080240', '15438', '1'),
('1080241', '15440', '1'),
('1080242', '20572', '1'),
('1080243', '21084', '1'),
('1080244', '21086', '1'),
('1080245', '21030', '1'),
('1080246', '21031', '1'),
('1080247', '21032', '1'),
('1080248', '21033', '1'),
('1080249', '21038', '1');
