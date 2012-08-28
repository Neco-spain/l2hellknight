package scripts.zone.type;

import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.zone.L2ZoneType;

public class L2S400Zone extends L2ZoneType
{
  public L2S400Zone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      if (player.inObserverMode()) {
        return;
      }
      if (EventManager.getInstance().isReg(player))
        return;
    }
  }

  protected void onExit(L2Character character)
  {
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}