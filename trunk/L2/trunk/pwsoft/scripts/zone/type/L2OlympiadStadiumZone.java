package scripts.zone.type;

import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.zone.L2ZoneType;

public class L2OlympiadStadiumZone extends L2ZoneType
{
  private int _stadiumId;

  public L2OlympiadStadiumZone(int id)
  {
    super(id);
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("stadiumId"))
    {
      _stadiumId = Integer.parseInt(value);
    }
    else super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      if ((player.inObserverMode()) || (player.inFClub()) || (player.inFightClub()) || (player.isEventWait())) {
        return;
      }
      if (EventManager.getInstance().isReg(player)) {
        return;
      }

      character.setInsideZone(64, true);

      player.setInOlumpiadStadium(true);
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(64, false);

    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      player.setInOlumpiadStadium(false);
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public int getStadiumId()
  {
    return _stadiumId;
  }
}