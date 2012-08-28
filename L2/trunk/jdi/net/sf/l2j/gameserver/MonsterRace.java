package net.sf.l2j.gameserver;

import java.lang.reflect.Constructor;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class MonsterRace
{
  private L2NpcInstance[] _monsters;
  private static MonsterRace _instance;
  private Constructor _constructor;
  private int[][] _speeds;
  private int[] _first;
  private int[] _second;

  private MonsterRace()
  {
    _monsters = new L2NpcInstance[8];
    _speeds = new int[8][20];
    _first = new int[2];
    _second = new int[2];
  }

  public static MonsterRace getInstance()
  {
    if (_instance == null)
    {
      _instance = new MonsterRace();
    }

    return _instance;
  }

  public void newRace()
  {
    int random = 0;

    for (int i = 0; i < 8; i++)
    {
      int id = 31003;
      random = Rnd.get(24);

      for (int j = i - 1; j >= 0; j--)
      {
        if (_monsters[j].getTemplate().npcId != id + random)
          continue;
        random = Rnd.get(24);
      }

      try
      {
        L2NpcTemplate template = NpcTable.getInstance().getTemplate(id + random);
        _constructor = java.lang.Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.type + "Instance").getConstructors()[0];
        int objectId = IdFactory.getInstance().getNextId();
        _monsters[i] = ((L2NpcInstance)_constructor.newInstance(new Object[] { Integer.valueOf(objectId), template }));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    newSpeeds();
  }

  public void newSpeeds()
  {
    _speeds = new int[8][20];
    int total = 0;
    _first[1] = 0; _second[1] = 0;
    for (int i = 0; i < 8; i++)
    {
      total = 0;
      for (int j = 0; j < 20; j++)
      {
        if (j == 19)
          _speeds[i][j] = 100;
        else
          _speeds[i][j] = (Rnd.get(60) + 65);
        total += _speeds[i][j];
      }
      if (total >= _first[1])
      {
        _second[0] = _first[0];
        _second[1] = _first[1];
        _first[0] = (8 - i);
        _first[1] = total;
      } else {
        if (total < _second[1])
          continue;
        _second[0] = (8 - i);
        _second[1] = total;
      }
    }
  }

  public L2NpcInstance[] getMonsters()
  {
    return _monsters;
  }

  public int[][] getSpeeds()
  {
    return _speeds;
  }

  public int getFirstPlace()
  {
    return _first[0];
  }

  public int getSecondPlace()
  {
    return _second[0];
  }
}