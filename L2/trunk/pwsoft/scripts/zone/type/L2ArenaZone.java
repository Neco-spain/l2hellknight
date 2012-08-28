package scripts.zone.type;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2ArenaZone extends L2ZoneType
{
  private String _arenaName;
  private int[] _spawnLoc;

  public L2ArenaZone(int id)
  {
    super(id);

    _spawnLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("name"))
    {
      _arenaName = value;
    }
    else if (name.equals("spawnX"))
    {
      _spawnLoc[0] = Integer.parseInt(value);
    }
    else if (name.equals("spawnY"))
    {
      _spawnLoc[1] = Integer.parseInt(value);
    }
    else if (name.equals("spawnZ"))
    {
      _spawnLoc[2] = Integer.parseInt(value);
    }
    else super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(1, true);
    character.sendPacket(Static.ENTERED_COMBAT_ZONE);
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(1, false);
    character.sendPacket(Static.LEFT_COMBAT_ZONE);
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character) {
  }

  public final int[] getSpawnLoc() {
    return _spawnLoc;
  }
}