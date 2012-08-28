package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SiegeNpcInstance extends L2FolkInstance
{
  public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
  {
    super(objectID, template);
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
    else
    {
      showSiegeInfoWindow(player);
    }

    player.sendPacket(new ActionFailed());
  }

  public void showSiegeInfoWindow(L2PcInstance player)
  {
    if (validateCondition(player)) {
      getCastle().getSiege().listRegisterClan(player);
    }
    else {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      html.setFile("data/html/siege/" + getTemplate().npcId + "-busy.htm");
      html.replace("%castlename%", getCastle().getName());
      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);
      player.sendPacket(new ActionFailed());
    }
  }

  private boolean validateCondition(L2PcInstance player)
  {
    return !getCastle().getSiege().getIsInProgress();
  }
}