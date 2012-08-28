package net.sf.l2j.gameserver.model.entity.olympiad;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.MultiValueIntegerMap;
import net.sf.l2j.util.Rnd;

public class OlympiadManager
  implements Runnable
{
  private FastMap<Integer, OlympiadGame> _olympiadInstances = new FastMap().setShared(true);

  public void wait2(long time) {
    try {
      wait(time);
    }
    catch (InterruptedException ex) {
    }
  }

  public synchronized void run() {
    if (Olympiad.isOlympiadEnd()) {
      return;
    }

    while (Olympiad.inCompPeriod()) {
      if (Olympiad._nobles.isEmpty()) {
        wait2(60000L);
        continue;
      }

      while (Olympiad.inCompPeriod())
      {
        if (Olympiad._nonClassBasedRegisters.size() >= Config.ALT_OLY_MINNONCLASS) {
          prepareBattles(CompType.NON_CLASSED, Olympiad._nonClassBasedRegisters);
        }

        for (Map.Entry entry : Olympiad._classBasedRegisters.entrySet()) {
          if (((FastList)entry.getValue()).size() >= Config.ALT_OLY_MINCLASS) {
            prepareBattles(CompType.CLASSED, (FastList)entry.getValue());
          }
        }

        wait2(30000L);
      }

      wait2(30000L);
    }

    Olympiad._classBasedRegisters.clear();
    Olympiad._nonClassBasedRegisters.clear();

    Olympiad._ips.clear();
    Olympiad._hwids.clear();

    boolean allGamesTerminated = false;

    while (!allGamesTerminated) {
      wait2(30000L);

      if (_olympiadInstances.isEmpty())
      {
        break;
      }
      allGamesTerminated = true;
      for (OlympiadGame game : _olympiadInstances.values()) {
        if ((game.getTask() != null) && (!game.getTask().isTerminated())) {
          allGamesTerminated = false;
        }
      }
    }

    _olympiadInstances.clear();
  }

  private void prepareBattles(CompType type, FastList<Integer> list) {
    for (int i = 0; i < Olympiad.STADIUMS.length; i++)
      try {
        if (!Olympiad.STADIUMS[i].isFreeToUse()) {
          continue;
        }
        if (list.size() < type.getMinSize())
        {
          break;
        }
        OlympiadGame game = new OlympiadGame(i, type, nextOpponents(list, type));
        game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1L));

        _olympiadInstances.put(Integer.valueOf(i), game);

        Olympiad.STADIUMS[i].setStadiaBusy();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  public void freeOlympiadInstance(int index)
  {
    _olympiadInstances.remove(Integer.valueOf(index));
    Olympiad.STADIUMS[index].setStadiaFree();
  }

  public OlympiadGame getOlympiadInstance(int index) {
    return (OlympiadGame)_olympiadInstances.get(Integer.valueOf(index));
  }

  public FastMap<Integer, OlympiadGame> getOlympiadGames() {
    return _olympiadInstances;
  }

  private FastList<Integer> nextOpponents(FastList<Integer> list, CompType type) {
    FastList opponents = new FastList();

    for (int i = 0; i < type.getMinSize(); i++) {
      Integer noble = (Integer)list.remove(Rnd.get(list.size()));
      opponents.add(noble);
      removeOpponent(noble);
    }

    return opponents;
  }

  private void removeOpponent(Integer noble) {
    Olympiad._classBasedRegisters.removeValue(noble);
    Olympiad._nonClassBasedRegisters.remove(noble);
    L2PcInstance player = L2World.getInstance().getPlayer(noble.intValue());
    if (player != null)
      player.olympiadClear();
  }
}