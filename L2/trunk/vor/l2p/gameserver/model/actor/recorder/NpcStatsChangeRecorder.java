package l2p.gameserver.model.actor.recorder;

import l2p.gameserver.model.instances.NpcInstance;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance>
{
  public NpcStatsChangeRecorder(NpcInstance actor)
  {
    super(actor);
  }

  protected void onSendChanges()
  {
    super.onSendChanges();

    if ((_changes & 0x1) == 1)
      ((NpcInstance)_activeChar).broadcastCharInfo();
  }
}