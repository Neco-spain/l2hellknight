package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2CastleBlacksmithInstance extends L2FolkInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2CastleBlacksmithInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else if (CastleManorManager.getInstance().isDisabled())
    {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      html.setFile("data/html/npcdefault.htm");
      html.replace("%objectId%", String.valueOf(getObjectId()));
      html.replace("%npcname%", getName());
      player.sendPacket(html);
    }
    else
    {
      showMessageWindow(player, 0);
    }

    player.sendPacket(new ActionFailed());
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (CastleManorManager.getInstance().isDisabled())
    {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      html.setFile("data/html/npcdefault.htm");
      html.replace("%objectId%", String.valueOf(getObjectId()));
      html.replace("%npcname%", getName());
      player.sendPacket(html);
      return;
    }

    int condition = validateCondition(player);
    if (condition <= 0) {
      return;
    }
    if (condition == 1)
      return;
    if (condition == 2)
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
        showMessageWindow(player, val);
      }
      else
      {
        super.onBypassFeedback(player, command);
      }
    }
  }

  private void showMessageWindow(L2PcInstance player, int val)
  {
    String filename = "data/html/castleblacksmith/castleblacksmith-no.htm";

    int condition = validateCondition(player);
    if (condition > 0)
    {
      if (condition == 1)
        filename = "data/html/castleblacksmith/castleblacksmith-busy.htm";
      else if (condition == 2)
      {
        if (val == 0)
          filename = "data/html/castleblacksmith/castleblacksmith.htm";
        else {
          filename = "data/html/castleblacksmith/castleblacksmith-" + val + ".htm";
        }
      }
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
    player.sendPacket(html);
  }

  protected int validateCondition(L2PcInstance player)
  {
    if (player.isGM()) return 2;
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getCastle().getSiege().getIsInProgress())
          return 1;
        if ((getCastle().getOwnerId() == player.getClanId()) && (player.isClanLeader()))
        {
          return 2;
        }
      }
    }
    return 0;
  }
}