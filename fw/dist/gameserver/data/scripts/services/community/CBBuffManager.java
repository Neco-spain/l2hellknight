package services.community;

import ai.custom.Newbie;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.clientpackets.SendBypassBuildCmd;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.serverpackets.ShowBoard;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.stats.Env;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 16.05.12
 * Time: 22:36
 */
public class CBBuffManager {
    private static int[] mageBuffs = {1353, 1352, 1354, 1048, 1045, 1389, 1035, 1040, 1303, 1085, 1078, 1062,264, 267,268,
                                      304, 305, 349, 363, 273, 276, 365, 1284, 1362, 1413, 4703, 1323, 1500, 1504, 1501};
    private static int[] militaryBuffs = {1352, 1353, 1354, 1252, 1045, 1040, 1036, 1035, 1388, 1204, 1086, 1068, 1077, 1242,1240, 1268, 264, 267,
                                          268, 304, 305, 364, 271, 274, 275, 310, 1362, 1243, 4700, 1363, 4703, 1323, 1502, 1501, 1542, 1499};
    private static int[] petMageBuffs = {1068, 1040, 1086, 1204, 1077, 1242, 1268, 1035, 1036, 1045, 1388, 1363, 271, 275, 274, 269, 264, 304, 364, 4699};
    private static int[] petMilitaryByffs = {1068, 1040, 1086, 1204, 1077, 1242, 1268, 1035, 1036, 1045, 1388, 1363, 271, 275, 274, 269, 264, 304, 364, 4699};

    private static Map<String,int[]> buffsSchemas = new HashMap<String,int[]>(){{
        put("mageBuffs", mageBuffs);
        put("militaryBuffs", militaryBuffs);
        put("petMageBuffs", petMageBuffs);
        put("petMilitaryByffs", petMilitaryByffs);
    }};

    public static void parsecmd(String command, Player player) {
        /* _bbsbuff:returnpage:function:var
           _bbsbuff:returnpage:buffprofile:profileName      - баф профиля
           _bbsbuff:returnpage:saveprofile:profileName      - сохранение профиля
           _bbsbuff:returnpage:deleteprofile:profileName    - удаление профиля
           _bbsbuff:returnpage:buffsingle:skill_id          - одиночный баф
           _bbsbuff:returnpage:buffschema:schemaName        - баф схемы
           _bbsbuff:returnpage:cancelBuffs                  - снятие всех бафов
           _bbsbuff:returnpage:cancelPetBuffs               - снятие всех бафов с пета
           _bbsbuff:returnpage:regen                        - восстановление hp/mp/cp
        */
        String[] cmd = command.split(":");
        String html = "";
        if (cmd[2].equals("buffprofile"))
            doBuffProfile(player, cmd[3]);
        else if (cmd[2].equals("saveprofile")) {
            String newProfile = cmd[3].trim();
            if (newProfile == null || newProfile.equals("")) {
                player.sendMessage("Вы не ввели имя профиля.");
                return;
            }
            saveProfile(player, newProfile);
        } else if (cmd[2].equals("buffsingle")) {
            int skillId = Integer.parseInt(cmd[3]);
            buffSingle(player, skillId);
        } else if (cmd[2].equals("deleteprofile")) {
            deleteprofile(player, cmd[3]);
        } else if (cmd[2].equals("buffschema")) {
            buffSchema(player, cmd[3]);
        } else if (cmd[2].equals("cancelBuffs")){
            player.getEffectList().stopAllEffects();
        } else if(cmd[2].equals("cancelPetBuffs")){
            if (player.getSummonList().getPet() == null)
                return;
            player.getSummonList().getPet().getEffectList().stopAllEffects();
        } else if (cmd[2].equals("regen")){
            player.setCurrentCp(player.getMaxCp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentHp(player.getMaxHp(), false);
        }

        html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/buffer.htm", player);
        html = CommunityBoard.cbReplaceMacro(html, player);
        html = CommunityBoard.cbBufferMacro(html, cmd[1], player);

        ShowBoard.separateAndSend(html, player);
    }

    private static void buffSchema(Player player, String schema) {
        if (!checkCondition(player))
            return;

        if (!player.getInventory().destroyItemByItemId(Config.CB_BUFFER_PRICE_ITEM, Config.CB_BUFFER_PRICE))
        {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return;
        }

        Skill skill;
        try {
            for (int skillId : buffsSchemas.get(schema))
            {
                int skillLvl = SkillTable.getInstance().getMaxLevel(skillId);
                skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (!schema.startsWith("pet"))
                    for (EffectTemplate et : skill.getEffectTemplates())
                    {
                        Env env = new Env(player, player, skill);
                        Effect effect = et.getEffect(env);
                        effect.setPeriod(Config.CB_BUFFER_BUFF_TIME);
                        player.getEffectList().addEffect(effect);
                    }
                else
                {
                    Summon pet = player.getSummonList().getPet();
                    if (pet == null)
                        return;
                    for (EffectTemplate et : skill.getEffectTemplates())
                    {
                        Env env = new Env(pet, pet, skill);
                        Effect effect = et.getEffect(env);
                        effect.setPeriod(Config.CB_BUFFER_BUFF_TIME);
                        pet.getEffectList().addEffect(effect);
                    }
                }
            }
        } catch (Exception e) {
            player.sendMessage("Invalid skill!");
        }
    }

    private static void doBuffProfile(Player player, String buffProfile) {
        if (!checkCondition(player))
            return;

        if (!player.getInventory().destroyItemByItemId(Config.CB_BUFFER_PRICE_ITEM, Config.CB_BUFFER_PRICE))
        {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return;
        }

        Connection con = null;
        Statement community_skillsave_statement = null;
        ResultSet community_skillsave_rs = null;
        Skill skill;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            community_skillsave_statement = con.createStatement();
            community_skillsave_rs = community_skillsave_statement.executeQuery("SELECT `skills` FROM `community_skillsave` WHERE `charId`='" + player.getObjectId() + "' AND `name`='" + buffProfile + "'");

            while (community_skillsave_rs.next()) {
                String[] skills = community_skillsave_rs.getString("skills").split(";");
                for (String skillStr : skills)
                {
                    int skillId = Integer.parseInt(skillStr);
                    int skillLvl = SkillTable.getInstance().getMaxLevel(skillId);
                    skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                    for (EffectTemplate et : skill.getEffectTemplates())
                    {
                        Env env = new Env(player, player, skill);
                        Effect effect = et.getEffect(env);
                        effect.setPeriod(Config.CB_BUFFER_BUFF_TIME);
                        player.getEffectList().addEffect(effect);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, community_skillsave_statement, community_skillsave_rs);
        }
    }

    public static void buffSingle(Player player, int skill_id) {
        if (!checkCondition(player))
            return;

        if (!player.getInventory().destroyItemByItemId(Config.CB_BUFFER_PRICE_ITEM, Config.CB_BUFFER_PRICE))
        {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
            return;
        }

        try {
            int skillLvl = SkillTable.getInstance().getBaseLevel(skill_id);
            Skill skill = SkillTable.getInstance().getInfo(skill_id, skillLvl);
            for (EffectTemplate et : skill.getEffectTemplates())
            {
                    Env env = new Env(player, player, skill);
                    Effect effect = et.getEffect(env);
                    effect.setPeriod(Config.CB_BUFFER_BUFF_TIME);
                    player.getEffectList().addEffect(effect);
            }
        } catch (Exception e) {
            player.sendMessage("Invalid skill!");
        }
    }

    public static String getProfiles(Player player)
    {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT `name` FROM `community_skillsave` WHERE `charId`=?;");
            statement.setLong(1, player.getObjectId());
            rs = statement.executeQuery();
            StringBuilder html = new StringBuilder();
            while (rs.next()) {
                String profileName = rs.getString("name");
                html.append("<tr>");
                html.append("<td>");
                html.append("<button value=\"" + profileName + "\" action=\"bypass _bbsbuff:index:buffprofile:" + profileName + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                html.append("</td>");
                html.append("<td>");
                html.append("<button value=\"-\" action=\"bypass _bbsbuff:index:deleteprofile:" + profileName + "\" width=20 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                html.append("</td>");
                html.append("</tr>");
            }
            return html.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
        return "";
    }

    private static void saveProfile(Player player, String newProfile) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        if (player.getEffectList().isEmpty())
        {
            player.sendMessage("Ваш список эффектов пуст.");
            return;
        }

        try {
            con = DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("SELECT COUNT(*) FROM community_skillsave WHERE charId=? AND name=?;");
            statement.setInt(1, player.getObjectId());
            statement.setString(2, newProfile);
            rs = statement.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                String newBuffsProfile = "";
                Effect[] skill = player.getEffectList().getAllFirstEffects();
                for (Effect skl : skill)
                    newBuffsProfile += skl.getSkill().getId() + ";";
                statement = con.prepareStatement("INSERT INTO community_skillsave (charId, name, skills) VALUES(?,?,?);");
                statement.setInt(1, player.getObjectId());
                statement.setString(2, newProfile);
                statement.setString(3, newBuffsProfile);
                statement.execute();
            } else
                player.sendMessage("Это название уже занято.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private static void deleteprofile(Player player, String profile) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM community_skillsave WHERE `charId`=? AND `name`=?;");
            statement.setInt(1, player.getObjectId());
            statement.setString(2, profile);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    private static boolean checkCondition(Player player) {
        if (player == null)
            return false;

        if (player.isInOlympiadMode()) {
            player.sendMessage("Во время Олимпиады нельзя использовать данную функцию.");
            return false;
        }

        if (player.getReflectionId() != 0 && !Config.ALLOW_CB_BUFFER_IN_INSTANCE) {
            player.sendMessage("Бафф доступен только в обычном мире.");
            return false;
        }

        if (!Config.ALLOW_CB_BUFFER_ON_SIEGE) {
            final Castle castle = ResidenceHolder.getInstance().getResidenceByCoord(Castle.class, player.getX(), player.getY(), player.getZ(), player.getReflection());
            if (castle != null && castle.getSiegeEvent().isInProgress()) {
                player.sendMessage("Невозможно использовать бафера на осаде!");
                return false;
            }
        }
        return true;
    }
}