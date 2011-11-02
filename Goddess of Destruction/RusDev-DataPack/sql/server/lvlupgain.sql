DROP TABLE IF EXISTS `lvlupgain`;
CREATE TABLE `lvlupgain` (
  `classid` tinyint(3) unsigned NOT NULL,
  `defaulthpbase` decimal(7,3) NOT NULL,
  `defaulthpadd` decimal(7,3) NOT NULL,
  `defaulthpmod` decimal(3,2) NOT NULL DEFAULT '0.37',
  `defaultcpbase` decimal(7,3) NOT NULL,
  `defaultcpadd` decimal(7,3) NOT NULL,
  `defaultcpmod` decimal(3,2) NOT NULL DEFAULT '0.22',
  `defaultmpbase` decimal(7,3) NOT NULL,
  `defaultmpadd` decimal(7,3) NOT NULL,
  `defaultmpmod` decimal(3,2) NOT NULL DEFAULT '0.14',
  `class_lvl` tinyint(2) unsigned NOT NULL,
  PRIMARY KEY (`classid`)
);

INSERT INTO `lvlupgain` (`classid`,`defaulthpbase`,`defaulthpadd`,`defaultcpbase`,`defaultcpadd`,`defaultmpbase`,`defaultmpadd`,`class_lvl`) VALUES
-- HUMANS
(0, 80.0, 11.83, 32.0, 3.01, 30.0, 5.46, 1),
(1, 327.0, 33.00, 261.6, 26.40, 144.0, 9.90, 20),
(2, 1044.0, 49.40, 939.6, 44.46, 359.1, 19.50, 40),
(3, 1044.0, 54.60, 835.2, 43.68, 359.1, 19.50, 40),
(4, 327.0, 29.70, 196.2, 17.82, 144.0, 9.90, 20),
(5, 972.3, 46.80, 583.3, 28.08, 359.1, 19.50, 40),
(6, 972.3, 46.80, 583.3, 28.08, 359.1, 19.50, 40),
(7, 327.0, 27.50, 163.0, 12.65, 144.0, 9.90, 20),
(8, 924.5, 41.60, 508.475, 22.88, 359.1, 19.50, 40), -- ok
(9, 924.5, 44.20, 647.1, 30.94, 359.1, 19.50, 40),
(10, 101.0, 15.57, 50.5, 7.84, 40.0, 7.38, 1),
(11, 424.0, 27.60, 212.0, 13.85, 192.0, 13.30, 20),
(12, 1021.5, 45.60, 612.8, 22.85, 478.8, 26.10, 40),
(13, 1021.5, 45.60, 510.7, 22.85, 478.8, 26.10, 40),
(14, 1021.5, 49.50, 612.9, 29.74, 478.8, 26.10, 40),
(15, 424.0, 34.20, 212.0, 17.15, 192.0, 13.30, 20),
(16, 1164.9, 49.40, 1048.41, 44.46, 478.8, 26.00, 40), -- ok
(17, 1164.9, 53.40, 582.4, 26.75, 478.8, 26.10, 40),
-- ELVES
(18, 89.0, 12.74, 36.1, 3.38, 30.0, 5.46, 1),
(19, 355.0, 33.00, 177.5, 16.50, 144.0, 9.90, 20),
(20, 1072.0, 52.00, 643.2, 31.20, 359.1, 19.50, 40),
(21, 1072.0, 54.60, 536.0, 27.30, 359.1, 19.50, 40),
(22, 355.0, 30.80, 177.5, 14.40, 144.0, 9.90, 20),
(23, 1024.2, 46.80, 563.31, 25.74, 359.1, 19.50, 40), -- ok
(24, 1024.2, 49.40, 512.1, 24.70, 359.1, 19.50, 40),
(25, 104.0, 15.57, 52.0, 7.84, 40.0, 7.38, 1),
(26, 427.0, 28.70, 213.5, 14.40, 192.0, 13.30, 20),
(27, 1048.4, 48.20, 629.0, 24.15, 478.8, 26.10, 40),
(28, 1048.4, 50.80, 629.0, 30.52, 478.8, 26.10, 40),
(29, 427.0, 35.30, 213.5, 17.70, 192.0, 13.30, 20),
(30, 1191.8, 54.60, 1072.62, 49.14, 478.8, 26.00, 40), -- ok
-- DARK ELVES
(31, 94.0, 13.65, 37.6, 3.80, 30.0, 5.46, 1),
(32, 379.0, 35.20, 189.5, 17.60, 144.0, 9.90, 20),
(33, 1143.8, 54.60, 686.2, 32.76, 359.1, 19.50, 40),
(34, 1143.8, 58.50, 571.9, 29.25, 359.1, 19.50, 40),
(35, 379.0, 33.00, 185.1, 15.80, 144.0, 9.90, 20),
(36, 1096.0, 49.40, 602.8, 27.17, 359.1, 19.50, 40), -- ok
(37, 1096.0, 52.00, 548.0, 26.00, 359.1, 19.50, 40),
(38, 106.0, 15.57, 53.0, 7.84, 40.0, 7.38, 1),
(39, 429.0, 29.80, 214.5, 14.95, 192.0, 13.30, 20),
(40, 1074.3, 48.20, 644.5, 24.15, 478.8, 26.10, 40),
(41, 1074.3, 52.10, 644.5, 31.30, 478.8, 26.10, 40),
(42, 429.0, 36.40, 214.5, 18.25, 192.0, 13.30, 20),
(43, 1217.7, 54.60, 1095.93, 49.14, 478.8, 26.00, 40), -- ok
-- ORCS
(44, 80.0, 12.64, 40.0, 6.27, 30.0, 5.36, 1),
(45, 346.0, 35.10, 242.2, 24.54, 144.0, 9.80, 20),
(46, 1110.8, 57.10, 777.5, 39.94, 359.1, 19.40, 40),
(47, 346.0, 32.90, 173.0, 16.40, 144.0, 9.80, 20),
(48, 1063.0, 54.50, 531.5, 27.20, 359.1, 19.40, 40),
(49, 95.0, 15.47, 47.5, 7.74, 40.0, 7.28, 1),
(50, 418.0, 35.20, 209.0, 17.60, 192.0, 13.20, 20),
(51, 1182.8, 53.30, 1069.2, 42.64, 478.8, 26.00, 40),
(52, 1182.8, 53.30, 591.4, 26.65, 478.8, 26.00, 40),
-- DWARVES
(53, 80.0, 12.64, 56.0, 8.82, 30.0, 5.36, 1),
(54, 346.0, 35.10, 242.2, 24.54, 144.0, 9.80, 20),
(55, 1110.8, 57.10, 777.5, 39.94, 359.1, 19.40, 40),
(56, 346.0, 32.90, 276.8, 26.30, 144.0, 9.80, 20),
(57, 1063.0, 54.50, 850.4, 43.58, 359.1, 19.40, 40),
-- HUMANS 3rd Professions
(88, 3061.8, 63.08, 2755.6, 56.77, 1155.6, 24.90, 76),
(89, 3274.2, 69.72, 2619.3, 55.78, 1155.6, 24.90, 76),
(90, 2883.9, 59.76, 1730.3, 35.86, 1155.6, 24.90, 76),
(91, 2883.9, 59.76, 1730.3, 35.86, 1155.6, 24.90, 76),
(92, 2729.9, 56.44, 1910.9, 39.51, 1155.6, 24.90, 76),
(93, 2623.7, 53.12, 1443.035, 29.216, 1155.6, 24.90, 76), -- ok
(94, 2880.0, 58.10, 1728.0, 29.05, 1540.8, 33.20, 76),
(95, 2880.0, 58.10, 1440.0, 29.05, 1540.8, 33.20, 76),
(96, 3039.3, 63.08, 1823.5, 37.85, 1540.8, 33.20, 76),
(97, 3182.7, 63.08, 2864.43, 56.772, 1540.8, 33.20, 76), -- ok
(98, 3342.0, 68.06, 1671.0, 34.03, 1540.8, 33.20, 76),
-- ELVES 3rd Professions
(99, 3196.0, 66.40, 1917.6, 39.84, 1155.6, 24.90, 76),
(100, 3302.2, 69.72, 1651.1, 34.86, 1155.6, 24.90, 76),
(101, 2935.8, 59.76, 1614.69, 32.868, 1155.6, 24.90, 76), -- ok
(102, 3042.0, 63.08, 1521.0, 31.54, 1155.6, 24.90, 76),
(103, 3013.1, 61.42, 1807.8, 30.71, 1540.8, 33.20, 76),
(104, 3119.3, 64.74, 1871.5, 38.84, 1540.8, 33.20, 76),
(105, 3422.0, 69.72, 3079.8, 62.748, 1540.8, 33.20, 76), -- ok
-- DARK ELVES 3rd Professions
(106, 3374.0, 69.72, 2024.4, 41.83, 1155.6, 24.90, 76),
(107, 3533.3, 74.70, 1766.6, 37.35, 1155.6, 24.90, 76),
(108, 3113.8, 63.08, 1712.59, 34.694, 1155.6, 24.90, 76), -- ok
(109, 3220.0, 66.40, 1610.0, 33.20, 1155.6, 24.90, 76),
(110, 3039.0, 61.42, 1823.4, 30.71, 1540.8, 33.20, 76),
(111, 3198.3, 66.40, 1918.9, 39.84, 1540.8, 33.20, 76),
(112, 3447.9, 69.72, 3103.11, 62.748, 1540.8, 33.20, 76), -- ok
-- ORCS 3rd Professions
(113, 3447.2, 72.94, 2413.0, 51.03, 1155.6, 24.80, 76),
(114, 3293.2, 69.62, 1646.6, 34.76, 1155.6, 24.80, 76),
(115, 3359.9, 67.96, 3037.3, 54.35, 1540.8, 33.10, 76),
(116, 3359.9, 67.96, 1679.9, 33.93, 1540.8, 33.10, 76),
-- DWARVES 3rd Professions
(117, 3447.2, 72.94, 2413.0, 51.03, 1155.6, 24.80, 76),
(118, 3293.2, 69.62, 2634.5, 55.68, 1155.6, 24.80, 76),
-- KAMAELS
(123, 95.0, 13.65, 47.5, 6.825, 30.0, 5.46, 1), -- ok
(124, 97.0, 16.38, 48.5, 8.19, 40.0, 7.28, 1), -- ok
(125, 398.53, 35.20, 199.265, 17.60, 147.27, 9.90, 20), -- ok
(126, 449.9, 30.80, 224.95, 15.40, 196.36, 13.20, 20), -- ok
(127, 1178.81, 57.20, 589.405, 28.60, 370.11, 19.50, 40), -- ok
(128, 1173.65, 52.00, 586.825, 26.00, 376.56, 26.00, 40), -- ok
(129, 1134.58, 52.00, 567.29, 26.00, 493.48, 26.00, 40), -- ok
(130, 1139.74, 57.20, 569.87, 28.60, 487.03, 19.50, 40), -- ok
(131, 3515.21, 73.04, 1757.605, 36.52, 1166.61, 24.90, 76), -- ok
(132, 3297.65, 66.40, 1648.825, 33.20, 1438.56, 33.20, 76), -- ok
(133, 3258.58, 66.40, 1629.29, 33.20, 1555.48, 33.20, 76), -- ok
(134, 3476.14, 73.04, 1738.07, 36.52, 1283.53, 24.90, 76), -- ok
(135, 1135.87, 53.30, 567.935, 26.65, 493.48, 26.00, 40), -- ok
(136, 3312.97, 68.06, 1656.485, 34.03, 1555.48, 33.20, 76); -- ok