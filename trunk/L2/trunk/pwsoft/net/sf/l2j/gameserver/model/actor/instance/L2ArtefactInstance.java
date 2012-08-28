package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2ArtefactInstance extends L2NpcInstance
{
  public L2ArtefactInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  public boolean isAttackable()
  {
    return false;
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }

    player.sendActionFailed();
  }

  public void reduceCurrentHp(double damage, L2Character attacker)
  {
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake) {
  }

  public boolean isL2Artefact() {
    return true;
  }
}