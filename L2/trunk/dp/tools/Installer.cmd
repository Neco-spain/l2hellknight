@echo off

REM ###############################################
REM ## ╙ърцшЄх эшцх ярЁрьхЄЁ√ тр°хщ срч√ фрээ√ї  ##
REM ###############################################
REM ╧єЄ№ ъ Їрщыє MYSQL.exe
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
echo.   L2jSoftware Interlude - Операции с базой данных сервера авторизации           
echo. ________________________________________________________
echo.                                                                                                                         
echo.   1 - Полная инсталяция сервера авторизации.                                    
echo.   2 - Перейти к установке Сервера игры.                                              
echo.   3 - Выйти.                                                                                                    
echo. ============================================================

set Step1prompt=x
set /p Step1prompt= Введите значение:
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto fullend
goto Step1


:LoginInstall
@cls
echo.
echo Очистка БД: %lsdb% и установка сервера авторизации.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo Обновляем таблицу accounts
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo Обновляем таблицу gameservers
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo Cервер авторизации установлен.
pause
goto :Step2

:Step2
@cls
echo. ============================================================
echo.                                                                                                                         
echo.   L2jSoftware Interlude - Операции с базой данных сервера игры                        
echo. ________________________________________________________
echo.                                                                                                                         
echo.   1 - Полная инсталяция сервера игры.                                                  
echo.   2 - Выйти.                                                                                                   
echo. ============================================================

set Step2prompt=x
set /p Step2prompt= Введите значение:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo Удаляется старое содержимое БД сервера игры.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
set title=установлен
goto CreateTables

:CreateTables
@cls
echo.
echo Сейчас будут %title%ы основные файлы сервера игры.
pause
@cls
echo ***** Завершено 1 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/account_data.sql
@cls
echo ***** Завершено 2 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql
@cls
echo ***** Завершено 3 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armorsets.sql
@cls
echo ***** Завершено 4 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql
@cls
echo ***** Завершено 5 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql
@cls
echo ***** Завершено 6 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql
@cls
echo ***** Завершено 7 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql
@cls
echo ***** Завершено 8 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql
@cls
echo ***** Завершено 9 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql
@cls
echo ***** Завершено 10 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxaccess.sql
@cls
echo ***** Завершено 11 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/boxes.sql
@cls
echo ***** Завершено 12 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql
@cls
echo ***** Завершено 13 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql
@cls
echo ***** Завершено 14 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql
@cls
echo ***** Завершено 15 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql
@cls
echo ***** Завершено 16 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql
@cls
echo ***** Завершено 17 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql
@cls
echo ***** Завершено 18 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql
@cls
echo ***** Завершено 19 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ban_hwid.sql
@cls
echo ***** Завершено 20 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql
@cls
echo ***** Завершено 21 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
@cls
echo ***** Завершено 22 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql
@cls
echo ***** Завершено 23 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql
@cls
echo ***** Завершено 24 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql
@cls
echo ***** Завершено 25 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql
@cls
echo ***** Завершено 26 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql
@cls
echo ***** Завершено 27 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql
@cls
echo ***** Завершено 28 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql
@cls
echo ***** Завершено 29 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql
@cls
echo ***** Завершено 30 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql
@cls
echo ***** Завершено 31 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql
@cls
echo ***** Завершено 32 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql
@cls
echo ***** Завершено 33 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql
@cls
echo ***** Завершено 34 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql
@cls
echo ***** Завершено 35 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql
@cls
echo ***** Завершено 36 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql
@cls
echo ***** Завершено 37 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql
@cls
echo ***** Завершено 38 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql
@cls
echo ***** Завершено 39 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql
@cls
echo ***** Завершено 40 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql
@cls
echo ***** Завершено 41 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql
@cls
echo ***** Завершено 42 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql
@cls
echo ***** Завершено 43 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql
@cls
echo ***** Завершено 44 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql
@cls
echo ***** Завершено 45 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql
@cls
echo ***** Завершено 46 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql
@cls
echo ***** Завершено 47 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna.sql
@cls
echo ***** Завершено 48 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql
@cls
echo ***** Завершено 49 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql
@cls
echo ***** Завершено 50 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql
@cls
echo ***** Завершено 51 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql
@cls
echo ***** Завершено 52 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql
@cls
echo ***** Завершено 53 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql
@cls
echo ***** Завершено 54 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mapregion.sql
@cls
echo ***** Завершено 55 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql
@cls
echo ***** Завершено 56 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql
@cls
echo ***** Завершено 57 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql
@cls
echo ***** Завершено 58 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql
@cls
echo ***** Завершено 59 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql
@cls
echo ***** Завершено 60 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql
@cls
echo ***** Завершено 61 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql
@cls
echo ***** Завершено 62 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql
@cls
echo ***** Завершено 63 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql
@cls
echo ***** Завершено 64 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql
@cls
echo ***** Завершено 65 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql
@cls
echo ***** Завершено 66 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql
@cls
echo ***** Завершено 67 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql
@cls
echo ***** Завершено 68 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql
@cls
echo ***** Завершено 70 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql
@cls
echo ***** Завершено 71 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql
@cls
echo ***** Завершено 72 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql
@cls
echo ***** Завершено 73 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql
@cls
echo ***** Завершено 74 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql
@cls
echo ***** Завершено 75 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql
@cls
echo ***** Завершено 76 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql
@cls
echo ***** Завершено 77 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql
@cls
echo ***** Завершено 78 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql
@cls
echo ***** Завершено 79 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql
@cls
echo ***** Завершено 80 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql
@cls
echo ***** Завершено 81 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql
@cls
echo ***** Завершено 82 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql
@cls
echo ***** Завершено 83 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/walker_routes.sql
@cls
echo ***** Завершено 84 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql
@cls
echo ***** Завершено 85 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql
@cls
echo ***** Завершено 86 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql
@cls
echo ***** Завершено 87 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql
@cls
echo ***** Завершено 88 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql
@cls
echo ***** Завершено 89 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql
@cls
echo ***** Завершено 90 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql
@cls
echo ***** Завершено 93 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql
@cls
echo ***** Завершено 94 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lastimperialtomb_spawnlist.sql
@cls
echo ***** Завершено 95 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_functions.sql
@cls
echo ***** Завершено 96 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf.sql
@cls
echo ***** Завершено 97 процентов *****
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql
@cls
echo ***** Завершено 98 процентов *****
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
echo ***** Завершено 100 процентов *****
echo.
echo Cервер игры %title%.
pause
goto :Step1

:end
echo.
echo Установка завершена.
echo.
pause

:fullend
