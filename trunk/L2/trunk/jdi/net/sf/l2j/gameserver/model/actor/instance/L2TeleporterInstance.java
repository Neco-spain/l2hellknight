package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2TeleporterInstance extends L2FolkInstance
{
  private static final int COND_ALL_FALSE = 0;
  private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  private static final int COND_OWNER = 2;
  private static final int COND_REGULAR = 3;

  public L2TeleporterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    player.sendPacket(new ActionFailed());

    int condition = validateCondition(player);

    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    if (actualCommand.equalsIgnoreCase("goto"))
    {
      int npcId = getTemplate().npcId;

      switch (npcId)
      {
      case 31095:
      case 31096:
      case 31097:
      case 31098:
      case 31099:
      case 31100:
      case 31101:
      case 31102:
      case 31114:
      case 31115:
      case 31116:
      case 31117:
      case 31118:
      case 31119:
        player.setIsIn7sDungeon(true);
        break;
      case 31103:
      case 31104:
      case 31105:
      case 31106:
      case 31107:
      case 31108:
      case 31109:
      case 31110:
      case 31120:
      case 31121:
      case 31122:
      case 31123:
      case 31124:
      case 31125:
        player.setIsIn7sDungeon(false);
      case 31111:
      case 31112:
      case 31113:
      }if (st.countTokens() <= 0)
      {
        return;
      }
      int whereTo = Integer.parseInt(st.nextToken());
      if (condition == 3)
      {
        doTeleport(player, whereTo);
        return;
      }
      if (condition == 2)
      {
        int minPrivilegeLevel = 0;
        if (st.countTokens() >= 1)
        {
          minPrivilegeLevel = Integer.parseInt(st.nextToken());
        }
        if (10 >= minPrivilegeLevel)
          doTeleport(player, whereTo);
        else player.sendMessage("You don't have the sufficient access level to teleport there.");
        return;
      }
    }

    super.onBypassFeedback(player, command);
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
      if (condition == 1) filename = "data/html/teleporter/castleteleporter-busy.htm";
      else if (condition == 2) {
        filename = getHtmlPath(getNpcId(), 0);
      }
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
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
      if (SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
      {
        player.sendPacket(new SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
        return;
      }
      if (TownManager.getInstance().townHasCastleInSiege(list.getLocX(), list.getLocY()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
        return;
      }
      if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && (player.getKarma() > 0))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        sm.addString("Go away, you're not welcome here.");
        player.sendPacket(sm);
        return;
      }
      if ((player.isInOlympiadMode()) || (Olympiad.getInstance().isRegisteredInComp(player)) || (player.getOlympiadGameId() > 0))
      {
        player.sendMessage("You cant teleport in olympiad mode");
        return;
      }
      if ((list.getIsForNoble()) && (!player.isNoble()))
      {
        String filename = "data/html/teleporter/nobleteleporter-no.htm";
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
        return;
      }
      if (player.isAlikeDead())
      {
        return;
      }
      if ((!list.getIsForNoble()) && ((Config.ALT_GAME_FREE_TELEPORT) || (player.reduceAdena("Teleport", list.getPrice(), this, true))))
      {
        if (Config.DEBUG) {
          _log.fine("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
        }
        player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
      }
      else if ((list.getIsForNoble()) && ((Config.ALT_GAME_FREE_TELEPORT) || (player.destroyItemByItemId("Noble Teleport", 6651, list.getPrice(), this, true))))
      {
        if (Config.DEBUG) {
          _log.fine("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
        }
        player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
      }
    }
    else
    {
      _log.warning("No teleport destination with id:" + val);
    }
    player.sendPacket(new ActionFailed());
  }

  private int validateCondition(L2PcInstance player)
  {
    if (CastleManager.getInstance().getCastleIndex(this) < 0)
      return 3;
    if (getCastle().getSiege().getIsInProgress())
      return 1;
    if (player.getClan() != null)
    {
      if (getCastle().getOwnerId() == player.getClanId()) {
        return 2;
      }
    }
    return 0;
  }
}