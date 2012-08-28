package l2p.gameserver.model.actor.recorder;

import l2p.gameserver.model.Summon;

public class SummonStatsChangeRecorder extends CharStatsChangeRecorder<Summon>
{
  public SummonStatsChangeRecorder(Summon actor)
  {
    super(actor);
  }

  protected void onSendChanges()
  {
    super.onSendChanges();

    if ((_changes & 0x2) == 2)
      ((Summon)_activeChar).sendPetInfo();
    else if ((_changes & 0x1) == 1)
      ((Summon)_activeChar).broadcastCharInfo();
  }
}