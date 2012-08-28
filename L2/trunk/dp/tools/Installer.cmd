@echo off

REM ###############################################
REM ## ������� ���� ��������� ����� ���� ������  ##
REM ###############################################
REM ���� � ����� MYSQL.exe
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.0\bin

set DateT=%date%

REM LOGINSERVER
set lsuser=root
set lspass=
set lsdb=l2jsoftware
set lshost=localhost

REM GAMESERVER
set gsuser=root
set gspass=
set gsdb=l2jsoftware
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"


:Step1
cls
echo. ============================================================
echo.                                                                                                                         
echo.   L2jSoftware Interlude - ����樨 � ����� ������ �ࢥ� ���ਧ�樨           
echo. ________________________________________________________
echo.                                                                                                                         
echo.   1 - ������ ���⠫��� �ࢥ� ���ਧ�樨.                                    
echo.   2 - ��३� � ��⠭���� ��ࢥ� ����.                                              
echo.   3 - ���.                                                                                                    
echo. ============================================================

set Step1prompt=x
set /p Step1prompt= ������ ���祭��:
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto fullend
goto Step1


:LoginInstall
@cls
echo.
echo ���⪠ ��: %lsdb% � ��⠭���� �ࢥ� ���ਧ�樨.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo ������塞 ⠡���� accounts
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo ������塞 ⠡���� gameservers
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo C�ࢥ� ���ਧ�樨 ��⠭�����.
pause
goto :Step2

:Step2
@cls
echo. ============================================================
echo.                                                                                                                         
echo.   L2jSoftware Interlude - ����樨 � ����� ������ �ࢥ� ����                        
echo. ________________________________________________________
echo.                                                                                                                         
echo.   1 - ������ ���⠫��� �ࢥ� ����.                                                  
echo.   2 - ���.                                                                                                   
echo. ============================================================

set Step2prompt=x
set /p Step2prompt= ������ ���祭��:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo �������� ��஥ ᮤ�ন��� �� �ࢥ� ����.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
set title=��⠭�����
goto CreateTables

:CreateTables
@cls
echo.
echo ����� ���� %title%� �᭮��� 䠩�� �ࢥ� ����.
pause
@cls
echo ***** �����襭� 1 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/account_data.sql
@cls
echo ***** �����襭� 2 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql
@cls
echo ***** �����襭� 3 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armorsets.sql
@cls
echo ***** �����襭� 4 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql
@cls
echo ***** �����襭� 5 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql
@cls
echo ***** �����襭� 6 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql
@cls
echo ***** �����襭� 7 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql
@cls
echo ***** �����襭� 8 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql
@cls
echo ***** �����襭� 9 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql
@cls
echo ***** �����襭� 10 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxaccess.sql
@cls
echo ***** �����襭� 11 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxes.sql
@cls
echo ***** �����襭� 12 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql
@cls
echo ***** �����襭� 13 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql
@cls
echo ***** �����襭� 14 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql
@cls
echo ***** �����襭� 15 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql
@cls
echo ***** �����襭� 16 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql
@cls
echo ***** �����襭� 17 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql
@cls
echo ***** �����襭� 18 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql
@cls
echo ***** �����襭� 19 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ban_hwid.sql
@cls
echo ***** �����襭� 20 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql
@cls
echo ***** �����襭� 21 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
@cls
echo ***** �����襭� 22 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql
@cls
echo ***** �����襭� 23 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql
@cls
echo ***** �����襭� 24 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql
@cls
echo ***** �����襭� 25 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql
@cls
echo ***** �����襭� 26 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql
@cls
echo ***** �����襭� 27 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql
@cls
echo ***** �����襭� 28 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql
@cls
echo ***** �����襭� 29 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql
@cls
echo ***** �����襭� 30 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql
@cls
echo ***** �����襭� 31 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql
@cls
echo ***** �����襭� 32 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql
@cls
echo ***** �����襭� 33 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql
@cls
echo ***** �����襭� 34 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql
@cls
echo ***** �����襭� 35 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql
@cls
echo ***** �����襭� 36 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql
@cls
echo ***** �����襭� 37 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql
@cls
echo ***** �����襭� 38 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql
@cls
echo ***** �����襭� 39 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql
@cls
echo ***** �����襭� 40 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql
@cls
echo ***** �����襭� 41 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql
@cls
echo ***** �����襭� 42 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql
@cls
echo ***** �����襭� 43 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql
@cls
echo ***** �����襭� 44 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql
@cls
echo ***** �����襭� 45 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql
@cls
echo ***** �����襭� 46 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql
@cls
echo ***** �����襭� 47 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna.sql
@cls
echo ***** �����襭� 48 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql
@cls
echo ***** �����襭� 49 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql
@cls
echo ***** �����襭� 50 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql
@cls
echo ***** �����襭� 51 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql
@cls
echo ***** �����襭� 52 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql
@cls
echo ***** �����襭� 53 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql
@cls
echo ***** �����襭� 54 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mapregion.sql
@cls
echo ***** �����襭� 55 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql
@cls
echo ***** �����襭� 56 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql
@cls
echo ***** �����襭� 57 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql
@cls
echo ***** �����襭� 58 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql
@cls
echo ***** �����襭� 59 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql
@cls
echo ***** �����襭� 60 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql
@cls
echo ***** �����襭� 61 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql
@cls
echo ***** �����襭� 62 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql
@cls
echo ***** �����襭� 63 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql
@cls
echo ***** �����襭� 64 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql
@cls
echo ***** �����襭� 65 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql
@cls
echo ***** �����襭� 66 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql
@cls
echo ***** �����襭� 67 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql
@cls
echo ***** �����襭� 68 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql
@cls
echo ***** �����襭� 70 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql
@cls
echo ***** �����襭� 71 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql
@cls
echo ***** �����襭� 72 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql
@cls
echo ***** �����襭� 73 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql
@cls
echo ***** �����襭� 74 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql
@cls
echo ***** �����襭� 75 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql
@cls
echo ***** �����襭� 76 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql
@cls
echo ***** �����襭� 77 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql
@cls
echo ***** �����襭� 78 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql
@cls
echo ***** �����襭� 79 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql
@cls
echo ***** �����襭� 80 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql
@cls
echo ***** �����襭� 81 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql
@cls
echo ***** �����襭� 82 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql
@cls
echo ***** �����襭� 83 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/walker_routes.sql
@cls
echo ***** �����襭� 84 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql
@cls
echo ***** �����襭� 85 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql
@cls
echo ***** �����襭� 86 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql
@cls
echo ***** �����襭� 87 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql
@cls
echo ***** �����襭� 88 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql
@cls
echo ***** �����襭� 89 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql
@cls
echo ***** �����襭� 90 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql
@cls
echo ***** �����襭� 93 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql
@cls
echo ***** �����襭� 94 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lastimperialtomb_spawnlist.sql
@cls
echo ***** �����襭� 95 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_functions.sql
@cls
echo ***** �����襭� 96 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf.sql
@cls
echo ***** �����襭� 97 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql
@cls
echo ***** �����襭� 98 ��業⮢ *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_notices.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_news.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/account_premium.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_siege.sql

%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_buff_profiles.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_raid_points.sql

%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_door.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_doorupgrade.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fortsiege_clans.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/announce_records.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2top_votes.sql
@cls
echo ***** �����襭� 100 ��業⮢ *****
echo.
echo C�ࢥ� ���� %title%.
pause
goto :Step1

:end
echo.
echo ��⠭���� �����襭�.
echo.
pause

:fullend
