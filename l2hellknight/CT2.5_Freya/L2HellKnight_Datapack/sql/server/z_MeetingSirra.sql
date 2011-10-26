INSERT INTO spawnlist VALUES
('schuttgart12_npc2314_100', 1, 32781, 102402, -124491, -2768, 0, 0, -12072, 60, 0, 0),
('schuttgart12_npc2314_100', 1, 32761, 102512, -124416, -2768, 0, 0, -11100, 60, 0, 0),
('schuttgart12_npc2314_100', 1, 32777, 102306, -124361, -2748, 0, 0, -11564, 60, 0, 0),
('schuttgart12_npc2314_100', 1, 32778, 102462, -124237, -2752, 0, 0, -10828, 60, 0, 0);

UPDATE npcaidata SET clan='freya_show_friends', enemyClan = 'freya_show_foes' WHERE npc_id = 22767;
UPDATE npcaidata SET clan='freya_show_foes', enemyClan = 'freya_show_friends' WHERE npc_id IN (18848, 18849, 18926);
UPDATE npcaidata SET enemyRange = 2000 WHERE npc_id IN (18848, 18849, 18926);
UPDATE npcaidata SET enemyRange = 500, clan_range=500 WHERE npc_id = 22767;

UPDATE npc SET type = 'L2Monster' WHERE id IN (18847, 18848, 18849, 18926, 22767);
UPDATE npc SET aggro = 300 WHERE id IN (18847, 22767);  