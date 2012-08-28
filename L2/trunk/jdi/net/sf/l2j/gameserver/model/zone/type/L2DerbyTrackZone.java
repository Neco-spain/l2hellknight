package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class L2DerbyTrackZone extends L2PeaceZone
{
  public L2DerbyTrackZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(512, true);
    }
    super.onEnter(character);
  }

  protected void onExit(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(512, false);
    }
    super.onExit(character);
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}