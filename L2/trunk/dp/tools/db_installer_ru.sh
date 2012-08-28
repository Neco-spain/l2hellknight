#!/bin/bash
############################################
## ВНИМАНИЕ! ВНИМАНИЕ! ВНИМАНИЕ! ВНИМАНИЕ!##
## Установшик БД для *unix систем         ##
############################################
## Сделал Avanege                         ##
## Специально для  eonsw.ru               ##
## Я: http://vkontakte.ru/avanege         ##
############################################
trap finish 2

configure() {
echo "################################################"
echo "# Вы вошли в область конфигурации установщика. #"
echo "# Никаких изменений в вашей базе данных        #"
echo "# производится не будет. Вы лишь ответите на   #"
echo "# вопросы относящихся настройки вашей БД.      #"
echo "#############################################"
MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
MYSQLPATH=`which mysql 2>/dev/null`
if [ $? -ne 0 ]; then
echo "We were unable to find MySQL binaries on your path "
while :
 do
  echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
  read MYSQLBINPATH
    if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
       MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
       MYSQLPATH="$MYSQLBINPATH/mysql"
       break
    else
       echo "The data you entered is invalid. Please verify and try again."
       exit 1
    fi
 done
fi
#LS
echo -ne "\nПожалуйста введите хост MySQL Login Server'а (обычно это localhost): "
read LSDBHOST
if [ -z "$LSDBHOST" ]; then
  LSDBHOST="localhost"
fi
echo -ne "\nПожалуйста введите имя базы данных MySQL Login Server'а (обычно это l2jdb): "
read LSDB
if [ -z "$LSDB" ]; then
  LSDB="l2jdb"
fi
echo -ne "\nПожалуйста введите имя пользователя MySQL Login Server'a (обычно это root): "
read LSUSER
if [ -z "$LSUSER" ]; then
  LSUSER="root"
fi
echo -ne "\nПожалуйста, введите пароль MySQL Login Server'a $LSUSER'а (пароль не будет показан) :"
stty -echo
read LSPASS
stty echo
echo ""
if [ -z "$LSPASS" ]; then
  echo "Hum.. I'll let it be but don't be stupid and avoid empty passwords"
elif [ "$LSUSER" == "$LSPASS" ]; then
  echo "You're not too brilliant choosing passwords huh?"
fi
#GS
echo -ne "\nПожалуйста введите хост MySQL Game Server'а (на Login Server'е у вас $LSDBHOST): "
read GSDBHOST
if [ -z "$GSDBHOST" ]; then
  GSDBHOST="$LSDBHOST"
fi
echo -ne "\nПожалуйста введите имя базы данных MySQL Game Server'а(на Login Server'е у вас $LSDB): "
read GSDB
if [ -z "$GSDB" ]; then
  GSDB="$LSDB"
fi
echo -ne "\nПожалуйста введите имя пользователя  MySQL Game Server'а (на Login Server'е у вас $LSUSER): "
read GSUSER
if [ -z "$GSUSER" ]; then
  GSUSER="$LSUSER"
fi
echo -ne "\nПожалуйста, введите пароль MySQL Game Server $GSUSER'а (пароль не будет показан): "
stty -echo
read GSPASS
stty echo
echo ""
if [ -z "$GSPASS" ]; then
  echo "Hum.. I'll let it be but don't be stupid and avoid empty passwords"
elif [ "$GSUSER" == "$GSPASS" ]; then
  echo "You're not too brilliant choosing passwords huh?"
fi
save_config $1
}

save_config() {
if [ -n "$1" ]; then
CONF="$1"
else 
CONF="database_installer.rc"
fi
echo ""
echo "Из этих данных можно создать файл конфигурации,"
echo "который может быть прочитан. ПРЕДУПРЕЖДЕНИЕ: этот файл будет содержать пароли."
echo -ne "Создать файл конфигурации $CONF? (Y/n):"
read SAVE
if [ "$SAVE" == "y" -o "$SAVE" == "Y" -o "$SAVE" == "" ];then 
cat <<EOF>$CONF
#Configuration settings for L2J-Datapack database installer script
MYSQLDUMPPATH=$MYSQLDUMPPATH
MYSQLPATH=$MYSQLPATH
LSDBHOST=$LSDBHOST
LSDB=$LSDB
LSUSER=$LSUSER
LSPASS=$LSPASS
GSDBHOST=$GSDBHOST
GSDB=$GSDB
GSUSER=$GSUSER
GSPASS=$GSPASS
EOF
chmod 600 $CONF
echo "Configuration saved as $CONF"
echo "Permissions changed to 600 (rw- --- ---)"
elif [ "$SAVE" != "n" -a "$SAVE" != "N" ]; then
  save_config
fi
}

load_config() {
if [ -n "$1" ]; then
CONF="$1"
else 
CONF="database_installer.rc"
fi
if [ -e "$CONF" ] && [ -f "$CONF" ]; then
. $CONF
else
echo "Settings file not found: $CONF"
echo "You can specify an alternate settings filename:"
echo $0 config_filename
echo ""
echo "If file doesn't exist it can be created"
echo "If nothing is specified script will try to work with ./database_installer.rc"
echo ""
configure $CONF
fi
}

asklogin(){
echo "#############################################"
echo "# ПРЕДУПРЕЖДЕНИЕ: Этот этап установки может #"
echo "# очистить таблицы characters и accounts    #"
echo "# Будте внимательны, прочитайте описание    #"
echo "# перед тем как выполнить действие          #"
echo "#############################################"
echo ""
echo "Полная устновка (f) если у вас нету записей в таблице 'accounts' "
echo "Будет удалена вся информация с таблицы 'accounts' ."
echo "Пропустить полную установку (s) и перейти к "
echo "установке базы данных геймсервера"
echo -ne "Устновка ДБ Login Server'а : (f) полная устновка, (s) пропустить (q) выйти? "
read LOGINPROMPT
case "$LOGINPROMPT" in
	"f"|"F") logininstall; loginupgrade; gsbackup; asktype;;
	"s"|"S") gsbackup; asktype;;
	"q"|"Q") finish;;
	*) asklogin;;
esac
}

logininstall(){
echo "Deleting loginserver tables for new content."
#$MYL < login_install.sql &> /dev/null
$MYSQLDUMPPATH --add-drop-table -h $LSDBHOST -u $LSUSER --password=$LSPASS $LSDB --no-data | grep ^DROP | $MYL

}

loginupgrade(){
echo "Очиска базы данных и установка БД LoginServer'а"
$MYL < ../sql/accounts.sql &> /dev/null
$MYL < ../sql/gameservers.sql &> /dev/null
$MYL < ../sql/account_data.sql &> /dev/null
}

gsbackup(){
while :
  do
   echo ""
   echo -ne "Сохранить бкап базы данных Game Server'а ? (y/n): "
   read LSB
   if [ "$LSB" == "Y" -o "$LSB" == "y" ]; then
     echo "Making a backup of the original gameserver database."
     $MYSQLDUMPPATH --add-drop-table -h $GSDBHOST -u $GSUSER --password=$GSPASS $GSDB > gameserver_backup.sql
     if [ $? -ne 0 ];then
     echo ""
     echo "There was a problem accesing your GS database, either it wasnt created or authentication data is incorrect."
     exit 1
     fi
     break
   elif [ "$LSB" == "n" -o "$LSB" == "N" ]; then 
     break
   fi
  done 
}

lsbackup(){
while :
  do
   echo ""
   echo -ne "Сохранить бекап базы данных Login Server'а ? (y/n): "
   read LSB
   if [ "$LSB" == "Y" -o "$LSB" == "y" ]; then
     echo "Making a backup of the original loginserver database."
     $MYSQLDUMPPATH --add-drop-table -h $LSDBHOST -u $LSUSER --password=$LSPASS $LSDB > loginserver_backup.sql
     if [ $? -ne 0 ];then
        echo ""
        echo "There was a problem accesing your LS database, either it wasnt created or authentication data is incorrect."
        exit 1
     fi
     break
   elif [ "$LSB" == "n" -o "$LSB" == "N" ]; then 
     break
   fi
  done 
}

asktype(){
echo ""
echo ""
echo "ПРЕДУПРЕЖДЕНИЕ: Полная устновка (f) удалит всю информацию о персанажах на сервере."
echo -ne "Устновка  ДБ Game Servera'а : (f) полная устновка, (s) пропустить (q) выйти? "
read INSTALLTYPE
case "$INSTALLTYPE" in
	"f"|"F") fullinstall; upgradeinstall I;;
	"q"|"Q") finish;;
	*) asktype;;
esac
}

fullinstall(){
echo "Устновка новых таблиц Game Server'а"
#$MYG < full_install.sql &> /dev/null

}

upgradeinstall(){
if [ "$1" == "I" ]; then 
echo "Установка новых таблиц."
else
echo "Обновление таблиц"
fi
#if [ "$1" == "I" ]; then
$MYG < ../sql/account_data.sql &> /dev/null
$MYG < ../sql/armor.sql &> /dev/null
$MYG < ../sql/armorsets.sql &> /dev/null
$MYG < ../sql/auction.sql &> /dev/null
$MYG < ../sql/auction_bid.sql &> /dev/null
$MYG < ../sql/auction_watch.sql &> /dev/null
$MYG < ../sql/augmentations.sql &> /dev/null
$MYG < ../sql/auto_chat.sql &> /dev/null
$MYG < ../sql/auto_chat_text.sql &> /dev/null
$MYG < ../sql/boxaccess.sql &> /dev/null
$MYG < ../sql/boxes.sql &> /dev/null
$MYG < ../sql/castle.sql &> /dev/null
$MYG < ../sql/castle_door.sql &> /dev/null
$MYG < ../sql/castle_doorupgrade.sql &> /dev/null
$MYG < ../sql/castle_siege_guards.sql &> /dev/null
$MYG < ../sql/char_templates.sql &> /dev/null
$MYG < ../sql/character_friends.sql &> /dev/null
$MYG < ../sql/character_hennas.sql &> /dev/null
$MYG < ../sql/character_macroses.sql &> /dev/null
$MYG < ../sql/character_quests.sql &> /dev/null
$MYG < ../sql/character_recipebook.sql &> /dev/null
$MYG < ../sql/character_recommends.sql &> /dev/null
$MYG < ../sql/character_shortcuts.sql &> /dev/null
$MYG < ../sql/character_skills.sql &> /dev/null
$MYG < ../sql/character_skills_save.sql &> /dev/null
$MYG < ../sql/character_subclasses.sql &> /dev/null
$MYG < ../sql/characters.sql &> /dev/null
$MYG < ../sql/clan_data.sql &> /dev/null
$MYG < ../sql/clan_privs.sql &> /dev/null
$MYG < ../sql/clan_skills.sql &> /dev/null
$MYG < ../sql/clan_subpledges.sql &> /dev/null
$MYG < ../sql/clan_wars.sql &> /dev/null
$MYG < ../sql/clanhall.sql &> /dev/null
$MYG < ../sql/clanhall_functions.sql &> /dev/null
$MYG < ../sql/class_list.sql &> /dev/null
$MYG < ../sql/cursed_weapons.sql &> /dev/null
$MYG < ../sql/dimensional_rift.sql &> /dev/null
$MYG < ../sql/droplist.sql &> /dev/null
$MYG < ../sql/enchant_skill_trees.sql &> /dev/null
$MYG < ../sql/etcitem.sql &> /dev/null
$MYG < ../sql/fish.sql &> /dev/null
$MYG < ../sql/fishing_skill_trees.sql &> /dev/null
$MYG < ../sql/forums.sql &> /dev/null
$MYG < ../sql/games.sql &> /dev/null
$MYG < ../sql/global_tasks.sql &> /dev/null
$MYG < ../sql/helper_buff_list.sql &> /dev/null
$MYG < ../sql/henna.sql &> /dev/null
$MYG < ../sql/henna_trees.sql &> /dev/null
$MYG < ../sql/heroes.sql &> /dev/null
$MYG < ../sql/items.sql &> /dev/null
$MYG < ../sql/itemsonground.sql &> /dev/null
$MYG < ../sql/locations.sql &> /dev/null
$MYG < ../sql/lvlupgain.sql &> /dev/null
$MYG < ../sql/mapregion.sql &> /dev/null
$MYG < ../sql/merchant_areas_list.sql &> /dev/null
$MYG < ../sql/merchant_buylists.sql &> /dev/null
$MYG < ../sql/merchant_lease.sql &> /dev/null
$MYG < ../sql/merchant_shopids.sql &> /dev/null
$MYG < ../sql/merchants.sql &> /dev/null
$MYG < ../sql/minions.sql &> /dev/null
$MYG < ../sql/mods_wedding.sql &> /dev/null
$MYG < ../sql/npc.sql &> /dev/null
$MYG < ../sql/npcskills.sql &> /dev/null
$MYG < ../sql/olympiad_nobles.sql &> /dev/null
$MYG < ../sql/pets.sql &> /dev/null
$MYG < ../sql/pets_stats.sql &> /dev/null
$MYG < ../sql/pledge_skill_trees.sql &> /dev/null
$MYG < ../sql/posts.sql &> /dev/null
$MYG < ../sql/raidboss_spawnlist.sql &> /dev/null
$MYG < ../sql/random_spawn.sql &> /dev/null
$MYG < ../sql/random_spawn_loc.sql &> /dev/null
$MYG < ../sql/seven_signs.sql &> /dev/null
$MYG < ../sql/seven_signs_festival.sql &> /dev/null
$MYG < ../sql/seven_signs_status.sql &> /dev/null
$MYG < ../sql/siege_clans.sql &> /dev/null
$MYG < ../sql/skill_learn.sql &> /dev/null
$MYG < ../sql/skill_spellbooks.sql &> /dev/null
$MYG < ../sql/skill_trees.sql &> /dev/null
$MYG < ../sql/spawnlist.sql &> /dev/null
$MYG < ../sql/teleport.sql &> /dev/null
$MYG < ../sql/topic.sql &> /dev/null
$MYG < ../sql/walker_routes.sql &> /dev/null
$MYG < ../sql/weapon.sql &> /dev/null
$MYG < ../sql/zone_vertices.sql &> /dev/null
$MYG < ../sql/grandboss_data.sql &> /dev/null
$MYG < ../sql/quest_global_data.sql &> /dev/null
$MYG < ../sql/grandboss_list.sql &> /dev/null
$MYG < ../sql/four_sepulchers_spawnlist.sql &> /dev/null
$MYG < ../sql/castle_manor_procure.sql &> /dev/null
$MYG < ../sql/castle_manor_production.sql &> /dev/null
$MYG < ../sql/vanhalter_spawnlist.sql &> /dev/null
$MYG < ../sql/lastimperialtomb_spawnlist.sql &> /dev/null
$MYG < ../sql/castle_functions.sql &> /dev/null
$MYG < ../sql/ctf.sql &> /dev/null
$MYG < ../sql/ctf_teams.sql &> /dev/null
$MYG < ../sql/clan_notices.sql &> /dev/null
$MYG < ../sql/clan_news.sql &> /dev/null
$MYG < ../sql/account_premium.sql &> /dev/null
$MYG < ../sql/clanhall_siege.sql &> /dev/null
$MYG < ../sql/character_buff_profiles.sql &> /dev/null
$MYG < ../sql/character_raid_points.sql &> /dev/null
$MYG < ../sql/fort.sql &> /dev/null
$MYG < ../sql/fort_door.sql &> /dev/null
$MYG < ../sql/fort_doorupgrade.sql &> /dev/null
$MYG < ../sql/fort_siege_guards.sql &> /dev/null
$MYG < ../sql/fortsiege_clans.sql &> /dev/null
$MYG < ../sql/announce_records.sql &> /dev/null
$MYG < ../sql/l2top_votes.sql &> /dev/null
fulfinish
}

fulfinish(){
echo "Полная установка таблиц выполнена! Выйти? (y/n) "
read LOGINPROMPT
case "$LOGINPROMPT" in
	"y"|"Y") finish;;
	"n"|"N") asklogin;;
	*) asklogin;;
esac
}

finish(){
echo ""
echo "Скрипт выполнен"
exit 0
}

clear
load_config $1
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"
lsbackup
asklogin

