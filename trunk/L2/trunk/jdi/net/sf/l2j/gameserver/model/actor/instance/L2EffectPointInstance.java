package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2EffectPointInstance extends L2NpcInstance
{
  private L2Character _owner;

  public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
  {
    super(objectId, template);
    _owner = owner;
  }

  public L2Character getOwner()
  {
    return _owner;
  }

  public void onAction(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();
    if (player == null) return;
    player.sendPacket(new ActionFailed());
  }
}