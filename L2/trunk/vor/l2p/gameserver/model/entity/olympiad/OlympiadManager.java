package l2p.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.utils.MultiValueIntegerMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadManager extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadManager.class);

  private Map<Integer, OlympiadGame> _olympiadInstances = new ConcurrentHashMap();

  public void sleep(long time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException e)
    {
    }
  }

  public void runImpl() throws Exception
  {
    if (Olympiad.isOlympiadEnd()) {
      return;
    }
    while (Olympiad.inCompPeriod())
    {
      if (Olympiad._nobles.isEmpty())
      {
        sleep(60000L);
        continue;
      }

      while (Olympiad.inCompPeriod())
      {
        if (Olympiad._nonClassBasedRegisters.size() >= Config.NONCLASS_GAME_MIN) {
          prepareBattles(CompType.NON_CLASSED, Olympiad._nonClassBasedRegisters);
        }

        for (Map.Entry entry : Olympiad._classBasedRegisters.entrySet()) {
          if (((List)entry.getValue()).size() >= Config.CLASS_GAME_MIN) {
            prepareBattles(CompType.CLASSED, (List)entry.getValue());
          }
        }
        if (Olympiad._teamBasedRegisters.size() >= Config.TEAM_GAME_MIN) {
          prepareTeamBattles(CompType.TEAM, Olympiad._teamBasedRegisters.values());
        }
        sleep(30000L);
      }

      sleep(30000L);
    }

    Olympiad._classBasedRegisters.clear();
    Olympiad._nonClassBasedRegisters.clear();
    Olympiad._teamBasedRegisters.clear();

    boolean allGamesTerminated = false;

    while (!allGamesTerminated)
    {
      sleep(30000L);

      if (_olympiadInstances.isEmpty()) {
        break;
      }
      allGamesTerminated = true;
      for (OlympiadGame game : _olympiadInstances.values()) {
        if ((game.getTask() != null) && (!game.getTask().isTerminated()))
          allGamesTerminated = false;
      }
    }
    _olympiadInstances.clear();
  }

  private void prepareBattles(CompType type, List<Integer> list)
  {
    for (int i = 0; i < Olympiad.STADIUMS.length; i++)
      try
      {
        if (!Olympiad.STADIUMS[i].isFreeToUse())
          continue;
        if (list.size() < type.getMinSize()) {
          break;
        }
        OlympiadGame game = new OlympiadGame(i, type, nextOpponents(list, type));
        game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1L));

        _olympiadInstances.put(Integer.valueOf(i), game);

        Olympiad.STADIUMS[i].setStadiaBusy();
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
  }

  private void prepareTeamBattles(CompType type, Collection<List<Integer>> list)
  {
    for (int i = 0; i < Olympiad.STADIUMS.length; i++)
      try
      {
        if (!Olympiad.STADIUMS[i].isFreeToUse())
          continue;
        if (list.size() < type.getMinSize()) {
          break;
        }
        List nextOpponents = nextTeamOpponents(list, type);
        if (nextOpponents == null) {
          break;
        }
        OlympiadGame game = new OlympiadGame(i, type, nextOpponents);
        game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1L));

        _olympiadInstances.put(Integer.valueOf(i), game);

        Olympiad.STADIUMS[i].setStadiaBusy();
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
  }

  public void freeOlympiadInstance(int index)
  {
    _olympiadInstances.remove(Integer.valueOf(index));
    Olympiad.STADIUMS[index].setStadiaFree();
  }

  public OlympiadGame getOlympiadInstance(int index)
  {
    return (OlympiadGame)_olympiadInstances.get(Integer.valueOf(index));
  }

  public Map<Integer, OlympiadGame> getOlympiadGames()
  {
    return _olympiadInstances;
  }

  private List<Integer> nextOpponents(List<Integer> list, CompType type)
  {
    List opponents = new CopyOnWriteArrayList();

    for (int i = 0; i < type.getMinSize(); i++)
    {
      Integer noble = (Integer)list.remove(Rnd.get(list.size()));
      opponents.add(noble);
      removeOpponent(noble);
    }

    return opponents;
  }

  private List<Integer> nextTeamOpponents(Collection<List<Integer>> list, CompType type)
  {
    if (list.isEmpty())
      return null;
    List opponents = new CopyOnWriteArrayList();
    List a = new ArrayList();
    a.addAll(list);

    for (int i = 0; i < type.getMinSize(); i++)
    {
      if (a.size() < 1)
        continue;
      List team = (List)a.remove(Rnd.get(a.size()));
      if (team.size() == 3) {
        for (Integer noble : team)
        {
          opponents.add(noble);
          removeOpponent(noble);
        }
      }
      else {
        for (Integer noble : team)
          removeOpponent(noble);
        i--;
      }

      list.remove(team);
    }

    return opponents;
  }

  private void removeOpponent(Integer noble)
  {
    Olympiad._classBasedRegisters.removeValue(noble);
    Olympiad._nonClassBasedRegisters.remove(noble);
    Olympiad._teamBasedRegisters.removeValue(noble);
  }
}