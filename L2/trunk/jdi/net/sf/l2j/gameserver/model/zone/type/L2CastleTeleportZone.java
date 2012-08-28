package net.sf.l2j.gameserver.model.zone.type;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.util.Rnd;

public class L2CastleTeleportZone extends L2ZoneType
{
  private int[] _spawnLoc;
  private int _castleId;
  private Castle _castle;

  public L2CastleTeleportZone(int id)
  {
    super(id);

    _spawnLoc = new int[5];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("castleId"))
    {
      _castleId = Integer.parseInt(value);
      _castle = CastleManager.getInstance().getCastleById(_castleId);
      _castle.setTeleZone(this);
    }
    else if (name.equals("spawnMinX"))
    {
      _spawnLoc[0] = Integer.parseInt(value);
    }
    else if (name.equals("spawnMaxX"))
    {
      _spawnLoc[1] = Integer.parseInt(value);
    }
    else if (name.equals("spawnMinY"))
    {
      _spawnLoc[2] = Integer.parseInt(value);
    }
    else if (name.equals("spawnMaxY"))
    {
      _spawnLoc[3] = Integer.parseInt(value);
    }
    else if (name.equals("spawnZ"))
    {
      _spawnLoc[4] = Integer.parseInt(value);
    }
    else {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(4096, true);
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(4096, false);
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }

  public FastList<L2PcInstance> getAllPlayers()
  {
    FastList players = new FastList();

    for (L2Character temp : _characterList.values())
    {
      if ((temp instanceof L2PcInstance)) {
        players.add((L2PcInstance)temp);
      }
    }
    return players;
  }

  public void oustAllPlayers()
  {
    if (_characterList == null)
      return;
    if (_characterList.isEmpty())
      return;
    for (L2Character character : _characterList.values())
    {
      if (character == null)
        continue;
      if ((character instanceof L2PcInstance))
      {
        L2PcInstance player = (L2PcInstance)character;
        if (player.isOnline() == 1)
          player.teleToLocation(Rnd.get(_spawnLoc[0], _spawnLoc[1]), Rnd.get(_spawnLoc[2], _spawnLoc[3]), _spawnLoc[4]);
      }
    }
  }

  public int[] getSpawn()
  {
    return _spawnLoc;
  }
}