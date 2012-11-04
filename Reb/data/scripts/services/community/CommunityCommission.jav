package services.community;

import org.apache.commons.lang3.ArrayUtils;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.ItemHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.items.PcInventory;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.triggers.TriggerInfo;
import l2r.gameserver.tables.AugmentationData;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.ItemFunctions;
import l2r.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author RuleZzz
 */
public class CommunityCommission extends Functions implements ScriptFile, ICommunityBoardHandler {
    private static final Logger _log = LoggerFactory.getLogger(CommunityCommission.class);

    @Override
    public String[] getBypassCommands() {
        return new String[]{
                "_cbbcommission",
        };
    }

    @Override
    public void onBypassCommand(Player player, String bypass) {
        if (player == null)
            return;

        StringTokenizer st = new StringTokenizer(bypass, ":");
        st.nextToken();
        String action = st.hasMoreTokens() ? st.nextToken() : "main";

        if (action.equals("main"))
            showPage(player, 1, "all", "");
        else if (action.startsWith("list-")) {
            StringTokenizer list = new StringTokenizer(bypass, "-");
            list.nextToken();
            String category = list.hasMoreTokens() ? list.nextToken() : "all";
            int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
            showPage(player, page, category, "");
        } else if (action.startsWith("search-")) {
            StringTokenizer list = new StringTokenizer(bypass, "-");
            list.nextToken();
            String search = list.hasMoreTokens() ? list.nextToken().trim() : "";
            search = strip(search);
            if (search.length() < 2) {
                player.sendMessage(player.isLangRus() ? "Слово для поиска должно содержать как минимум 2 символа" : "Search term must contain at least 2 characters");
                return;
            }
            int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
            showPage(player, page, "search", search);
        } else if (action.startsWith("show-")) {
            StringTokenizer stid = new StringTokenizer(bypass, "-");
            stid.nextToken();
            int id;
            try {
                id = Integer.parseInt(stid.nextToken());
            } catch (Exception e) {
                return;
            }
            showItem(player, id);
        } else if (action.startsWith("add-")) {
            StringTokenizer stid = new StringTokenizer(bypass, "-");
            stid.nextToken();
            int id;
            try {
                id = Integer.parseInt(stid.nextToken());
            } catch (Exception e) {
                return;
            }
            addProduct(player, id);
        } else if (action.startsWith("select-")) {
            StringTokenizer stid = new StringTokenizer(bypass, "-");
            stid.nextToken();
            int objId;
            try {
                objId = Integer.parseInt(stid.nextToken());
            } catch (Exception e) {
                return;
            }
            selectProduct(player, objId);
        } else if (action.startsWith("create-")) {
            StringTokenizer stid = new StringTokenizer(bypass, "-");
            stid.nextToken();
            int objId;
            String price;
            int price_id;
            long price_count;
            long item_count;
            try {
                objId = Integer.parseInt(stid.nextToken());
                price = stid.nextToken().trim();
                price_count = Long.parseLong(stid.nextToken().trim());
                item_count = Integer.parseInt(stid.nextToken().trim());
            } catch (Exception e) {
                return;
            }
            price_id = getPriceId(price);
            if (price_id == 0) return;
            createProduct(player, objId, price_id, price_count, item_count);
        } else if (action.startsWith("get-")) {
            StringTokenizer stid = new StringTokenizer(bypass, "-");
            stid.nextToken();
            int id;
            try {
                id = Integer.parseInt(stid.nextToken());
            } catch (Exception e) {
                return;
            }
            getItem(player, id);
            showPage(player, 1, "all", "");
        }
    }

    private void createProduct(Player player, int objId, int price_id, long price_count, long item_count) {
        ItemInstance item = player.getInventory().getItemByObjectId(objId);
        if (item == null || !checkItem(item))
            return;

        if (item.getCount() < item_count && item.getCount() - item_count < 0) {
            player.sendMessage((player.isLangRus() ? "Максимальное количество, которое можно выставить на продажу равняется " : "The maximum amount that can be put up for sale is equal to ") + item.getCount());
            return;
        }

        int need_item_id = 0;
        int need_item_count = 0;
        if (item.isArmor()) {
            need_item_id = Config.BBS_COMMISSION_ARMOR_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_ARMOR_PRICE[1];
        } else if (item.isWeapon()) {
            need_item_id = Config.BBS_COMMISSION_WEAPON_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_WEAPON_PRICE[1];
        } else if (item.isAccessory()) {
            need_item_id = Config.BBS_COMMISSION_JEWERLY_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_JEWERLY_PRICE[1];
        } else {
            need_item_id = Config.BBS_COMMISSION_OTHER_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_OTHER_PRICE[1];
        }

        if (need_item_id > 0 && need_item_count > 0)
            if(!checkHaveItem(player, need_item_id, need_item_count)) {
                player.sendMessage((player.isLangRus() ? "Недостаточное количество " : "Insufficient number ") + ItemFunctions.getName(need_item_id, false) + (player.isLangRus() ? " для оплаты выставления товара на продажу" : " to pay for placing the goods on sale"));
                return;
            } else
                Functions.removeItem(player, need_item_id, need_item_count);

        int item_id = item.getItemId();
        int enchant_level = item.getEnchantLevel();
        int augment_id = item.getAugmentationId();
        int attribute_fire = item.getAttributes().getFire();
        int attribute_water = item.getAttributes().getWater();
        int attribute_wind = item.getAttributes().getWind();
        int attribute_earth = item.getAttributes().getEarth();
        int attribute_holy = item.getAttributes().getHoly();
        int attribute_unholy = item.getAttributes().getUnholy();
        String category = "";
        if (item.isWeapon())
            category = "weapon";
        else if (item.isArmor())
            category = "armor";
        else if (item.isAccessory())
            category = "jewelry";
        else if (item.getTemplate().getItemClass() == ItemTemplate.ItemClass.MATHERIALS)
            category = "matherials";
        else if (item.getTemplate().getItemClass() == ItemTemplate.ItemClass.PIECES)
            category = "pieces";
        else if (item.getTemplate().getItemClass() == ItemTemplate.ItemClass.RECIPIES)
            category = "recipe";
        else
            category = "other";

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO `bbs_commission` (`owner_id`, `category`, `item_id`, `item_name`, `item_count`, `enchant_level`, `augment_id`, `attribute_fire`, `attribute_water`, `attribute_wind`, `attribute_earth`, `attribute_holy`, `attribute_unholy`, `price_id`, `price_count`, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, player.getObjectId());
            statement.setString(2, category);
            statement.setInt(3, item_id);
            statement.setString(4, item.getName());
            statement.setLong(5, item_count);
            statement.setInt(6, enchant_level);
            statement.setInt(7, augment_id);
            statement.setInt(8, attribute_fire);
            statement.setInt(9, attribute_water);
            statement.setInt(10, attribute_wind);
            statement.setInt(11, attribute_earth);
            statement.setInt(12, attribute_holy);
            statement.setInt(13, attribute_unholy);
            statement.setInt(14, price_id);
            statement.setLong(15, price_count);
            statement.setLong(16, System.currentTimeMillis());
            statement.execute();

            if (ItemFunctions.removeItemByObjectId(player, item, item_count, true) == 0)
                _log.warn("Not remove Item By Object Id = " + objId);

            Log.add("CommunityCommission\tВыставление товара на продажу objId=" + objId + ":item_id=" + item_id + ":item_count="+item_count, "community", player);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        showPage(player, 1, "myitems", "");
    }

    private void selectProduct(Player player, int objId) {
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/commission.htm", player);
        ItemInstance item = player.getInventory().getItemByObjectId(objId);
        if (item == null || !checkItem(item))
            return;

        StringBuilder sb = new StringBuilder();
        int enchant_level = item.getEnchantLevel();
        int augment_id = item.getAugmentationId();
        int attribute_fire = item.getAttributes().getFire();
        int attribute_water = item.getAttributes().getWater();
        int attribute_wind = item.getAttributes().getWind();
        int attribute_earth = item.getAttributes().getEarth();
        int attribute_holy = item.getAttributes().getHoly();
        int attribute_unholy = item.getAttributes().getUnholy();
        int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
        long item_count = item.getCount();

        sb.append("<table width=320 border=0><tr><td width=320 height=290 align=center><table width=38 border=1><tr><td width=38 height=38 align=center><img src=\"").append(item.getTemplate().getIcon()).append("\" width=32 height=32></td></tr></table><br><img src=\"L2UI.SquareWhite\" width=300 height=1><br><table width=330 border=0>");
        sb.append("<tr><td width=80 align=right>Название:</td><td width=220><font color=0080c0>").append(item.getName()).append("</font></td></tr>");
        if (item.getTemplate().getAdditionalName().length() > 0)
            sb.append("<tr><td width=80 align=right>Особое Свойство:</td><td width=220><font color=ff8000>").append(item.getTemplate().getAdditionalName()).append("</font></td></tr>");
        if (item.isStackable())
            sb.append("<tr><td width=80 align=right>Количество:</td><td width=220><font color=ff8000>").append(item_count).append("</font> шт.</td></tr>");
        if (enchant_level > 0)
            sb.append("<tr><td width=80 align=right>Уровень заточки:</td><td width=220><font color=ffff00>+").append(enchant_level).append("</font></td></tr>");
        if (augment_id > 0) {
            int stat34 = augment_id >> 16;
            TriggerInfo tr = AugmentationData.getInstance().getAugmentTrigger(stat34);
            String augm = "<font color=8000ff>Без скила</font>";

            if (tr != null) {
                String type = "Шанс";
                if (tr.getSkill().isPassive())
                    type = "Пассивный";
                else if (tr.getSkill().isActive())
                    type = "Активный";

                augm = "<font color=8000ff>" + tr.getSkill().getName().replace("Augment Option - ", "").replace("Item Skill: ", "") + "</font> <font color=a448ff>" +tr.getSkill().getLevel()+ "Lv</font> <font color=a448ff>("+type+")</font>";
            }
            sb.append("<tr><td width=80 align=right>Аугментация:</td><td width=220>").append(augm).append("</td></tr>");
        }
        if (att_count > 0) {
            sb.append("<tr><td width=80 align=right>Элементы:</td><td width=220 valign=top><table width=160>");
            if (attribute_fire > 0)
                sb.append("<tr><td width=80><font color=f72c31>Огонь</font></td><td width=80><font color=f72c31>").append(attribute_fire).append("</font></td></tr>");
            if (attribute_water > 0)
                sb.append("<tr><td width=80><font color=f72c31>Вода</font></td><td width=80><font color=f72c31>").append(attribute_water).append("</font></td></tr>");
            if (attribute_wind > 0)
                sb.append("<tr><td width=80><font color=f72c31>Воздух</font></td><td width=80><font color=f72c31>").append(attribute_wind).append("</font></td></tr>");
            if (attribute_earth > 0)
                sb.append("<tr><td width=80><font color=f72c31>Земля</font></td><td width=80><font color=f72c31>").append(attribute_earth).append("</font></td></tr>");
            if (attribute_holy > 0)
                sb.append("<tr><td width=80><font color=f72c31>Святость</font></td><td width=80><font color=f72c31>").append(attribute_holy).append("</font></td></tr>");
            if (attribute_unholy > 0)
                sb.append("<tr><td width=80><font color=f72c31>Тьма</font></td><td width=80><font color=f72c31>").append(attribute_unholy).append("</font></td></tr>");
            sb.append("</table></td></tr>");
        }
        sb.append("</table><br>");

        if (item.isStackable()) {
            sb.append("<img src=\"L2UI.SquareWhite\" width=300 height=1><br><font color=LEVEL>Введите количество предметов на продажу:</font><br1>");
            sb.append("<table width=320 border=0><tr><td height=27 align=center><edit var=\"item_count\" width=100 height=15></td>");
            sb.append("<td align=left>Макc: ").append(item_count).append("</td></tr></table><br>");
        }

        sb.append("<img src=\"L2UI.SquareWhite\" width=300 height=1><br><font color=LEVEL>Введите цену, за которую хотите продать товар:</font><br1>");
        sb.append("<table width=320 border=0><tr><td height=27 align=center><combobox width=150 var=\"price\" list=\"").append(getAvailablePrice()).append("\"></td>");
        sb.append("<td height=27 align=center><edit var=\"price_coint\" width=100 height=15></td></tr></table></td></tr>");
        sb.append("<tr><td align=right><br><button value=\"Выставить на продажу\" action=\"bypass _cbbcommission:create-").append(objId).append("- $price - $price_coint - ").append(item.isStackable() ? " $item_count ": "1").append("\" width=185 height=30 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>");

        int need_item_id = 0;
        int need_item_count = 0;
        if (item.isArmor()) {
            need_item_id = Config.BBS_COMMISSION_ARMOR_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_ARMOR_PRICE[1];
        } else if (item.isWeapon()) {
            need_item_id = Config.BBS_COMMISSION_WEAPON_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_WEAPON_PRICE[1];
        } else if (item.isAccessory()) {
            need_item_id = Config.BBS_COMMISSION_JEWERLY_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_JEWERLY_PRICE[1];
        } else {
            need_item_id = Config.BBS_COMMISSION_OTHER_PRICE[0];
            need_item_count = Config.BBS_COMMISSION_OTHER_PRICE[1];
        }

        if (need_item_id > 0 && need_item_count > 0)
            sb.append("<img src=\"L2UI.SquareWhite\" width=300 height=1><br><table width=320 border=0><tr><td height=27 align=right><font color=ff0000>Стоимость выставления товара на продажу составит:</font><br1><font color=ff0000>").append(need_item_count).append(" ").append(ItemFunctions.getName(need_item_id, false)).append("</font></td></tr></table><br1>");

        html = html.replace("<?content?>", sb.toString());
        html = html.replace("<?pages?>", "");
        html = html.replace("<?category?>", "select");
        html = html.replace("<?category_name?>", getCategoryName("select"));
        ShowBoard.separateAndSend(html, player);
    }

    public String getAvailablePrice() {
        String rewards = "";
        for (int id : Config.BBS_COMMISSION_MONETS)
            if (rewards.isEmpty())
                rewards+=ItemFunctions.getName(id, false);
            else
                rewards+=";" + ItemFunctions.getName(id, false);
        return rewards;
    }

    public int getPriceId(String name) {
        for (int id : Config.BBS_COMMISSION_MONETS)
            if (ItemFunctions.getName(id, false).equalsIgnoreCase(name) && name.length() > 1)
                return id;

        return 0;
    }

    private boolean checkItem(ItemInstance item) {
        if (ArrayUtils.contains(Config.BBS_COMMISSION_ALLOW_ITEMS, item.getItemId()))
            return true;

        if (item.getEnchantLevel() > Config.BBS_COMMISSION_MAX_ENCHANT)
            return false;

        if (!item.getTemplate().isTradeable())
            return false;

        if (ArrayUtils.contains(Config.BBS_COMMISSION_NOT_ALLOW_ITEMS, item.getItemId()))
            return false;

        if (item.isShadowItem())
            return false;

        if (item.getTemplate().isQuest())
            return false;

        if (item.getTemplate().isHerb())
            return false;

        if (item.getTemplate().isTerritoryFlag())
            return false;

        if (item.isCommonItem())
            return false;

        if (item.isTemporalItem())
            return false;

        if (item.isHeroWeapon())
            return false;

        if (item.getTemplate().isUnderwear() && !Config.BBS_COMMISSION_ALLOW_UNDERWEAR)
            return false;

        if (item.getTemplate().isCloak() && !Config.BBS_COMMISSION_ALLOW_CLOAK)
            return false;

        if (item.getTemplate().isBracelet() && !Config.BBS_COMMISSION_ALLOW_BRACELET)
            return false;

        if (item.isAugmented() && !Config.BBS_COMMISSION_ALLOW_AUGMENTED)
            return false;

        return true;
    }

    private void addProduct(Player player, int page) {
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/commission.htm", player);

        List<ItemInstance> temp = new ArrayList<ItemInstance>();
        for (ItemInstance item : player.getInventory().getItems()) {
            if (!checkItem(item))
                continue;

            temp.add(item);
        }


        if (temp.size()<=Config.BBS_COMMISSION_COUNT_TO_PAGE)
            page = 1;

        ItemInstance item;

        StringBuilder sb = new StringBuilder();
        int coun = 2;
        for (int i = page*Config.BBS_COMMISSION_COUNT_TO_PAGE-Config.BBS_COMMISSION_COUNT_TO_PAGE; i<page*Config.BBS_COMMISSION_COUNT_TO_PAGE; i++)
        {
            if (i>=temp.size())
                break;

            item = temp.get(i);

            if (item!=null) {
                if (coun == 1) coun = 2;
                else coun = 1;
                int objId = item.getObjectId();
                if (item.isWeapon()) {
                    int enchant_level = item.getEnchantLevel();
                    int augment_id = item.getAugmentationId();
                    int attribute_fire = item.getAttributes().getFire();
                    int attribute_water = item.getAttributes().getWater();
                    int attribute_wind = item.getAttributes().getWind();
                    int attribute_earth = item.getAttributes().getEarth();
                    int attribute_holy = item.getAttributes().getHoly();
                    int attribute_unholy = item.getAttributes().getUnholy();
                    int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
                    sb.append("<table width=330 border=0").append(coun == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getTemplate().getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:select-").append(objId).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(augment_id > 0 ? "Аугм: <font color=008000>Есть</font> / " : "").append(att_count > 0 ? "Атт: <font color=008000>Есть</font>" : "").append("</font></td></tr></table><br>");
                } else if (item.isArmor()) {
                    int enchant_level = item.getEnchantLevel();
                    int attribute_fire = item.getAttributes().getFire();
                    int attribute_water = item.getAttributes().getWater();
                    int attribute_wind = item.getAttributes().getWind();
                    int attribute_earth = item.getAttributes().getEarth();
                    int attribute_holy = item.getAttributes().getHoly();
                    int attribute_unholy = item.getAttributes().getUnholy();
                    int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
                    sb.append("<table width=330 border=0").append(coun == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getTemplate().getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:select-").append(objId).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(att_count > 0 ? "Атт: <font color=008000>Есть</font>" : "").append("</font></td></tr></table><br>");
                } else if (item.isAccessory()) {
                    int enchant_level = item.getEnchantLevel();
                    int augment_id = item.getAugmentationId();
                    sb.append("<table width=330 border=0").append(coun == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getTemplate().getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:select-").append(objId).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(augment_id > 0 ? "Аугм: <font color=008000>Есть</font>" : "").append("</font></td></tr></table><br>");
                } else {
                    long item_count = item.getCount();
                    sb.append("<table width=330 border=0").append(coun == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getTemplate().getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:select-").append(objId).append("\"><font color=0080c0>").append(item.getName()).append("</font></a><br1><font color=LEVEL>Кол-во:</font> <font color=ff8040>").append(item_count).append(" шт.</font></td></tr></table><br>");
                }
            }
        }

        StringBuilder pg = new StringBuilder();
        if (temp.size() > Config.BBS_COMMISSION_COUNT_TO_PAGE) {
            pg.append("<table width=330 border=0><tr><td width=200 height=20 align=right>Страница:</td>");
            int pages = temp.size() / Config.BBS_COMMISSION_COUNT_TO_PAGE + 1;
            for (int i = 0; i < pages; i++) {
                int cur = i + 1;
                if (page == cur)
                    pg.append("<td width=24 align=center>[").append(cur).append("]</td>");
                else {
                    pg.append("<td width=20 align=center><button value=\"").append(cur).append("\" action=\"bypass _cbbcommission:add-").append(cur).append("\" width=20 height=20 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
                }
            }
            pg.append("</tr></table><br>");
        }
        html = html.replace("<?content?>", sb.toString());
        html = html.replace("<?pages?>", pg.toString());
        html = html.replace("<?category?>", "add");
        html = html.replace("<?category_name?>", getCategoryName("add"));
        ShowBoard.separateAndSend(html, player);
    }

    private void getItem(Player player, int id) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `id` = ? LIMIT 0, 1");
            statement.setInt(1, id);
            rset = statement.executeQuery();
            if (rset.next()) {
                int item_id = rset.getInt("item_id");
                long item_count = rset.getLong("item_count");
                int owner_id = rset.getInt("owner_id");
                int price_id = rset.getInt("price_id");
                long price_count = rset.getLong("price_count");
                int enchant_level = rset.getInt("enchant_level");
                int augment_id = rset.getInt("augment_id");
                int attribute_fire = rset.getInt("attribute_fire");
                int attribute_water = rset.getInt("attribute_water");
                int attribute_wind = rset.getInt("attribute_wind");
                int attribute_earth = rset.getInt("attribute_earth");
                int attribute_holy = rset.getInt("attribute_holy");
                int attribute_unholy = rset.getInt("attribute_unholy");

                PcInventory inventory = player.getInventory();

                // Не забираем оплату, если забирает хозяин
                if (owner_id != player.getObjectId()) {
                    if(!checkHaveItem(player, price_id, price_count)) {
                        player.sendMessage((player.isLangRus() ? "Недостаточное количество " : "Insufficient number ") + ItemFunctions.getName(price_id, false));
                        return;
                    }
                    Functions.removeItem(player, price_id, price_count);

                    ItemTemplate sell = ItemHolder.getInstance().getTemplate(item_id);
                    // отправляем посылку с оплатой за товар
                    Mail mail = new Mail();
                    mail.setSenderId(1);
                    mail.setSenderName("Комиссионный магазин");
                    mail.setReceiverId(owner_id);
                    mail.setReceiverName(getCharName(owner_id));
                    mail.setTopic("Ваш товар купили");
                    mail.setBody(new StringBuffer().append("Ваш товар ").append(sell.getName()).append(sell.isStackable() ? " в колличестве " + item_count + " шт." : "").append(" купил(-а) ").append(player.getName()).append(". Примите оплату.").toString());
                    mail.setPrice(0);
                    mail.setUnread(true);

                    ItemInstance item = ItemFunctions.createItem(price_id);
                    item.setLocation(ItemInstance.ItemLocation.MAIL);
                    item.setCount(price_count);
                    item.save();

                    mail.addAttachment(item);
                    mail.setType(Mail.SenderType.NORMAL);
                    mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
                    mail.save();

                    Player target = World.getPlayer(owner_id);
                    if(target != null)
                    {
                        target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
                        target.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
                    }
                }
                ItemInstance item = ItemFunctions.createItem(item_id);
                // Если стопковый предмет, ставим количество
                if (item.isStackable())
                    item.setCount(item_count);
                // Добавляем заточку, если есть
                if (enchant_level > 0)
                    item.setEnchantLevel(enchant_level);
                // Добавляем аугментацию, если есть
                if (augment_id > 0)
                    item.setAugmentationId(augment_id);
                // Добавляем аттрибуты, если есть
                if (attribute_fire > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.FIRE) : Element.FIRE, attribute_fire);
                if (attribute_water > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.WATER) : Element.WATER, attribute_water);
                if (attribute_wind > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.WIND) : Element.WIND, attribute_wind);
                if (attribute_earth > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.EARTH) : Element.EARTH, attribute_earth);
                if (attribute_holy > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.HOLY) : Element.HOLY, attribute_holy);
                if (attribute_unholy > 0)
                    item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.UNHOLY) : Element.UNHOLY, attribute_unholy);

                player.sendPacket(SystemMessage2.obtainItems(item));
                inventory.addItem(item);
                deleteItem(con, player, id);
                if (owner_id != player.getObjectId())
                    Log.add("CommunityCommission\tВыставленный товар " + item.getName() + " купил " + player.getName() + " за " + price_id + "-" + price_count, "community", player);
                else
                    Log.add("CommunityCommission\tПродавец " + player.getName() + " забрал итем " + item.getName(), "community", player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    private void deleteItem(Connection con, Player player, int id) {
        PreparedStatement statement = null;
        try {
            // Удаляем предмет с аукциона
            statement = con.prepareStatement("DELETE FROM `bbs_commission` WHERE `id` = ?");
            statement.setInt(1, id);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void showItem(Player player, int id) {
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/commission.htm", player);

        StringBuilder sb = new StringBuilder();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `id` = ?");
            statement.setInt(1, id);
            rset = statement.executeQuery();
            while (rset.next()) {
                int item_id = rset.getInt("item_id");
                long item_count = rset.getLong("item_count");
                int owner_id = rset.getInt("owner_id");
                int price_id = rset.getInt("price_id");
                long price_count = rset.getLong("price_count");
                if (price_id == 0 || item_id == 0)
                    continue;
                int enchant_level = rset.getInt("enchant_level");
                int augment_id = rset.getInt("augment_id");
                int attribute_fire = rset.getInt("attribute_fire");
                int attribute_water = rset.getInt("attribute_water");
                int attribute_wind = rset.getInt("attribute_wind");
                int attribute_earth = rset.getInt("attribute_earth");
                int attribute_holy = rset.getInt("attribute_holy");
                int attribute_unholy = rset.getInt("attribute_unholy");
                int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
                ItemTemplate item = ItemHolder.getInstance().getTemplate(item_id);
                if (item == null)
                    continue;
                sb.append("<table width=320 border=0><tr><td width=320 height=290 align=center><table width=238 border=1><tr><td width=38 height=38 align=center><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td><td width=200 align=\"center\">");
                if (owner_id == player.getObjectId())
                    sb.append("<button value=\"Снять с продажи\" action=\"bypass _cbbcommission:get-").append(id).append("\" width=180 height=30 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">");
                else
                    sb.append("<button value=\"Купить\" action=\"bypass _cbbcommission:get-").append(id).append("\" width=180 height=30 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\">");
                sb.append("</td></tr></table><br><table width=330 border=0><tr><td width=80 align=right>Название:</td><td width=220><font color=0080c0>");
                sb.append(item.getName());
                sb.append("</font></td></tr>");
                if (item.getAdditionalName().length() > 0)
                    sb.append("<tr><td width=80 align=right>Особое Свойство:</td><td width=220><font color=ff8000>").append(item.getAdditionalName()).append("</font></td></tr>");
                if (item.isStackable())
                    sb.append("<tr><td width=80 align=right>Количество:</td><td width=220><font color=ff8000>").append(item_count).append("</font> шт.</td></tr>");
                if (enchant_level > 0)
                    sb.append("<tr><td width=80 align=right>Уровень заточки:</td><td width=220><font color=ffff00>+").append(enchant_level).append("</font></td></tr>");
                if (augment_id > 0) {
                    int stat34 = augment_id >> 16;
                    TriggerInfo tr = AugmentationData.getInstance().getAugmentTrigger(stat34);
                    String augm = "<font color=8000ff>Без скила</font>";

                    if (tr != null) {
                        String type = "Шанс";
                        if (tr.getSkill().isPassive())
                            type = "Пассивный";
                        else if (tr.getSkill().isActive())
                            type = "Активный";

                        augm = "<font color=8000ff>" + tr.getSkill().getName().replace("Augment Option - ", "").replace("Item Skill: ", "") + "</font> <font color=a448ff>" +tr.getSkill().getLevel()+ "Lv</font> <font color=a448ff>("+type+")</font>";
                    }
                    sb.append("<tr><td width=80 align=right>Аугментация:</td><td width=220>").append(augm).append("</td></tr>");
                }
                if (att_count > 0) {
                    sb.append("<tr><td height=20></td><td></td></tr><tr><td width=80 align=right>Элементы:</td><td width=220 valign=top><table width=160>");
                    if (attribute_fire > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Огонь</font></td><td width=80><font color=f72c31>").append(attribute_fire).append("</font></td></tr>");
                    if (attribute_water > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Вода</font></td><td width=80><font color=f72c31>").append(attribute_water).append("</font></td></tr>");
                    if (attribute_wind > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Воздух</font></td><td width=80><font color=f72c31>").append(attribute_wind).append("</font></td></tr>");
                    if (attribute_earth > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Земля</font></td><td width=80><font color=f72c31>").append(attribute_earth).append("</font></td></tr>");
                    if (attribute_holy > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Святость</font></td><td width=80><font color=f72c31>").append(attribute_holy).append("</font></td></tr>");
                    if (attribute_unholy > 0)
                        sb.append("<tr><td width=80><font color=f72c31>Тьма</font></td><td width=80><font color=f72c31>").append(attribute_unholy).append("</font></td></tr>");
                    sb.append("</table></td></tr>");
                }
                sb.append("<tr><td height=15></td><td></td></tr><tr><td width=80 align=right>Продавец:</td><td width=220><font color=00ff00>").append(getCharName(owner_id)).append(owner_id == player.getObjectId() ? " (вы)" : "").append("</font></td></tr>");
                sb.append("<tr><td height=20></td><td></td></tr><tr><td width=80 align=right>Стоимость:</td><td width=220><font color=00ff00>").append(price_count).append(" ").append(ItemFunctions.getName(price_id, false)).append("</font></td></tr>");
                sb.append("</table></td></tr></table><br>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        html = html.replace("<?content?>", sb.toString());
        html = html.replace("<?category_name?>", "Просмотр продукта");
        html = html.replace("<?pages?>", "");
        ShowBoard.separateAndSend(html, player);
    }

    private String getCharName(int objId) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `obj_Id` = ? LIMIT 1");
            statement.setInt(1, objId);
            rset = statement.executeQuery();
            if (rset.next())
                return rset.getString("char_name");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return "No char";
    }

    private void showPage(Player player, int page, String category, String search) {
        String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/commission.htm", player);
        String content = getContent(page, category, search, player.getObjectId());
        if (content.length() < 10)
            if (search.isEmpty() || search.length() < 2)
                content = "<table width=330 border=0 bgcolor=433d32><tr><td width=330 height=38 align=center><br><font color=FF0000>Товары ещё не завезли</font></td></tr></table><br>";
            else if (category.equalsIgnoreCase("myitems"))
                content = "<table width=330 border=0 bgcolor=433d32><tr><td width=330 height=38 align=center><br><font color=FF0000>Товаров не найдено</font></td></tr></table><br>";
            else
                content = "<table width=330 border=0 bgcolor=433d32><tr><td width=330 height=38 align=center><br><font color=FF0000>Товар под названием "+search+" не найден</font></td></tr></table><br>";

        StringBuilder pg = new StringBuilder();
        int items = getItemsCount(category, search, player.getObjectId());
        if (items > Config.BBS_COMMISSION_COUNT_TO_PAGE) {
            pg.append("<table width=330 border=0><tr><td width=200 height=20 align=right>Страница:</td>");
            int pages = items / Config.BBS_COMMISSION_COUNT_TO_PAGE + 1;
            for (int i = 0; i < pages; i++) {
                int cur = i + 1;
                if (page == cur)
                    pg.append("<td width=24 align=center>[").append(cur).append("]</td>");
                else {
                    if (search.isEmpty() || search.length() < 2)
                        pg.append("<td width=20 align=center><button value=\"").append(cur).append("\" action=\"bypass _cbbcommission:list-<?category?>-").append(cur).append("\" width=20 height=20 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
                    else
                        pg.append("<td width=20 align=center><button value=\"").append(cur).append("\" action=\"bypass _cbbcommission:search-").append(search).append("-").append(cur).append("\" width=20 height=20 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
                }
            }
            pg.append("</tr></table><br>");
        }

        html = html.replace("<?content?>", content);
        html = html.replace("<?pages?>", pg.toString());
        html = html.replace("<?category?>", category);
        html = html.replace("<?category_name?>", getCategoryName(category) + (search.length() >= 2 ? " `" + search + "`": ""));
        html = html.replace("<?page_int?>", String.valueOf(page));
        html = html.replace("<?OtherName?>", ItemFunctions.getName(Config.BBS_COMMISSION_OTHER_PRICE[0], false));
        html = html.replace("<?OtherPrice?>", String.valueOf(Config.BBS_COMMISSION_OTHER_PRICE[1]));
        html = html.replace("<?WeaponName?>", ItemFunctions.getName(Config.BBS_COMMISSION_WEAPON_PRICE[0], false));
        html = html.replace("<?WeaponPrice?>", String.valueOf(Config.BBS_COMMISSION_WEAPON_PRICE[1]));
        html = html.replace("<?ArmorName?>", ItemFunctions.getName(Config.BBS_COMMISSION_ARMOR_PRICE[0], false));
        html = html.replace("<?ArmorPrice?>", String.valueOf(Config.BBS_COMMISSION_ARMOR_PRICE[1]));
        html = html.replace("<?JewerlyName?>", ItemFunctions.getName(Config.BBS_COMMISSION_JEWERLY_PRICE[0], false));
        html = html.replace("<?JewerlyPrice?>", String.valueOf(Config.BBS_COMMISSION_JEWERLY_PRICE[1]));
        html = html.replace("<?Name_Server?>", Config.NAME_SERVER);
        ShowBoard.separateAndSend(html, player);
    }

    private int getItemsCount(String category, String search, int owner_id) {
        int rowCount = 0;
        Connection con = null;
        PreparedStatement statement;
        ResultSet rs;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            if (category.equalsIgnoreCase("all"))
                statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission`");
            else if (category.equalsIgnoreCase("myitems")) {
                statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `owner_id` = ?");
                statement.setInt(1, owner_id);
            } else if (search.length() >= 2)
                statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `item_name` LIKE '%"+search.toLowerCase()+"%'");
            else {
                statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `category` = ?");
                statement.setString(1, category);
            }
            rs = statement.executeQuery();
            if (rs.next())
                rowCount = rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rowCount == 0) {
            return 0;
        }

        return rowCount;
    }

    private String getContent(int page, String category, String search, int owner_id) {
        StringBuilder sb = new StringBuilder();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        int limit1 = (page - 1) * Config.BBS_COMMISSION_COUNT_TO_PAGE;
        int limit2 = Config.BBS_COMMISSION_COUNT_TO_PAGE;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            if (category.equalsIgnoreCase("all")) {
                statement = con.prepareStatement("SELECT * FROM `bbs_commission` ORDER BY `date` DESC LIMIT ?, ?");
                statement.setInt(1, limit1);
                statement.setInt(2, limit2);
            } else if (category.equalsIgnoreCase("myitems")) {
                statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `owner_id` = ? ORDER BY `date` DESC LIMIT ?, ?");
                statement.setInt(1, owner_id);
                statement.setInt(2, limit1);
                statement.setInt(3, limit2);
            } else if (search.length() >= 2) {
                statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `item_name` LIKE '%"+search.toLowerCase()+"%' ORDER BY `date` DESC LIMIT ?, ?");
                statement.setInt(1, limit1);
                statement.setInt(2, limit2);
            } else {
                statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `category` = ? ORDER BY `date` DESC LIMIT ?, ?");
                statement.setString(1, category);
                statement.setInt(2, limit1);
                statement.setInt(3, limit2);
            }
            rset = statement.executeQuery();
            int i = 2;
            while (rset.next()) {
                if (i == 1) i = 2;
                else i = 1;
                int id = rset.getInt("id");
                int item_id = rset.getInt("item_id");
                int price_id = rset.getInt("price_id");
                long price_count = rset.getLong("price_count");
                if (price_id == 0 || item_id == 0)
                    continue;
                ItemTemplate item = ItemHolder.getInstance().getTemplate(item_id);
                if (item == null)
                    continue;
                if (item.isWeapon()) {
                    int enchant_level = rset.getInt("enchant_level");
                    int augment_id = rset.getInt("augment_id");
                    int attribute_fire = rset.getInt("attribute_fire");
                    int attribute_water = rset.getInt("attribute_water");
                    int attribute_wind = rset.getInt("attribute_wind");
                    int attribute_earth = rset.getInt("attribute_earth");
                    int attribute_holy = rset.getInt("attribute_holy");
                    int attribute_unholy = rset.getInt("attribute_unholy");
                    int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
                    sb.append("<table width=330 border=0").append(i == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:show-").append(id).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(augment_id > 0 ? "Аугм: <font color=008000>Есть</font> / " : "").append(att_count > 0 ? " Атт: <font color=008000>Есть</font>" : "").append("</font></td>");
                    sb.append("<td width=92 align=center><font color=804000>Цена</font><br1><font color=00ff00>").append(price_count).append(" ").append(ItemFunctions.getName(price_id, false)).append("</font></td></tr></table><br>");
                } else if (item.isArmor()) {
                    int enchant_level = rset.getInt("enchant_level");
                    int attribute_fire = rset.getInt("attribute_fire");
                    int attribute_water = rset.getInt("attribute_water");
                    int attribute_wind = rset.getInt("attribute_wind");
                    int attribute_earth = rset.getInt("attribute_earth");
                    int attribute_holy = rset.getInt("attribute_holy");
                    int attribute_unholy = rset.getInt("attribute_unholy");
                    int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
                    sb.append("<table width=330 border=0").append(i == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:show-").append(id).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(att_count > 0 ? "Атт: <font color=008000>Есть</font>" : "").append("</font></td>");
                    sb.append("<td width=92 align=center><font color=804000>Цена</font><br1><font color=00ff00>").append(price_count).append(" ").append(ItemFunctions.getName(price_id, false)).append("</font></td></tr></table><br>");
                } else if (item.isAccessory() && (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("jewelry") || category.equalsIgnoreCase("search"))) {
                    int enchant_level = rset.getInt("enchant_level");
                    int augment_id = rset.getInt("augment_id");
                    sb.append("<table width=330 border=0").append(i == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:show-").append(id).append("\"><font color=0080c0>").append(item.getName()).append(" [").append(item.getCrystalType().toString()).append("]").append("</font></a><br1><font color=LEVEL>").append(enchant_level > 0 ? "Заточен: <font color=804000>"+enchant_level+"</font> / " : "").append(augment_id > 0 ? "Аугм: <font color=008000>Есть</font>" : "").append("</font></td>");
                    sb.append("<td width=92 align=center><font color=804000>Цена</font><br1><font color=00ff00>").append(price_count).append(" ").append(ItemFunctions.getName(price_id, false)).append("</font></td></tr></table><br>");
                } else {
                    int item_count = rset.getInt("item_count");
                    sb.append("<table width=330 border=0").append(i == 1 ? " bgcolor=433d32" : "").append("><tr><td width=38 height=38 align=center><img src=\"").append(item.getIcon()).append("\" width=32 height=32></td>");
                    sb.append("<td width=200 align=center><a action=\"bypass _cbbcommission:show-").append(id).append("\"><font color=0080c0>").append(item.getName()).append("</font></a><br1><font color=LEVEL>Кол-во:</font> <font color=ff8040>").append(item_count).append(" шт.</font></td>");
                    sb.append("<td width=92 align=center><font color=804000>Цена</font><br1><font color=00ff00>").append(price_count).append(" ").append(ItemFunctions.getName(price_id, false)).append("</font></td></tr></table><br>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<table width=330 border=0 bgcolor=433d32><tr><td width=330 height=38 align=center><br><font color=FF0000>Товары ещё не завезли</font></td></tr></table><br>";
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return sb.toString();
    }

    private String strip(String text) {
        text = text.replaceAll("<a>", "");
        text = text.replaceAll("</a>", "");
        text = text.replaceAll("<font>", "");
        text = text.replaceAll("</font>", "");
        text = text.replaceAll("<table>", "");
        text = text.replaceAll("<tr>", "");
        text = text.replaceAll("<td>", "");
        text = text.replaceAll("</table>", "");
        text = text.replaceAll("</tr>", "");
        text = text.replaceAll("</td>", "");
        text = text.replaceAll("<br>", "");
        text = text.replaceAll("<br1>", "");
        text = text.replaceAll("<button", "");
        return text;
    }

    private String getCategoryName(String category) {
        if (category.equalsIgnoreCase("all"))
            return "Все товары";
        else if (category.equalsIgnoreCase("matherials"))
            return "Ресурсы";
        else if (category.equalsIgnoreCase("pieces"))
            return "Материалы";
        else if (category.equalsIgnoreCase("recipe"))
            return "Рецепты";
        else if (category.equalsIgnoreCase("matherials"))
            return "Ресурсы";
        else if (category.equalsIgnoreCase("weapon"))
            return "Оружие";
        else if (category.equalsIgnoreCase("armor"))
            return "Броня";
        else if (category.equalsIgnoreCase("jewelry"))
            return "Бижутерия";
        else if (category.equalsIgnoreCase("other"))
            return "Остальные товары";

        else if (category.equalsIgnoreCase("search"))
            return "Поиск по слову";

        else if (category.equalsIgnoreCase("myitems"))
            return "Ваши товары";
        else if (category.equalsIgnoreCase("add"))
            return "Шаг первый, выберите предмет для продажи";
        else if (category.equalsIgnoreCase("select"))
            return "Шаг второй, выбрите предмет за который продаёте";

        return "Неизвестная категория";
    }

    private static boolean checkHaveItem(Player player, int itemId, long count)
    {
        if(Functions.getItemCount(player, itemId) < count)
        {
            if(itemId == 57)
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            else
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return false;
        }
        return true;
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}

    @Override
    public void onLoad() {
        if (Config.BBS_COMMISSION_ALLOW) {
            _log.info("CommunityBoard: Commission Loaded");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public void onReload() {
        CommunityBoardManager.getInstance().removeHandler(this);
    }

    @Override
    public void onShutdown() {}
}
