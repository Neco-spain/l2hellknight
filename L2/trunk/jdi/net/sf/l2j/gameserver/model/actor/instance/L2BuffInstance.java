package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharSchemesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2BuffInstance extends L2NpcInstance
{
  public int[] TableId;
  public int[] TableDialog;
  String _curHtm = "data/html/buff/" + getNpcId() + ".htm";

  public L2BuffInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    TableId = Config.BUFFS_LIST;
    TableDialog = Config.BUFFER_TABLE_DIALOG;
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "data/html/buff/" + pom + ".htm";
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (player.getPvpFlag() != 0)
    {
      player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
      return;
    }
    if (player.isDead())
    {
      player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
      return;
    }

    StringTokenizer st = new StringTokenizer(command, " ");
    String cmd = st.nextToken();
    if (cmd.startsWith("chat"))
    {
      String file = "data/html/buff/" + getNpcId() + ".htm";
      int cmdChoice = Integer.parseInt(command.substring(5, 7).trim());
      if (cmdChoice > 0)
      {
        file = "data/html/buff/" + getNpcId() + "-" + cmdChoice + ".htm";
      }
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      _curHtm = file;
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("cancel"))
    {
      player.stopAllEffects();
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/buff/" + getNpcId() + ".htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (command.startsWith("warior3"))
    {
      String file = "data/html/buff/40001-1.htm";

      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(file);
        player.sendPacket(new ActionFailed());
        return;
      }

      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, player, false);
      SkillTable.getInstance().getInfo(1068, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1086, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1077, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1242, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1268, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1035, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1036, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1045, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1388, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1363, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(271, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(275, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(274, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(269, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(264, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(304, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(364, 1).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("warior2"))
    {
      String file = "data/html/buff/40001-1.htm";
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_2))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(file);
        player.sendPacket(new ActionFailed());
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_2, player, false);
      SkillTable.getInstance().getInfo(1068, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1086, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1077, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1242, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1268, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1035, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1036, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(271, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(275, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(274, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(269, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(264, 1).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("warior1"))
    {
      String file = "data/html/buff/40001-1.htm";
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_1))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(file);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_1, player, false);
      SkillTable.getInstance().getInfo(1068, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1086, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1077, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1242, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1268, 4).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (command.startsWith("mage3"))
    {
      String file = "data/html/buff/40001-1.htm";

      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_3))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(file);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_3, player, false);
      SkillTable.getInstance().getInfo(1085, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1059, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1078, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1048, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1397, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1303, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1035, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1062, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(273, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(276, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(349, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(363, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(365, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(1413, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(1036, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1389, 3).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("mage2"))
    {
      String file = "data/html/buff/40001-1.htm";
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_2))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(file);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_2, player, false);
      SkillTable.getInstance().getInfo(1085, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1059, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1078, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1048, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1397, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1303, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1035, 4).getEffects(this, player);
      SkillTable.getInstance().getInfo(1062, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(273, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(276, 1).getEffects(this, player);
      SkillTable.getInstance().getInfo(349, 1).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("mage1"))
    {
      String file = "data/html/buff/40001-1.htm";
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_MAGE_1))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(_curHtm);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_MAGE_1, player, false);
      SkillTable.getInstance().getInfo(1085, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1059, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1078, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1204, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1048, 6).getEffects(this, player);
      SkillTable.getInstance().getInfo(1397, 3).getEffects(this, player);
      SkillTable.getInstance().getInfo(1303, 2).getEffects(this, player);
      SkillTable.getInstance().getInfo(1040, 3).getEffects(this, player);
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("regen"))
    {
      if (player.getPvpFlag() != 0)
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0444\u0443\u043D\u043A\u0446\u0438\u044E \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F \u0432 PvP");
        return;
      }
      if (player.isDead())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        return;
      }
      player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
      player.setCurrentCp(player.getMaxCp());
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/buff/" + getNpcId() + ".htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }

    if (cmd.startsWith("buff"))
    {
      String filename = "data/html/buff/40001";

      int cmdChoice = Integer.parseInt(command.substring(5, 7).trim());
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_OTHER))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_OTHER, player, false);

      if (player.isDead())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0431\u0430\u0444\u0444\u0435\u0440\u0430 \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B.");
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
      if (id == 4552)
        level = 4;
      String v;
      String v;
      if (dialog == 0)
        v = "";
      else {
        v = "-" + Integer.toString(dialog);
      }

      player.stopSkillEffects(id);

      if (player.getShowAnim())
      {
        player.broadcastPacket(new MagicSkillUser(player, player, id, level, 350, 150));
      }

      SkillTable.getInstance().getInfo(id, level).getEffects(player, player);

      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(filename + v + ".htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("save"))
    {
      int cmdChoice = Integer.parseInt(command.substring(5, 6).trim());
      int flag = 0;
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      if (cmdChoice > 3)
      {
        html.setFile("data/html/buff/" + getNpcId() + "-11.htm");
        flag = 1;
      }
      else {
        html.setFile("data/html/buff/" + getNpcId() + ".htm");
      }CreateScheme(player, Integer.toString(cmdChoice), flag);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("give"))
    {
      int cmdChoice = Integer.parseInt(command.substring(5, 6).trim());
      if ((cmdChoice < 1) && (cmdChoice > 6)) return;
      String key = "data/html/buff/" + getNpcId(); String sKey = Integer.toString(cmdChoice);
      int flag = 0;
      NpcHtmlMessage html = new NpcHtmlMessage(1);

      if (cmdChoice > 3)
      {
        flag = 1;
        key = "data/html/buff/" + getNpcId() + "-8";
      }
      if (player.isDead())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u043E\u0441\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        return;
      }
      if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), sKey) != null)
      {
        player.stopAllEffects();
        if (flag == 0)
        {
          for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), sKey))
          {
            player.stopSkillEffects(sk.getId());
            sk.getEffects(this, player);
          }
        }
        else
        {
          for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), sKey))
          {
            L2Summon pet = player.getPet();
            if (pet != null)
            {
              pet.stopSkillEffects(sk.getId());
              sk.getEffects(this, pet);
            }
          }
        }
        html.setFile(key + ".htm");
      }
      else
      {
        player.sendMessage("\u041F\u0440\u043E\u0444\u0438\u043B\u044C " + sKey + " \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D");
        return;
      }
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("rebuff"))
    {
      int rebuffChoice = 4;
      String key = "data/html/buff/" + getNpcId(); String sKey = Integer.toString(rebuffChoice);
      NpcHtmlMessage html = new NpcHtmlMessage(1);

      if (player.isDead())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0440\u0435\u0431\u0430\u0444\u0444\u0430\u0442\u044C\u0441\u044F \u043A\u043E\u0433\u0434\u0430 \u043C\u0435\u0440\u0442\u0432\u044B");
        return;
      }
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_REBUF))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_REBUF, player, false);

      CreateScheme(player, Integer.toString(4), 0);
      if (CharSchemesTable.getInstance().getScheme(player.getObjectId(), sKey) != null)
      {
        for (L2Skill sk : CharSchemesTable.getInstance().getScheme(player.getObjectId(), sKey))
        {
          player.stopSkillEffects(sk.getId());
          sk.getEffects(this, player);
        }

        html.setFile(key + ".htm");
      }
      else
      {
        player.sendMessage("\u0417\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u043E");
        return;
      }
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
  {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
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
}