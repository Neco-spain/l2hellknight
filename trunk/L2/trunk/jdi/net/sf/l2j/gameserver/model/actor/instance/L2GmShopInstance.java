package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2GmShopInstance extends L2FolkInstance
{
  private String _curHtm = null;

  public L2GmShopInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
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
    return "data/html/gmshop/" + pom + ".htm";
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    StringTokenizer st = new StringTokenizer(command, " ");
    String cmd = st.nextToken();

    if (cmd.startsWith("chat"))
    {
      String file = "data/html/gmshop/" + getNpcId() + ".htm";
      int cmdChoice = Integer.parseInt(command.substring(5, 7).trim());
      if (cmdChoice > 0)
      {
        file = "data/html/gmshop/" + getNpcId() + "-" + cmdChoice + ".htm";
      }
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      _curHtm = file;
      html.setFile(file);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
    }
    else if (cmd.startsWith("cfpa"))
    {
      if ((player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID) == null) || (player.getInventory().getItemByItemId(Config.BUFF_ITEM_ID).getCount() < Config.BUFF_FIGHTER_3))
      {
        player.sendMessage("\u040C\u0490 \u0435\u045E\u00A0\u0432\u00A0\u0490\u0432 \u00AC\u00AE\u00AD\u0490\u0432");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(_curHtm);
        return;
      }
      player.destroyItemByItemId("Consume", Config.BUFF_ITEM_ID, Config.BUFF_FIGHTER_3, player, false);

      player.getInventory().addItem("ItemsOnCreate", 356, 1, player, null);

      player.getInventory().addItem("ItemsOnCreate", 2438, 1, player, null);

      player.getInventory().addItem("ItemsOnCreate", 2462, 1, player, null);

      player.getInventory().addItem("ItemsOnCreate", 2414, 1, player, null);
      player.sendMessage("\u201A\u043B \u0407\u00AE\u00AB\u0433\u0437\u0401\u00AB\u0401 FPA \u0431\u0490\u0432");
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(_curHtm);
      sendHtmlMessage(player, html);
      player.sendPacket(new ActionFailed());
      player.getInventory().updateDatabase();
    }
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
  {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
  }
}