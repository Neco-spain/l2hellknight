package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2FolkInstance
{
  private static final int COND_ALL_FALSE = 0;
  private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  private static final int COND_OWNER = 2;
  private static final int COND_REGULAR = 3;

  public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    int condition = validateCondition(player);
    if (condition <= 1) {
      return;
    }
    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    if (actualCommand.equalsIgnoreCase("goto"))
    {
      if (st.countTokens() <= 0) return;
      int whereTo = Integer.parseInt(st.nextToken());
      if (condition == 3)
      {
        doTeleport(player, whereTo);
        return;
      }
      if (condition == 2)
      {
        int minPrivilegeLevel = 0;
        if (st.countTokens() >= 1) minPrivilegeLevel = Integer.parseInt(st.nextToken());
        if (10 >= minPrivilegeLevel)
          doTeleport(player, whereTo);
        else
          player.sendMessage("You don't have the sufficient access level to teleport there.");
        return;
      }
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
    {
      pom = "" + npcId;
    }
    else
    {
      pom = npcId + "-" + val;
    }

    return "data/html/teleporter/" + pom + ".htm";
  }

  public void showChatWindow(L2PcInstance player)
  {
    String filename = "data/html/teleporter/castleteleporter-no.htm";

    int condition = validateCondition(player);
    if (condition == 3)
    {
      super.showChatWindow(player);
      return;
    }
    if (condition > 0)
    {
      if (condition == 1)
        filename = "data/html/teleporter/castleteleporter-busy.htm";
      else if (condition == 2) {
        filename = "data/html/teleporter/" + getNpcId() + ".htm";
      }
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  private void doTeleport(L2PcInstance player, int val)
  {
    L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
    if (list != null)
    {
      if (player.reduceAdena("Teleport", list.getPrice(), player.getLastFolkNPC(), true))
      {
        player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
        player.stopMove(new L2CharPosition(list.getLocX(), list.getLocY(), list.getLocZ(), player.getHeading()));
      }
    }
    else
    {
      _log.warning("No teleport destination with id:" + val);
    }
    player.sendActionFailed();
  }

  private int validateCondition(L2PcInstance player)
  {
    if ((player.getClan() != null) && (getCastle() != null))
    {
      if (getCastle().getSiege().getIsInProgress())
        return 1;
      if (getCastle().getOwnerId() == player.getClanId()) {
        return 2;
      }
    }
    return 0;
  }
}