package net.sf.l2j.gameserver.model.zone.type;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2FortZone extends L2ZoneType
{
  private int _fortId;
  private Fort _fort;
  private int[] _spawnLoc;

  public L2FortZone(int id)
  {
    super(id);

    _spawnLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("fortId"))
    {
      _fortId = Integer.parseInt(value);

      _fort = FortManager.getInstance().getFortById(_fortId);
      _fort.setZone(this);
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
    else
    {
      super.setParameter(name, value);
    }
  }

  protected void onEnter(L2Character character)
  {
    if (_fort.getSiege().getIsInProgress())
    {
      character.setInsideZone(1, true);
      character.setInsideZone(4, true);

      if ((character instanceof L2PcInstance))
      {
        ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
      }
    }
  }

  protected void onExit(L2Character character)
  {
    if (_fort.getSiege().getIsInProgress())
    {
      character.setInsideZone(1, false);
      character.setInsideZone(4, false);

      if ((character instanceof L2PcInstance))
      {
        ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));

        if (((L2PcInstance)character).getPvpFlag() == 0)
        {
          ((L2PcInstance)character).startPvPFlag();
        }
      }
    }
    if ((character instanceof L2SiegeSummonInstance))
    {
      ((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public void updateZoneStatusForCharactersInside()
  {
    if (_fort.getSiege().getIsInProgress())
    {
      for (L2Character character : _characterList.values())
      {
        try
        {
          onEnter(character);
        }
        catch (NullPointerException e)
        {
          e.printStackTrace();
        }
      }
    }
    else
    {
      for (L2Character character : _characterList.values())
      {
        try
        {
          character.setInsideZone(1, false);
          character.setInsideZone(4, false);

          if ((character instanceof L2PcInstance))
          {
            ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
          }

          if ((character instanceof L2SiegeSummonInstance))
          {
            ((L2SiegeSummonInstance)character).unSummon(((L2SiegeSummonInstance)character).getOwner());
          }
        }
        catch (NullPointerException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  public void banishForeigners(int owningClanId)
  {
    for (L2Character temp : _characterList.values())
    {
      if ((!(temp instanceof L2PcInstance)) || 
        (((L2PcInstance)temp).getClanId() == owningClanId))
      {
        continue;
      }

      ((L2PcInstance)temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public void announceToPlayers(String message)
  {
    for (L2Character temp : _characterList.values())
    {
      if ((temp instanceof L2PcInstance))
      {
        ((L2PcInstance)temp).sendMessage(message);
      }
    }
  }

  public FastList<L2PcInstance> getAllPlayers()
  {
    FastList players = new FastList();

    for (L2Character temp : _characterList.values())
    {
      if ((temp instanceof L2PcInstance))
      {
        players.add((L2PcInstance)temp);
      }
    }

    return players;
  }

  public int[] getSpawn()
  {
    return _spawnLoc;
  }
}