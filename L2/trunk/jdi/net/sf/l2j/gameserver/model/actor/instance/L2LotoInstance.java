package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Random;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2LotoInstance extends L2FolkInstance
{
  int RND;
  private String _curHtm = null;

  public String getHtmlPath(int npcId, int val)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "data/html/l2js/loto/" + pom + ".htm";
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    StringTokenizer st = new StringTokenizer(command, " ");
    String cmd = st.nextToken();
    if (cmd.startsWith("chat"))
    {
      String file = "data/html/l2js/loto/" + getNpcId() + ".htm";
      int cmdChoice = Integer.parseInt(command.substring(5, 7).trim());
      if (cmdChoice > 0)
      {
        file = "data/html/l2js/loto/" + getNpcId() + "-" + cmdChoice + ".htm";
      }
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      _curHtm = file;
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    if (cmd.startsWith("loto1"))
    {
      if ((player.getInventory().getItemByItemId(Config.STAWKAID_COIN) == null) || (player.getInventory().getItemByItemId(Config.STAWKAID_COIN).getCount() < Config.STAWKA_COIN_AMOUNT))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/" + getNpcId() + ".htm");
        sendHtmlMessage(player, html);
        return;
      }
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/l2js/loto/41101-1.htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }

    if (cmd.startsWith("loto2"))
    {
      if ((player.getInventory().getItemByItemId(Config.STAWKAID_ADENA) == null) || (player.getInventory().getItemByItemId(Config.STAWKAID_ADENA).getCount() < Config.STAWKA_ADENA_AMOUNT))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/" + getNpcId() + ".htm");
        sendHtmlMessage(player, html);
        return;
      }
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/l2js/loto/41101-5.htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }

    if (cmd.startsWith("loto3"))
    {
      Random random = new Random();

      RND = random.nextInt(100);
      if ((player.getInventory().getItemByItemId(Config.STAWKAID_COIN) == null) || (player.getInventory().getItemByItemId(Config.STAWKAID_COIN).getCount() < Config.STAWKA_COIN_AMOUNT))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/" + getNpcId() + ".htm");
        sendHtmlMessage(player, html);
        return;
      }

      if (RND < Config.CHANCE_WIN)
      {
        player.addItem("ItemsOnCreate", Config.WIN_COIN, Config.WIN_COIN_AMOUNT, this, true);
        player.getInventory().updateDatabase();
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/41101-3.htm");
        sendHtmlMessage(player, html);
        player.sendPacket(new ActionFailed());
        return;
      }

      player.destroyItemByItemId("Consume", Config.STAWKAID_COIN, Config.STAWKA_COIN_AMOUNT, player, true);
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/l2js/loto/41101-4.htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
      return;
    }

    if (cmd.startsWith("loto4"))
    {
      Random random = new Random();
      if ((player.getInventory().getItemByItemId(Config.STAWKAID_ADENA) == null) || (player.getInventory().getItemByItemId(Config.STAWKAID_ADENA).getCount() < Config.STAWKA_ADENA_AMOUNT))
      {
        player.sendMessage("\u041D\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043C\u043E\u043D\u0435\u0442");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/" + getNpcId() + ".htm");
        sendHtmlMessage(player, html);
        return;
      }

      RND = random.nextInt(100);

      if (RND < Config.CHANCE_WIN)
      {
        player.addItem("ItemsOnCreate", Config.WIN_ADENA, Config.WIN_ADENA_AMOUNT, this, true);
        player.getInventory().updateDatabase();
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/l2js/loto/41101-3.htm");
        sendHtmlMessage(player, html);
        player.sendPacket(new ActionFailed());
        return;
      }

      player.destroyItemByItemId("Consume", Config.STAWKAID_ADENA, Config.STAWKA_ADENA_AMOUNT, player, true);
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile("data/html/l2js/loto/41101-4.htm");
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
      return;
    }
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
  {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
  }

  public L2LotoInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }
}