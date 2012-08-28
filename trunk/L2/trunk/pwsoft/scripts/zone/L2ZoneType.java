package scripts.zone;

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;

public abstract class L2ZoneType
{
  private final int _id;
  protected List<L2ZoneForm> _zone;
  protected FastMap<Integer, L2Character> _characterList;
  protected FastMap<Integer, Integer> _zones;
  private boolean _checkAffected;
  private int _minLvl;
  private int _maxLvl;
  private int[] _race;
  private int[] _class;
  private char _classType;

  protected L2ZoneType(int id)
  {
    _id = id;
    _characterList = new FastMap().shared();
    _zones = new FastMap().shared();

    _checkAffected = false;

    _minLvl = 0;
    _maxLvl = 255;

    _classType = '\000';

    _race = null;
    _class = null;
  }

  public int getId()
  {
    return _id;
  }

  public void setParameter(String name, String value)
  {
    _checkAffected = true;

    if (name.equals("affectedLvlMin"))
    {
      _minLvl = Integer.parseInt(value);
    }
    else if (name.equals("affectedLvlMax"))
    {
      _maxLvl = Integer.parseInt(value);
    }
    else if (name.equals("affectedRace"))
    {
      if (_race == null)
      {
        _race = new int[1];
        _race[0] = Integer.parseInt(value);
      }
      else
      {
        int[] temp = new int[_race.length + 1];

        int i = 0;
        for (; i < _race.length; i++) {
          temp[i] = _race[i];
        }
        temp[i] = Integer.parseInt(value);

        _race = temp;
      }

    }
    else if (name.equals("affectedClassId"))
    {
      if (_class == null)
      {
        _class = new int[1];
        _class[0] = Integer.parseInt(value);
      }
      else
      {
        int[] temp = new int[_class.length + 1];

        int i = 0;
        for (; i < _class.length; i++) {
          temp[i] = _class[i];
        }
        temp[i] = Integer.parseInt(value);

        _class = temp;
      }

    }
    else if (name.equals("affectedClassType"))
    {
      if (value.equals("Fighter"))
      {
        _classType = '\001';
      }
      else
      {
        _classType = '\002';
      }
    }
  }

  private boolean isAffected(L2Character character)
  {
    if ((character.getLevel() < _minLvl) || (character.getLevel() > _maxLvl)) {
      return false;
    }
    if (character.isPlayer())
    {
      if (_classType != 0)
      {
        if (((L2PcInstance)character).isMageClass())
        {
          if (_classType == '\001')
            return false;
        }
        else if (_classType == '\002') {
          return false;
        }
      }

      if (_race != null)
      {
        boolean ok = false;

        for (int i = 0; i < _race.length; i++)
        {
          if (((L2PcInstance)character).getRace().ordinal() != _race[i])
            continue;
          ok = true;
          break;
        }

        if (!ok) {
          return false;
        }
      }

      if (_class != null)
      {
        boolean ok = false;

        for (int i = 0; i < _class.length; i++)
        {
          if (((L2PcInstance)character).getClassId().ordinal() != _class[i])
            continue;
          ok = true;
          break;
        }

        if (!ok)
          return false;
      }
    }
    return true;
  }

  public void setZone(L2ZoneForm zone)
  {
    getZones().add(zone);
  }

  public L2ZoneForm getZone()
  {
    Iterator i$ = getZones().iterator(); if (i$.hasNext()) { L2ZoneForm zone = (L2ZoneForm)i$.next();

      return zone;
    }
    return null;
  }

  public final List<L2ZoneForm> getZones()
  {
    if (_zone == null)
      _zone = new FastList();
    return _zone;
  }

  public boolean isInsideZone(int x, int y)
  {
    for (L2ZoneForm zone : getZones())
    {
      if (zone.isInsideZone(x, y, zone.getHighZ()))
        return true;
    }
    return false;
  }

  public boolean isInsideZone(int x, int y, int z)
  {
    for (L2ZoneForm zone : getZones())
    {
      if (zone.isInsideZone(x, y, z))
        return true;
    }
    return false;
  }

  public boolean isInsideZone(L2Object object)
  {
    return isInsideZone(object.getX(), object.getY(), object.getZ());
  }

  public double getDistanceToZone(int x, int y)
  {
    return getZone().getDistanceToZone(x, y);
  }

  public double getDistanceToZone(L2Object object)
  {
    return getZone().getDistanceToZone(object.getX(), object.getY());
  }

  public void revalidateInZone(L2Character character)
  {
    if (_checkAffected)
    {
      if (!isAffected(character)) {
        return;
      }
    }

    if (isInsideZone(character.getX(), character.getY(), character.getZ()))
    {
      if (!_characterList.containsKey(Integer.valueOf(character.getObjectId())))
      {
        _characterList.put(Integer.valueOf(character.getObjectId()), character);
        onEnter(character);
      }

    }
    else if (_characterList.containsKey(Integer.valueOf(character.getObjectId())))
    {
      _characterList.remove(Integer.valueOf(character.getObjectId()));
      onExit(character);
    }
  }

  public void removeCharacter(L2Character character)
  {
    if (_characterList.containsKey(Integer.valueOf(character.getObjectId())))
    {
      _characterList.remove(Integer.valueOf(character.getObjectId()));
      onExit(character);
    }
  }

  public boolean isCharacterInZone(L2Character character)
  {
    return _characterList.containsKey(Integer.valueOf(character.getObjectId())); } 
  protected abstract void onEnter(L2Character paramL2Character);

  protected abstract void onExit(L2Character paramL2Character);

  protected abstract void onDieInside(L2Character paramL2Character);

  protected abstract void onReviveInside(L2Character paramL2Character);

  public FastMap<Integer, L2Character> getCharactersInside() { return _characterList;
  }

  public boolean isPvP(int x, int y)
  {
    return false;
  }

  public boolean isInsideTradeZone(int x, int y) {
    return true;
  }

  public boolean isArena() {
    return false;
  }
}