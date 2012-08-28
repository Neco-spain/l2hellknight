package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;


public class donate
  implements IVoicedCommandHandler
{
  @SuppressWarnings("unused")
private int _effectsId = 0;
  @SuppressWarnings("unused")
private L2ItemInstance _item;
  private static final Logger _log = Logger.getLogger(GameServer.class.getName());
  private static final String[] VOICED_COMMANDS = { "donate", "hero", "clan", "time", "heroall", "vip", "pk", "celest" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    int itemid = 9994;
    String ItemName = "\u0414\u043E\u043D\u0430\u0442 \u043C\u043E\u043D\u0435\u0442";
    @SuppressWarnings("unused")
	int NobleCount = 1;
    int HeroCount = 60;
    int FullCount = 50;
    int HeroDays = 30;

    if (command.equalsIgnoreCase("donate"))
    {
      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile("data/html/donate/index.html");
      activeChar.sendPacket(html);

      return false;
    }

    if (command.equalsIgnoreCase("vip")) {
      if (activeChar.getInventory().getInventoryItemCount(9994, 0) >= 10)
      {
        activeChar.getInventory().destroyItemByItemId("donat", 9994, 10, activeChar, null);
        InventoryUpdate iu = new InventoryUpdate();
        ItemList il = new ItemList(activeChar, true);
        activeChar.sendPacket(il);
        activeChar.sendPacket(iu);
        activeChar.getInventory().updateDatabase();
        activeChar.setPremiumService(1);
        activeChar.store();
        activeChar.broadcastStatusUpdate();
        activeChar.broadcastUserInfo();
        activeChar.sendMessage("\u0412\u0430\u043C \u0434\u043E\u0441\u0442\u0430\u0432\u043B\u0435\u043D \u0441\u0442\u0430\u0442\u0443\u0441 VIP, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435");
        _log.info("log info: player: " + activeChar.getName() + " use command .vip is ok");
        return false;
      }

      activeChar.sendMessage("\u0414\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438 \u0441\u0442\u0430\u0442\u0443\u0441\u0430, \u0432\u0430\u043C \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F 10 \u0434\u043E\u043D\u0430\u0442 \u043C\u043E\u043D\u0435\u0442");
      return false;
    }

    if (command.equalsIgnoreCase("pk")) {
      if ((activeChar.getInventory().getInventoryItemCount(9994, 0) >= 10) && (!activeChar.isInCombat()) && (activeChar.getKarma() == 0))
      {
        activeChar.getInventory().destroyItemByItemId("donat", 9994, 10, activeChar, null);
        InventoryUpdate iu = new InventoryUpdate();
        ItemList il = new ItemList(activeChar, true);
        activeChar.sendPacket(il);
        activeChar.sendPacket(iu);
        activeChar.getInventory().updateDatabase();
        activeChar.setPkKills(0);
        activeChar.store();
        activeChar.broadcastStatusUpdate();
        activeChar.broadcastUserInfo();
        activeChar.sendMessage("\u0412\u044B \u043E\u0431\u043D\u0443\u043B\u0438\u043B\u0438 \u0441\u0447\u0435\u0442\u0447\u0438\u043A \u041F\u043A");
        _log.info("log info: player: " + activeChar.getName() + " use command .pk is ok");
        return false;
      }

      activeChar.sendMessage("\u0414\u043B\u044F \u043E\u0431\u043D\u0443\u043B\u0435\u043D\u0438\u044F \u0441\u0442\u0430\u0442\u0443\u0441\u0430 \u043F\u043A, \u0432\u0430\u043C \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F 10 \u0434\u043E\u043D\u0430\u0442 \u043C\u043E\u043D\u0435\u0442");
      return false;
    }
    

    @SuppressWarnings("unused")
	NpcHtmlMessage html = new NpcHtmlMessage(5);

    if (command.equalsIgnoreCase("celest"))
    {
      if (activeChar.getInventory().getInventoryItemCount(9994, 0) >= 100)
      {
        Connection con = null;
        activeChar.getInventory().destroyItemByItemId("donat", 9994, 100, activeChar, null);
        InventoryUpdate iu = new InventoryUpdate();
        ItemList il = new ItemList(activeChar, true);
        activeChar.sendPacket(il);
        activeChar.sendPacket(iu);
        activeChar.getInventory().updateDatabase();
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();

          PreparedStatement statement = con.prepareStatement("INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)");
          statement.setInt(1, activeChar.getObjectId());
          statement.setInt(2, 3158);
          statement.setInt(3, 1);
          statement.setString(4, "Donate Celest");
          statement.setInt(5, 0);
          statement.executeUpdate();
          statement.close();
        }
        catch (Exception e)
        {
          _log.log(Level.SEVERE, "Could not donate selest for player: " + activeChar.getObjectId() + " from DB:");
        }

        activeChar.getAllSkills();
        activeChar.store();
        activeChar.broadcastStatusUpdate();
        activeChar.broadcastUserInfo();

        activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0421\u0435\u043B\u0435\u0441\u0442 \u0432 \u0441\u043A\u0438\u043B\u043B\u044B \u043D\u0430 \u043E\u0441\u043D\u043E\u0432\u043D\u043E\u0439 \u043A\u043B\u0430\u0441\u0441");
        _log.info("log info: player: " + activeChar.getName() + " use command .celest is ok");
        return false;
      }

      activeChar.sendMessage("\u0414\u043B\u044F \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u044F \u0434\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u044C\u043D\u043E\u0433\u043E Item Skill: Celestal Shield \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F 100 \u0434\u043E\u043D\u0430\u0442 \u043C\u043E\u043D\u0435\u0442");
      return false;
    }

    if (command.equalsIgnoreCase("hero"))
    {
      if (activeChar.getInventory().getInventoryItemCount(itemid, 0) >= HeroCount)
      {
        if (!activeChar.isNoble())
        {
          activeChar.sendMessage("\u0412\u0430\u043C \u043D\u0443\u0436\u043D\u043E \u0431\u044B\u0442\u044C \u041D\u0443\u0431\u043B\u0438\u0441\u043E\u043C");
          return false;
        }
        activeChar.getInventory().destroyItemByItemId("donat", itemid, HeroCount, activeChar, null);
        InventoryUpdate iu = new InventoryUpdate();
        activeChar.sendPacket(iu);
        ItemList il = new ItemList(activeChar, true);
        activeChar.sendPacket(il);
        activeChar.getInventory().updateDatabase();
        Heroes.getInstance().addHero(activeChar, HeroDays);
        activeChar.getInventory().addItem("Wings of Destiny Circlet", 6842, 1, activeChar, null);
        activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 Wings of Destiny Circlet");
        activeChar.setHero(true);
        activeChar.broadcastStatusUpdate();
        activeChar.broadcastUserInfo();
        activeChar.sendMessage("\u0412\u044B \u0437\u0430\u0434\u043E\u043D\u0430\u0442\u0438\u043B\u0438 \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u043D\u0430 " + HeroDays + " \u0434\u043D\u0435\u0439");
        Announcements.getInstance().announceToAll(new StringBuilder().append("\u0418\u0433\u0440\u043E\u043A ").append(activeChar.getName()).append(" \u043A\u0443\u043F\u0438\u043B \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u043D\u0430 30 \u0434\u043D\u0435\u0439.").toString());
        _log.info("log info: player: " + activeChar.getName() + " use command .hero " + HeroDays + " days");
        return false;
      }

      activeChar.sendMessage("\u0414\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438 \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F " + HeroCount + " " + ItemName + "(\u044B)");
      return false;
    }

    if (command.equalsIgnoreCase("heroall"))
    {
      if (activeChar.getInventory().getInventoryItemCount(itemid, 0) >= 150)
      {
        if (!activeChar.isNoble())
        {
          activeChar.sendMessage("\u0412\u0430\u043C \u043D\u0443\u0436\u043D\u043E \u0431\u044B\u0442\u044C \u041D\u0443\u0431\u043B\u0438\u0441\u043E\u043C");
          return false;
        }
        activeChar.getInventory().destroyItemByItemId("donat", itemid, 150, activeChar, null);
        InventoryUpdate iu = new InventoryUpdate();
        activeChar.sendPacket(iu);
        ItemList il = new ItemList(activeChar, true);
        activeChar.sendPacket(il);
        activeChar.getInventory().updateDatabase();
        Heroes.getInstance().addHero(activeChar, 500);
        activeChar.getInventory().addItem("Wings of Destiny Circlet", 6842, 1, activeChar, null);
        activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 Wings of Destiny Circlet");
        activeChar.setHero(true);
        activeChar.broadcastStatusUpdate();
        activeChar.broadcastUserInfo();
        activeChar.sendMessage("\u0412\u044B \u0437\u0430\u0434\u043E\u043D\u0430\u0442\u0438\u043B\u0438 \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u043F\u043E\u0436\u0438\u0437\u043D\u0435\u043D\u043D\u043E");
        Announcements.getInstance().announceToAll(new StringBuilder().append("\u0418\u0433\u0440\u043E\u043A ").append(activeChar.getName()).append(" \u043A\u0443\u043F\u0438\u043B \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u043E \u043F\u043E\u0436\u0438\u0437\u043D\u0435\u043D\u043D\u043E").toString());
        _log.info("log info: player: " + activeChar.getName() + " use command .hero");
        return false;
      }

      activeChar.sendMessage("\u0414\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438 \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F 150 " + ItemName + "(\u044B)");
      return false;
    }

    L2Clan clan = activeChar.getClan();

    if (command.equalsIgnoreCase("clan")) {
      if (activeChar.getInventory().getInventoryItemCount(itemid, 0) <= FullCount)
      {
        activeChar.sendMessage("\u0414\u043B\u044F \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u044F 8 \u043B\u0432\u043B \u043A\u043B\u0430\u043D\u0430 \u0438 \u0444\u0443\u043B \u0441\u043A\u0438\u043B\u043B\u043E\u0432 \u043A\u043B\u0430\u043D\u0430 \u0442\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F " + FullCount + " " + ItemName + "");
        return false;
      }
      if (clan == null)
      {
        activeChar.sendMessage("\u0423 \u0412\u0430\u0441 \u043D\u0435\u0442 \u043A\u043B\u0430\u043D\u0430");
        return false;
      }

      activeChar.getInventory().destroyItemByItemId("donat", itemid, FullCount, activeChar, null);
      InventoryUpdate iu = new InventoryUpdate();
      activeChar.sendPacket(iu);
      ItemList il = new ItemList(activeChar, true);
      activeChar.sendPacket(il);
      activeChar.getInventory().updateDatabase();

      clan.changeLevel(8);

      L2Skill sk = SkillTable.getInstance().getInfo(370, 3);
      L2Skill sk1 = SkillTable.getInstance().getInfo(371, 3);
      L2Skill sk2 = SkillTable.getInstance().getInfo(372, 3);
      L2Skill sk3 = SkillTable.getInstance().getInfo(373, 3);
      L2Skill sk4 = SkillTable.getInstance().getInfo(374, 3);
      L2Skill sk5 = SkillTable.getInstance().getInfo(375, 3);
      L2Skill sk6 = SkillTable.getInstance().getInfo(376, 3);
      L2Skill sk7 = SkillTable.getInstance().getInfo(377, 3);
      L2Skill sk8 = SkillTable.getInstance().getInfo(378, 3);
      L2Skill sk9 = SkillTable.getInstance().getInfo(379, 3);
      L2Skill sk10 = SkillTable.getInstance().getInfo(380, 3);
      L2Skill sk11 = SkillTable.getInstance().getInfo(381, 3);
      L2Skill sk12 = SkillTable.getInstance().getInfo(382, 3);
      L2Skill sk13 = SkillTable.getInstance().getInfo(383, 3);
      L2Skill sk14 = SkillTable.getInstance().getInfo(384, 3);
      L2Skill sk15 = SkillTable.getInstance().getInfo(385, 3);
      L2Skill sk16 = SkillTable.getInstance().getInfo(386, 3);
      L2Skill sk17 = SkillTable.getInstance().getInfo(387, 3);
      L2Skill sk18 = SkillTable.getInstance().getInfo(388, 3);
      L2Skill sk19 = SkillTable.getInstance().getInfo(389, 3);
      L2Skill sk20 = SkillTable.getInstance().getInfo(390, 3);
      L2Skill sk21 = SkillTable.getInstance().getInfo(391, 1);
      clan.addNewSkill(sk);
      clan.addNewSkill(sk1);
      clan.addNewSkill(sk2);
      clan.addNewSkill(sk3);
      clan.addNewSkill(sk4);
      clan.addNewSkill(sk5);
      clan.addNewSkill(sk6);
      clan.addNewSkill(sk7);
      clan.addNewSkill(sk8);
      clan.addNewSkill(sk9);
      clan.addNewSkill(sk10);
      clan.addNewSkill(sk11);
      clan.addNewSkill(sk12);
      clan.addNewSkill(sk13);
      clan.addNewSkill(sk14);
      clan.addNewSkill(sk15);
      clan.addNewSkill(sk16);
      clan.addNewSkill(sk17);
      clan.addNewSkill(sk18);
      clan.addNewSkill(sk19);
      clan.addNewSkill(sk20);
      clan.addNewSkill(sk21);
      clan.getAllSkills();

      clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
      activeChar.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 8 Level \u043A\u043B\u0430\u043D\u0430 \u0438 \u0444\u0443\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B. \u0416\u0435\u043B\u0430\u0442\u0435\u043B\u044C\u043D\u043E \u0441\u0434\u0435\u043B\u0430\u0442\u044C \u0440\u0435\u0441\u0442\u0430\u0440\u0442.");
      _log.info("log info: player: " + activeChar.getName() + " use command .clan is ok");
      activeChar.sendPacket(new UserInfo(activeChar));
      activeChar.broadcastStatusUpdate();
      activeChar.broadcastUserInfo();
      activeChar.sendSkillList();
      clan.broadcastClanStatus();
      activeChar.broadcastUserInfo();
      activeChar.getAllSkills();
      activeChar.broadcastStatusUpdate();
      return false;
    }

    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}