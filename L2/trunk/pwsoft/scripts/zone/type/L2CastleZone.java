package scripts.zone.type;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.zone.L2ZoneType;

public class L2CastleZone extends L2ZoneType
{
  private int _castleId;
  private Castle _castle;
  private int[] _spawnLoc;

  public L2CastleZone(int id)
  {
    super(id);

    _spawnLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("castleId"))
    {
      _castleId = Integer.parseInt(value);

      _castle = CastleManager.getInstance().getCastleById(_castleId);
      _castle.setZone(this);
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
    } else {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    character.setInCastleZone(true);
    if (_castle.getSiege().getIsInProgress())
    {
      character.setInsideZone(1, true);
      character.setInsideZone(4, true);
      character.sendPacket(Static.ENTERED_COMBAT_ZONE);
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInCastleZone(false);
    if (_castle.getSiege().getIsInProgress())
    {
      character.setInsideZone(1, false);
      character.setInsideZone(4, false);
      character.sendPacket(Static.LEFT_COMBAT_ZONE);

      if (character.getPvpFlag() == 0) {
        character.startPvPFlag();
      }
    }
    if ((character instanceof L2SiegeSummonInstance))
      ((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character) {
  }

  public void updateZoneStatusForCharactersInside() {
    if (_castle.getSiege().getIsInProgress())
    {
      for (L2Character character : _characterList.values())
      {
        try
        {
          onEnter(character);
        }
        catch (NullPointerException e) {
        }
      }
    }
    else
      for (L2Character character : _characterList.values())
      {
        try
        {
          onExit(character);
        }
        catch (NullPointerException e)
        {
        }
      }
  }

  public void banishForeigners(int owningClanId)
  {
    for (L2Character temp : _characterList.values())
    {
      if ((!temp.isPlayer()) || 
        (temp.getClanId() == owningClanId)) {
        continue;
      }
      temp.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public void announceToPlayers(String message)
  {
    for (L2Character temp : _characterList.values())
    {
      temp.sendMessage(message);
    }
  }

  public void announceSmToPlayers(SystemMessage sm)
  {
    for (L2Character temp : _characterList.values())
    {
      temp.sendPacket(sm);
    }
  }

  public FastList<L2PcInstance> getAllPlayers()
  {
    FastList players = new FastList();

    for (L2Character temp : _characterList.values())
    {
      if (temp.isPlayer()) {
        players.add((L2PcInstance)temp);
      }
    }
    return players;
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

  public int[] getSpawn()
  {
    return _spawnLoc;
  }
}