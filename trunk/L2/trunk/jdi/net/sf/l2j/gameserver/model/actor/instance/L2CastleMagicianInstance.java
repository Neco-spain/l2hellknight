package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CastleMagicianInstance extends L2FolkInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2CastleMagicianInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void showChatWindow(L2PcInstance player, int val)
  {
    player.sendPacket(new ActionFailed());
    String filename = "data/html/castlemagician/magician-no.htm";

    int condition = validateCondition(player);
    if (condition > 0) {
      if (condition == 1)
        filename = "data/html/castlemagician/magician-busy.htm";
      else if (condition == 2)
      {
        if (val == 0)
          filename = "data/html/castlemagician/magician.htm";
        else {
          filename = "data/html/castlemagician/magician-" + val + ".htm";
        }
      }
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", String.valueOf(getName() + " " + getTitle()));
    player.sendPacket(html);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("Chat"))
    {
      int val = 0;
      try
      {
        val = Integer.parseInt(command.substring(5));
      } catch (IndexOutOfBoundsException ioobe) {
      } catch (NumberFormatException nfe) {
      }
      showChatWindow(player, val);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  protected int validateCondition(L2PcInstance player) {
    if (player.isGM()) return 2;
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getCastle().getSiege().getIsInProgress())
          return 1;
        if (getCastle().getOwnerId() == player.getClanId())
          return 2;
      }
    }
    return 0;
  }
}