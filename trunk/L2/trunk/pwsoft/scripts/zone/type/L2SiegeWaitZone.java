package scripts.zone.type;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.util.Location;
import scripts.zone.L2ZoneType;

public class L2SiegeWaitZone extends L2ZoneType
{
  private int _castleId;
  private Castle _castle;

  public L2SiegeWaitZone(int id)
  {
    super(id);
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("castleId"))
    {
      _castleId = Integer.parseInt(value);

      _castle = CastleManager.getInstance().getCastleById(_castleId);
      _castle.setWaitZone(this);
    } else {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      if ((getCastle() != null) && (getCastle().getSiege().getIsInProgress()))
        player.setInCastleWaitZone(true);
    }
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;
      player.setInCastleWaitZone(false);
    }
  }

  public void oustDefenders()
  {
    int[] coord = getCastle().getZone().getSpawn();
    Location loc = new Location(coord[0], coord[1], coord[2]);

    for (L2Character temp : _characterList.values())
    {
      if ((temp != null) && (temp.isPlayer()))
      {
        L2PcInstance player = (L2PcInstance)temp;
        if (player == null)
        {
          continue;
        }
        player.setInCastleWaitZone(false);
        player.teleToLocation(loc, false);
      }
    }
  }

  public int getCastleId()
  {
    return _castleId;
  }

  private final Castle getCastle()
  {
    if (_castle == null)
      _castle = CastleManager.getInstance().getCastleById(_castleId);
    return _castle;
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}