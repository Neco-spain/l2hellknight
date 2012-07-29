package services.community;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.AccountBonusDAO;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.handler.bbs.CommunityBoardManager;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.serverpackets.ExBR_PremiumState;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quests._234_FatesWhisper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.StringTokenizer;

public class CommunityBoard implements ScriptFile, ICommunityBoardHandler {
    private static final Logger _log = LoggerFactory.getLogger(CommunityBoard.class);

    @Override
    public void onLoad() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            _log.info("CommunityBoard: service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public void onReload() {
        if (Config.COMMUNITYBOARD_ENABLED)
            CommunityBoardManager.getInstance().removeHandler(this);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbshome", "_bbslink", "_bbsmultisell", "_bbspage", "_bbsscripts", "_bbsservices", "_bbsbuff"};
    }

    @Override
    public void onBypassCommand(Player player, String bypass) {
        StringTokenizer st = new StringTokenizer(bypass, "_");
        String cmd = st.nextToken();
        String html = "";
        if ("bbshome".equals(cmd)) {
            StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT, "_");
            String dafault = p.nextToken();
            if (dafault.equals(cmd)) {
                html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_top.htm", player);

                int favCount = 0;
                Connection con = null;
                PreparedStatement statement = null;
                ResultSet rset = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
                    statement.setInt(1, player.getObjectId());
                    rset = statement.executeQuery();
                    if (rset.next())
                        favCount = rset.getInt("cnt");
                } catch (Exception e) {
                } finally {
                    DbUtils.closeQuietly(con, statement, rset);
                }

                html = html.replace("<?fav_count?>", String.valueOf(favCount));
                html = html.replace("<?clan_count?>", String.valueOf(ClanTable.getInstance().getClans().length));
                html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
                html = cbReplaceMacro(html, player);
            } else {
                onBypassCommand(player, Config.BBS_DEFAULT);
                return;
            }
        } else if ("bbslink".equals(cmd))
            html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_homepage.htm", player);
        else if (bypass.startsWith("_bbspage")) {
            //Example: "bypass _bbspage:index".
            String[] b = bypass.split(":");
            String page = b[1];
            html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/" + page + ".htm", player);
            html = cbReplaceMacro(html, player);
            if (b[1].startsWith("services"))
                html = cbServicesMacro(html, b[2], player);
            else if (b[1].startsWith("buffer"))
                html = cbBufferMacro(html, b[2], player);
        } else if (bypass.startsWith("_bbsmultisell")) {
            //Example: "_bbsmultisell:10000;_bbspage:index" or "_bbsmultisell:10000;_bbshome" or "_bbsmultisell:10000"...
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String[] mBypass = st2.nextToken().split(":");
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if (pBypass != null) {
                ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                if (handler != null)
                    handler.onBypassCommand(player, pBypass);
            }

            int listId = Integer.parseInt(mBypass[1]);
            MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
            return;
        } else if (bypass.startsWith("_bbsscripts")) {
            //Example: "_bbsscripts:events.GvG.GvG:addGroup;_bbspage:index" or "_bbsscripts:events.GvG.GvG:addGroup;_bbshome" or "_bbsscripts:events.GvG.GvG:addGroup"...
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String sBypass = st2.nextToken().substring(12);
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if (pBypass != null) {
                ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                if (handler != null)
                    handler.onBypassCommand(player, pBypass);
            }

            String[] word = sBypass.split("\\s+");
            String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
            String[] path = word[0].split(":");
            if (path.length != 2)
                return;

            Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[]{} : new Object[]{args});
            return;
        } else if (bypass.startsWith("_bbsbuff")) {
            CBBuffManager.parsecmd(bypass, player);
            return;
        } else if (bypass.startsWith("_bbsservices")) {
            //Example: "bypass _bbsservices:changecolor:hexcolor;_bbspage;index" or "bypass _bbsservices:changecolor:hexcolor;_bbshome" or bypass "_bbsservices:changecolor:hexcolor"
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String[] sBypass = st2.nextToken().split(":");
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if (pBypass != null) {
                ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                if (handler != null)
                    handler.onBypassCommand(player, pBypass);
            }

            if (sBypass[1].startsWith("changecolor")) {
                if (player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_NICK_COLOR_ITEM, Config.SERVICES_CHANGE_NICK_COLOR_PRICE)) {
                    String hexcolor = sBypass[2];
                    player.setNameColor(Integer.parseInt(hexcolor, 16));
                    player.broadcastUserInfo(true);
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            } else if (sBypass[1].startsWith("changename")) {
                String newName = sBypass[2].trim();
                if (mysql.simple_get_int("count(*)", "characters", "`char_name` like '" + newName + "'") > 0) {
                    player.sendMessage("Имя персонажа уже занято.");
                    return;
                }

                if (player.getInventory().getCountOf(Config.SERVICES_CHANGE_NICK_ITEM) < Config.SERVICES_CHANGE_NICK_PRICE) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                    return;
                }

                player.reName(newName);
                player.sendMessage("Вы изменили свое имя.");
                player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_NICK_ITEM, Config.SERVICES_CHANGE_NICK_PRICE);
            } else if (sBypass[1].startsWith("changepetname")) {
                String newPetName = sBypass[2].trim();
                PetInstance pet = player.getSummonList().getPet();
                if (pet == null) {
                    player.sendMessage("Необходимо вызвать питомца перед сменой его имени.");
                    return;
                }
                if (pet.isDefaultName()) {
                    if (newPetName.length() < 1 || newPetName.length() > 8) {
                        player.sendPacket(Msg.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS);
                        return;
                    }
                    if (player.getInventory().getCountOf(Config.SERVICES_CHANGE_PET_NAME_ITEM) < Config.SERVICES_CHANGE_PET_NAME_PRICE) {
                        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                        return;
                    }

                    pet.setName(newPetName);
                    pet.broadcastCharInfo();
                    pet.updateControlItem();
                    player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_PET_NAME_ITEM, Config.SERVICES_CHANGE_PET_NAME_PRICE);
                }
            } else if (sBypass[1].startsWith("changesex")) {
                if (player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_SEX_ITEM, Config.SERVICES_CHANGE_SEX_PRICE)) {
                    player.changeSex();
                    player.sendMessage("Ваш пол изменен.");
                    player.broadcastUserInfo(true);
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            } else if (sBypass[1].startsWith("delevel")) {
                if (player.getInventory().getCountOf(Config.SERVICES_DELEVEL_ITEM) < Config.SERVICES_DELEVEL_PRICE) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                    return;
                }

                if (player.getLevel() > 1)
                {
                    long exp_add = Experience.LEVEL[player.getLevel()-1] - player.getExp();
                    player.addExpAndSp(exp_add, 0);
                    player.broadcastUserInfo(true);
                    player.sendMessage("Ваш уровень понижен.");
                }
                else
                    return;
            } else if (sBypass[1].startsWith("buynoble")) {
                if (player.isNoble()) {
                    player.sendMessage("Вы уже являетесь дворянином.");
                    return;
                }

                if (player.getSubLevel() < 75) {
                    player.sendMessage("Вам необходимо сначала добавить сабкласс.");
                    return;
                }

                if (player.getInventory().destroyItemByItemId(Config.SERVICES_NOBLESS_SELL_ITEM, Config.SERVICES_NOBLESS_SELL_PRICE)) {
                    Quest q = QuestManager.getQuest(_234_FatesWhisper.class);
                    QuestState qs = player.getQuestState(q.getClass());
                    if (qs != null)
                        qs.exitCurrentQuest(true);
                    q.newQuestState(player, Quest.COMPLETED);

                    if (player.getRace() == Race.kamael) {
                        q = QuestManager.getQuest("_236_SeedsOfChaos");
                        qs = player.getQuestState(q.getClass());
                        if (qs != null)
                            qs.exitCurrentQuest(true);
                        q.newQuestState(player, Quest.COMPLETED);
                    } else {
                        q = QuestManager.getQuest("_235_MimirsElixir");
                        qs = player.getQuestState(q.getClass());
                        if (qs != null)
                            qs.exitCurrentQuest(true);
                        q.newQuestState(player, Quest.COMPLETED);
                    }
                    Olympiad.addNoble(player);
                    player.setNoble(true);
                    player.updatePledgeClass();
                    player.updateNobleSkills();
                    player.sendSkillList();
                    player.broadcastUserInfo(true);
                    player.sendMessage("Вы получили статус дворянина.");
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            } else if (sBypass[1].startsWith("changeclanname")) {
                if (player.getInventory().getCountOf(Config.SERVICES_CHANGE_CLAN_NAME_ITEM) < Config.SERVICES_CHANGE_CLAN_NAME_PRICE) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                    return;
                }

                String newName = sBypass[2].trim();
                if (player.getClan() == null || !player.isClanLeader()) {
                    player.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addName(player));
                    return;
                }

                if (!Util.isMatchingRegexp(newName, Config.CLAN_NAME_TEMPLATE)) {
                    player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
                    return;
                }
                if (ClanTable.getInstance().getClanByName(newName) != null) {
                    player.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
                    return;
                }

                SubUnit sub = player.getClan().getSubUnit(Clan.SUBUNIT_MAIN_CLAN);
                sub.setName(newName, true);
                player.sendMessage("Вы изменили имя клана.");
                player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_NICK_ITEM, Config.SERVICES_CHANGE_NICK_PRICE);
            } else if (sBypass[1].startsWith("premium")) {
                int premiumIndex = Integer.parseInt(sBypass[2]);

                double bonus = Config.SERVICES_RATE_BONUS_VALUE[premiumIndex];
                int bonusExpire = (int) (System.currentTimeMillis() / 1000L) + Config.SERVICES_RATE_BONUS_DAYS[premiumIndex] * 24 * 60 * 60;

                AccountBonusDAO.getInstance().insert(player.getAccountName(), bonus, bonusExpire);

                player.getNetConnection().setBonus(bonus);
                player.getNetConnection().setBonusExpire(bonusExpire);

                player.stopBonusTask();
                player.startBonusTask();

                if (player.getParty() != null)
                    player.getParty().recalculatePartyData();

                player.sendPacket(new ExBR_PremiumState(player, true));

                player.sendMessage("Для вашего аккаунта установлены премиум-рейты.");
            }
            if (sBypass[1].startsWith("expandinventory")) {
                if (player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX) {
                    player.sendMessage("У вас максимальный размер инвентаря.");
                    return;
                }

                if (player.getInventory().destroyItemByItemId(Config.SERVICES_EXPAND_INVENTORY_ITEM, Config.SERVICES_EXPAND_INVENTORY_PRICE)) {
                    player.setExpandInventory(player.getExpandInventory() + 1);
                    player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
                    player.sendMessage("Размер вашего инвентаря: " + player.getInventoryLimit());
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            }
            if (sBypass[1].startsWith("expandwh")) {
                if (player.getInventory().destroyItemByItemId(Config.SERVICES_EXPAND_WAREHOUSE_ITEM, Config.SERVICES_EXPAND_WAREHOUSE_PRICE)) {
                    player.setExpandWarehouse(player.getExpandWarehouse() + 1);
                    player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
                    player.sendMessage("Размер склада увеличен до " + player.getWarehouseLimit());
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            }
            if (sBypass[1].startsWith("expandcwh")) {
                if (player.getClan() == null && player.isClanLeader()) {
                    player.sendMessage("Вы должны быть в клане и являться его лидером!");
                    return;
                }

                if (player.getInventory().destroyItemByItemId(Config.SERVICES_EXPAND_CWH_ITEM, Config.SERVICES_EXPAND_CWH_PRICE)) {
                    player.getClan().setWhBonus(player.getClan().getWhBonus() + 1);
                    player.sendMessage("Размер склада клана увеличен до " + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus()));
                } else
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            }
            return;
        }

        ShowBoard.separateAndSend(html, player);
    }

    public static String cbReplaceMacro(String htmltext, Player player) {
        String menuBlock = HtmCache.getInstance().getNotNull("scripts/services/community/blocks/menu.htm", player);
        String copyrightBlock = HtmCache.getInstance().getNotNull("scripts/services/community/blocks/copyright.htm", player);
        htmltext = htmltext.replace("%menu%", menuBlock);
        htmltext = htmltext.replace("%services%", Config.ALLOW_CB_SERVICES ? "<td><button value=\"Сервисы\" action=\"bypass _bbspage:services:index\" width=88 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>" : "");
        htmltext = htmltext.replace("%shop%", Config.ALLOW_CB_SHOP ? "<td><button value=\"Магазин\" action=\"bypass _bbspage:shop\" width=88 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>" : "");
        htmltext = htmltext.replace("%buffer%", Config.ALLOW_CB_BUFFER ? "<td><button value=\"Бафер\" action=\"bypass _bbspage:buffer:index\" width=88 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>" : "");
        htmltext = htmltext.replace("%copyright%", copyrightBlock);
        return htmltext;
    }

    private String cbServicesMacro(String htmltext, String selectedPage, Player player) {
        String htmlServicesLeftMenu = HtmCache.getInstance().getNotNull("scripts/services/community/blocks/services-leftmenu.htm", player);
        String htmlServicesPage = HtmCache.getInstance().getNotNull("scripts/services/community/pages/services/" + selectedPage + ".htm", player);
        htmltext = htmltext.replace("%services_leftmenu%", htmlServicesLeftMenu);
        htmltext = htmltext.replace("%nickname%", Config.SERVICES_CHANGE_NICK_ENABLED ? "<tr><td align=center><button value=\"Ник чара\" action=\"bypass _bbspage:services:nickname\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%delevel%", Config.SERVICES_CHANGE_NICK_ENABLED ? "<tr><td align=center><button value=\"Делевел\" action=\"bypass _bbspage:services:delevel\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%petname%", Config.SERVICES_CHANGE_PET_NAME_ENABLED ? "<tr><td align=center><button value=\"Ник петомца\" action=\"bypass _bbspage:services:petname\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%sexchange%", Config.SERVICES_CHANGE_SEX_ENABLED ? "<tr><td align=center><button value=\"Пол чара\" action=\"bypass _bbspage:services:sexchange\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%noble%", Config.SERVICES_NOBLESS_SELL_ENABLED ? "<tr><td align=center><button value=\"Ноблес\" action=\"bypass _bbspage:services:noble\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%clanname%", Config.SERVICES_CHANGE_CLAN_NAME_ENABLED ? "<tr><td align=center><button value=\"Имя клана\" action=\"bypass _bbspage:services:clanname\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%premium%", Config.SERVICES_RATE_TYPE > 0 ? "<tr><td align=center><button value=\"Премиум\" action=\"bypass _bbspage:services:premium\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%charinventory%", Config.SERVICES_EXPAND_INVENTORY_ENABLED ? "<tr><td align=center><button value=\"Инвентарь\" action=\"bypass _bbspage:services:inventory\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%ownwh%", Config.SERVICES_EXPAND_WAREHOUSE_ENABLED ? "<tr><td align=center><button value=\"Личный склад\" action=\"bypass _bbspage:services:ownwh\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");
        htmltext = htmltext.replace("%clanwh%", Config.SERVICES_EXPAND_CWH_ENABLED && player.getClan() != null ? "<tr><td align=center><button value=\"Склад клана\" action=\"bypass _bbspage:services:clanwh\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>" : "");

        htmltext = htmltext.replace("%servicebody%", htmlServicesPage);

        if (selectedPage.startsWith("nickname")) {
            ItemTemplate CHANGE_NAME_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_ITEM);
            ItemTemplate CHANGE_COLOR_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_ITEM);
            htmltext = htmltext.replace("%change_name_price%", Config.SERVICES_CHANGE_NICK_PRICE + " " + CHANGE_NAME_ITEM.getName());
            htmltext = htmltext.replace("%change_name_color_price%", Config.SERVICES_CHANGE_NICK_COLOR_PRICE + " " + CHANGE_COLOR_ITEM.getName());
            StringBuilder colorTable = new StringBuilder();
            int i = 0;
            for (String color : Config.SERVICES_CHANGE_NICK_COLOR_LIST) {
                colorTable.append("<td align=center width=100 height=20><a action=\"bypass _bbsservices:changecolor:").append(color).append("\"><font color=\"").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("\">").append(player.getName()).append("</font></a></td>");
                i++;
                if (i % 3 == 0)
                    colorTable.append("</tr><tr>");
            }

            htmltext = htmltext.replace("%change_name_color_table%", colorTable.toString());
        }
        if (selectedPage.startsWith("petname")) {
            ItemTemplate CHANGE_PETNAME_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_PET_NAME_ITEM);
            htmltext = htmltext.replace("%change_petname_price%", Config.SERVICES_CHANGE_PET_NAME_PRICE + " " + CHANGE_PETNAME_ITEM.getName());
        }
        if (selectedPage.startsWith("sexchange")) {
            ItemTemplate CHANGE_SEX_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_SEX_ITEM);
            htmltext = htmltext.replace("%change_sex_price%", Config.SERVICES_CHANGE_SEX_PRICE + " " + CHANGE_SEX_ITEM.getName());
        }
        if (selectedPage.startsWith("noble")) {
            ItemTemplate BUY_NOBLE_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_NOBLESS_SELL_ITEM);
            htmltext = htmltext.replace("%change_noble_price%", Config.SERVICES_NOBLESS_SELL_PRICE + " " + BUY_NOBLE_ITEM.getName());
        }
        if (selectedPage.startsWith("clanname")) {
            ItemTemplate CHANGE_CLANNAME_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_CLAN_NAME_ITEM);
            htmltext = htmltext.replace("%change_clanname_price%", Config.SERVICES_CHANGE_CLAN_NAME_PRICE + " " + CHANGE_CLANNAME_ITEM.getName());
        }
        if (selectedPage.startsWith("premium")) {
            int endtime = player.getNetConnection().getBonusExpire();
            if (endtime >= System.currentTimeMillis() / 1000L)
                htmltext = htmltext.replace("%premium_menu%", "У вас уже есть премиум до " + new Date(endtime * 1000L).toString());
            else {
                String add = "";
                for (int i = 0; i < Config.SERVICES_RATE_BONUS_DAYS.length; i++)
                    add += "<tr><td align=center><a action=\"bypass _bbsservices:buynoble:" + i + ";_bbspage:services:premium\">"
                            + "x" + (int) (Config.SERVICES_RATE_BONUS_VALUE[i] * Config.RATE_XP) +
                            " на " + Config.SERVICES_RATE_BONUS_DAYS[i] +
                            " дня(дней) - " + Config.SERVICES_RATE_BONUS_PRICE[i] +
                            " " + ItemHolder.getInstance().getTemplate(Config.SERVICES_RATE_BONUS_ITEM[i]).getName() + "</a></td></tr>";

                htmltext = htmltext.replace("%premium_menu%", add);
            }
        }
        if (selectedPage.startsWith("inventory")) {
            ItemTemplate EXPAND_INVENTORY_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_INVENTORY_ITEM);
            htmltext = htmltext.replace("%inventorylimit%", " " + player.getInventoryLimit());
            htmltext = htmltext.replace("%maxinvsize%", " " + Config.SERVICES_EXPAND_INVENTORY_MAX);
            htmltext = htmltext.replace("%expandprice%", Config.SERVICES_EXPAND_INVENTORY_PRICE + " " + EXPAND_INVENTORY_ITEM.getName());
        }
        if (selectedPage.startsWith("ownwh")) {
            ItemTemplate EXPAND_WH_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_WAREHOUSE_ITEM);
            htmltext = htmltext.replace("%whlimit%", " " + player.getWarehouseLimit());
            htmltext = htmltext.replace("%expandprice%", Config.SERVICES_EXPAND_WAREHOUSE_PRICE + " " + EXPAND_WH_ITEM.getName());
        }
        if (selectedPage.startsWith("clanwh")) {
            ItemTemplate EXPAND_CWH_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_CWH_ITEM);
            htmltext = htmltext.replace("%cwhlimit%", " " + (Config.WAREHOUSE_SLOTS_CLAN + player.getClan().getWhBonus() + 1));
            htmltext = htmltext.replace("%expandprice%", Config.SERVICES_EXPAND_CWH_PRICE + " " + EXPAND_CWH_ITEM.getName());
        }
        if (selectedPage.startsWith("delevel")) {
            ItemTemplate DELEVEL_ITEM = ItemHolder.getInstance().getTemplate(Config.SERVICES_DELEVEL_ITEM);
            htmltext = htmltext.replace("%delevel_price%", Config.SERVICES_DELEVEL_PRICE + " " + DELEVEL_ITEM.getName());
        }

        return htmltext;
    }

    public static String cbBufferMacro(String htmltext, String selectedPage, Player player) {
        String profilesHtml = CBBuffManager.getProfiles(player);
        String htmlBufferPage = HtmCache.getInstance().getNotNull("scripts/services/community/pages/buffer/" + selectedPage + ".htm", player);
        htmltext = htmltext.replace("%buffer_profiles%", profilesHtml);
        htmltext = htmltext.replace("%buffer_page%", htmlBufferPage);

        return htmltext;
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
    }
}
