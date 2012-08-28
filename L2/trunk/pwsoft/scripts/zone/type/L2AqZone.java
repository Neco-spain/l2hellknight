package scripts.zone.type;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.util.Rnd;
import scripts.zone.L2ZoneType;

public class L2AqZone extends L2ZoneType
{
  private String _zoneName;

  public L2AqZone(int id)
  {
    super(id);
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("name"))
      _zoneName = value;
    else
      super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer()) {
      if (character.getLevel() > Config.AQ_PLAYER_MAX_LVL) {
        if (Rnd.get(100) < 33)
          character.teleToLocation(-19480, 187344, -5600);
        else if (Rnd.get(100) < 50)
          character.teleToLocation(-17928, 180912, -5520);
        else {
          character.teleToLocation(-23808, 182368, -5600);
        }
        return;
      }

      character.setInAqZone(true);
      character.setInsideSilenceZone(true);

      if (Config.ALLOW_RAID_PVP) {
        character.setInHotZone(true);
      }

      if (character.isGM()) {
        character.sendAdmResultMessage("You entered " + _zoneName);
        return;
      }
    }
    if (Config.ALLOW_RAID_PVP)
      character.setInsideZone(1, true);
  }

  protected void onExit(L2Character character)
  {
    if (Config.ALLOW_RAID_PVP) {
      character.setInsideZone(1, false);
    }

    if (character.isPlayer()) {
      character.setInAqZone(false);
      character.setInsideSilenceZone(false);

      if (Config.ALLOW_RAID_PVP) {
        character.setInHotZone(false);
      }

      if (character.isGM()) {
        character.sendAdmResultMessage("You left " + _zoneName);
        return;
      }
    }
  }

  public void oustAllPlayers()
  {
    for (L2Character temp : _characterList.values()) {
      if (!temp.isPlayer())
      {
        continue;
      }
      temp.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public void movePlayersTo(int x, int y, int z) {
    if (_characterList == null) {
      return;
    }
    if (_characterList.isEmpty()) {
      return;
    }
    for (L2Character character : _characterList.values()) {
      if (character == null) {
        continue;
      }
      if ((character.isPlayer()) && 
        (character.isOnline() == 1))
        character.teleToLocation(x, y, z);
    }
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}