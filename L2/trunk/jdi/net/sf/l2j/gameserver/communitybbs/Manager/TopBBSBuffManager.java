package net.sf.l2j.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class TopBBSBuffManager extends BaseBBSManager
{
  public int[] TableId = Config.BUFFS_LIST;
  public int[] TableDialog = Config.BUFFER_TABLE_DIALOG;

  private static TopBBSBuffManager _instance = new TopBBSBuffManager();

  public void parsecmd(String command, L2PcInstance activeChar)
  {
    if (command.equals("_bbstop"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
      if (content == null)
      {
        content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
      }
      separateAndSend(content, activeChar);
    } else {
      if (command.equals("_bbsbuff_warior3"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");

        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          activeChar.sendPacket(new ActionFailed());
          return;
        }

        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }

        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, activeChar, false);

        SkillTable.getInstance().getInfo(1068, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1086, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1077, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1242, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1268, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1035, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1036, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1045, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1388, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1363, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(271, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(275, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(274, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(269, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(264, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(304, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(364, 1).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());

        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if (command.startsWith("_bbsbuff_warior2"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_2))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_2, activeChar, false);
        SkillTable.getInstance().getInfo(1068, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1086, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1077, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1242, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1268, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1035, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1036, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(271, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(275, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(274, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(269, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(264, 1).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if (command.startsWith("_bbsbuff_warior1"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_1))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          separateAndSend(content, activeChar);
          return;
        }
        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_1, activeChar, false);
        SkillTable.getInstance().getInfo(1068, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1086, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1077, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1242, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1268, 4).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if (command.startsWith("_bbsbuff_mage3"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");

        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_3))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          separateAndSend(content, activeChar);
          return;
        }
        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_3, activeChar, false);
        SkillTable.getInstance().getInfo(1085, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1059, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1078, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1048, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1397, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1303, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1035, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1062, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(273, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(276, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(349, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(363, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(365, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1413, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1036, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1389, 3).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if (command.startsWith("_bbsbuff_mage2"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_2))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          separateAndSend(content, activeChar);
          return;
        }
        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_2, activeChar, false);
        SkillTable.getInstance().getInfo(1085, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1059, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1078, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1048, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1397, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1303, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1035, 4).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1062, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(273, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(276, 1).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(349, 1).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if (command.startsWith("_bbsbuff_mage1"))
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");

        if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_1))
        {
          activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
          separateAndSend(content, activeChar);
          return;
        }
        if (activeChar.isDead())
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
          return;
        }
        if (activeChar.getPvpFlag() != 0)
        {
          activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
          return;
        }
        if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
        {
          activeChar.sendMessage("You cant not buff in olympiad mode");
          return;
        }
        if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
        {
          activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
          return;
        }
        if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
        {
          activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
          activeChar.sendPacket(new ActionFailed());
          return;
        }
        activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_1, activeChar, false);
        SkillTable.getInstance().getInfo(1085, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1059, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1078, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1204, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1048, 6).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1397, 3).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1303, 2).getEffects(activeChar, activeChar);
        SkillTable.getInstance().getInfo(1040, 3).getEffects(activeChar, activeChar);
        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
        activeChar.setCurrentCp(activeChar.getMaxCp());
        separateAndSend(content, activeChar);
        activeChar.sendPacket(new ActionFailed());
        return;
      }
    }
    if (command.startsWith("_bbsbuff_buff"))
    {
      String filename = "data/html/CommunityBoard/buff/4000";

      int cmdChoice = Integer.parseInt(command.substring(14, 16).trim());
      if ((activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (activeChar.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_OTHER))
      {
        activeChar.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        return;
      }
      activeChar.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_OTHER, activeChar, false);

      if (activeChar.isDead())
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0431\u0430\u0444\u0444\u0435\u0440\u0430 \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B.");
        return;
      }
      int id = TableId[cmdChoice];
      int dialog = TableDialog[cmdChoice];
      int level = SkillTable.getInstance().getMaxLevel(id, 0);
      if (id == 4554) {
        level = 4;
      }
      if (id == 4553)
        level = 4;
      if (id == 4551)
        level = 4;
      if (id == 4552) {
        level = 4;
      }
      String v = Integer.toString(dialog);

      activeChar.stopSkillEffects(id);

      if (activeChar.getShowAnim())
      {
        activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, id, level, 350, 150));
      }

      SkillTable.getInstance().getInfo(id, level).getEffects(activeChar, activeChar);
      String content = HtmCache.getInstance().getHtm(filename + v + ".htm");
      separateAndSend(content, activeChar);
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (command.startsWith("_bbsbuff_save"))
    {
      int cmdChoice = Integer.parseInt(command.substring(14, 15).trim());
      int flag = 0;
      String content;
      if (cmdChoice > 3)
      {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011.htm");
        flag = 1;
      }
      else {
        content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
      }CreateScheme(activeChar, Integer.toString(cmdChoice), flag);
      separateAndSend(content, activeChar);
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (command.startsWith("_bbsbuff_give"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011.htm");
      int cmdChoice = Integer.parseInt(command.substring(14, 15).trim());
      if ((cmdChoice < 1) && (cmdChoice > 6)) return;
      String key = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/400011"); String sKey = Integer.toString(cmdChoice);
      int flag = 0;
      NpcHtmlMessage html = new NpcHtmlMessage(1);

      if (cmdChoice > 3)
      {
        flag = 1;
        key = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40008");
      }
      if (activeChar.isDead())
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        separateAndSend(content, activeChar);
        return;
      }
      if (CharSchemesTable.getInstance().getScheme(activeChar.getObjectId(), sKey) != null)
      {
        activeChar.stopAllEffects();
        if (flag == 0)
        {
          for (L2Skill sk : CharSchemesTable.getInstance().getScheme(activeChar.getObjectId(), sKey))
          {
            activeChar.stopSkillEffects(sk.getId());
            sk.getEffects(activeChar, activeChar);
          }
        }
        else
        {
          for (L2Skill sk : CharSchemesTable.getInstance().getScheme(activeChar.getObjectId(), sKey))
          {
            L2Summon pet = activeChar.getPet();
            if (pet != null)
            {
              pet.stopSkillEffects(sk.getId());
              sk.getEffects(activeChar, pet);
            }
          }
        }
        content = HtmCache.getInstance().getHtm(key + ".htm");
      }
      else
      {
        activeChar.sendMessage("\u041F\u0440\u043E\u0444\u0438\u043B\u044C " + sKey + " \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D");
        return;
      }
      separateAndSend(content, activeChar);
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (command.startsWith("_bbsbuff_regen"))
    {
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
      if (activeChar.getPvpFlag() != 0)
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
        return;
      }
      if (activeChar.isDead())
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        return;
      }
      if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
      {
        activeChar.sendMessage("You cant not buff in olympiad mode");
        return;
      }
      if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
      {
        activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
        return;
      }
      if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
      {
        activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
        activeChar.sendPacket(new ActionFailed());
        return;
      }
      activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
      activeChar.setCurrentCp(activeChar.getMaxCp());
      separateAndSend(content, activeChar);
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (command.startsWith("_bbsbuff_cancel"))
    {
      if (activeChar.getPvpFlag() != 0)
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
        return;
      }
      if (activeChar.isDead())
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0431\u0430\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        return;
      }
      if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
      {
        activeChar.sendMessage("You cant not buff in olympiad mode");
        return;
      }
      if ((activeChar.isMounted()) || (activeChar.getActiveTradeList() != null) || (activeChar.isCastingNow()) || (activeChar.getActiveEnchantItem() != null) || (TvTEvent.isPlayerParticipant(activeChar.getObjectId())))
      {
        activeChar.sendPacket(SystemMessage.sendString("\u043D\u0435\u0431\u043B\u0430\u0433\u043E\u043F\u0440\u0438\u044F\u0442\u043D\u044B\u0435 \u0443\u0441\u043B\u043E\u0432\u0438\u044F \u0434\u043B\u044F \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u043D\u0438\u044F \u0431\u0430\u0444\u0435\u0440\u0430"));
        return;
      }
      if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
      {
        activeChar.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
        activeChar.sendPacket(new ActionFailed());
        return;
      }
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/40001.htm");
      activeChar.stopAllEffects();
      separateAndSend(content, activeChar);
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (command.startsWith("_bbsbuff;"))
    {
      StringTokenizer st = new StringTokenizer(command, ";");
      st.nextToken();
      int idp = Integer.parseInt(st.nextToken());
      String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/buff/" + idp + ".htm");
      if (content == null)
      {
        content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/buff/" + idp + ".htm' </center></body></html>";
      }
      separateAndSend(content, activeChar);
    }
    else
    {
      ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
      activeChar.sendPacket(sb);
      activeChar.sendPacket(new ShowBoard(null, "102"));
      activeChar.sendPacket(new ShowBoard(null, "103"));
    }
  }

  public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
  {
  }

  private void CreateScheme(L2PcInstance player, String name, int flag)
  {
    if ((CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) != null) && (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).containsKey(name)))
    {
      CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()).remove(name);
    }
    if (CharSchemesTable.getInstance().getAllSchemes(player.getObjectId()) == null)
    {
      CharSchemesTable.getInstance().getSchemesTable().put(Integer.valueOf(player.getObjectId()), new FastMap(6));
    }

    CharSchemesTable.getInstance().setScheme(player.getObjectId(), name.trim(), new FastList(69));
    L2Effect[] s;
    L2Effect[] s;
    if (flag == 0)
    {
      s = player.getAllEffects();
    }
    else
    {
      L2Summon pet = player.getPet();
      s = pet.getAllEffects();
    }

    Boolean Ok = Boolean.valueOf(false);
    int i = 0;
    while (i < s.length) {
      L2Effect value = s[i];
      int Id = value.getSkill().getId();
      int k = 0;
      while (k < TableId.length) {
        if (Id == TableId[k]) {
          Ok = Boolean.valueOf(true);
          break;
        }
        k++;
      }
      if (Ok.booleanValue()) {
        CharSchemesTable.getInstance().getScheme(player.getObjectId(), name).add(SkillTable.getInstance().getInfo(Id, value.getSkill().getLevel()));
      }

      Ok = Boolean.valueOf(false);
      i++;
    }
    if (name.equals(Integer.toString(4)))
    {
      player.sendMessage("\u0422\u0435\u043A\u0443\u0449\u0438\u0435 \u0431\u0430\u0444\u0444\u044B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043E\u0431\u043D\u043E\u0432\u043B\u0435\u043D\u044B");
    }
    else
    {
      player.sendMessage("\u041F\u0440\u043E\u0444\u0438\u043B\u044C " + name + " \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0445\u0440\u0430\u043D\u0451\u043D");
    }
  }

  public static TopBBSBuffManager getInstance()
  {
    return _instance;
  }
}