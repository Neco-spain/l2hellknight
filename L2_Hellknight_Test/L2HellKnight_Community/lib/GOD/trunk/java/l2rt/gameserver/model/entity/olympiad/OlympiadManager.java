package l2rt.gameserver.model.entity.olympiad;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Rnd;

import java.util.Collection;
import java.util.Map;

public class OlympiadManager implements Runnable
{
	private FastMap<Integer, OlympiadGame> _olympiadInstances = new FastMap<Integer, OlympiadGame>().setShared(true);

	public void wait2(long time)
	{
		try
		{
			wait(time);
		}
		catch(InterruptedException ex)
		{}
	}

	@Override
	public synchronized void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		while(Olympiad.inCompPeriod())
		{
			if(Olympiad._nobles.isEmpty())
			{
				wait2(60000);
				continue;
			}

			while(Olympiad.inCompPeriod())
			{
				// Подготовка и запуск внеклассовых боев
				if(Olympiad._nonClassBasedRegisters.size() >= Config.NONCLASS_GAME_MIN)
					prepareBattles(CompType.NON_CLASSED, Olympiad._nonClassBasedRegisters);

				// Подготовка и запуск классовых боев
				for(Map.Entry<Integer, GCSArray<Integer>> entry : Olympiad._classBasedRegisters.entrySet())
					if(entry.getValue().size() >= Config.CLASS_GAME_MIN)
						prepareBattles(CompType.CLASSED, entry.getValue());

				// Подготовка и запуск командных боев случайного типа
				if(Olympiad._teamRandomBasedRegisters.size() >= Config.RANDOM_TEAM_GAME_MIN)
					prepareBattles(CompType.TEAM_RANDOM, Olympiad._teamRandomBasedRegisters);

				// Подготовка и запуск командных боев
				if(Olympiad._teamBasedRegisters.size() >= Config.TEAM_GAME_MIN)
					prepareTeamBattles(CompType.TEAM, Olympiad._teamBasedRegisters.values());

				wait2(30000);
			}

			wait2(30000);
		}

		Olympiad._classBasedRegisters.clear();
		Olympiad._nonClassBasedRegisters.clear();
		Olympiad._teamRandomBasedRegisters.clear();
		Olympiad._teamBasedRegisters.clear();

		// when comp time finish wait for all games terminated before execute the cleanup code
		boolean allGamesTerminated = false;

		// wait for all games terminated
		while(!allGamesTerminated)
		{
			wait2(30000);

			if(_olympiadInstances.isEmpty())
				break;

			allGamesTerminated = true;
			for(OlympiadGame game : _olympiadInstances.values())
				if(game.getTask() != null && !game.getTask().isTerminated())
					allGamesTerminated = false;
		}

		_olympiadInstances.clear();
	}

	private void prepareBattles(CompType type, GCSArray<Integer> list)
	{
		for(int i = 0; i < Olympiad.STADIUMS.length; i++)
			try
			{
				if(!Olympiad.STADIUMS[i].isFreeToUse())
					continue;
				if(list.size() < type.getMinSize())
					break;

				OlympiadGame game = new OlympiadGame(i, type, nextOpponents(list, type));
				game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1));

				_olympiadInstances.put(i, game);

				Olympiad.STADIUMS[i].setStadiaBusy();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	private void prepareTeamBattles(CompType type, Collection<GCSArray<Integer>> list)
	{
		for(int i = 0; i < Olympiad.STADIUMS.length; i++)
			try
			{
				if(!Olympiad.STADIUMS[i].isFreeToUse())
					continue;
				if(list.size() < type.getMinSize())
					break;

				GCSArray<Integer> nextOpponents = nextTeamOpponents(list, type);
				if(nextOpponents == null)
					break;

				OlympiadGame game = new OlympiadGame(i, type, nextOpponents);
				game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1));

				_olympiadInstances.put(i, game);

				Olympiad.STADIUMS[i].setStadiaBusy();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public void freeOlympiadInstance(int index)
	{
		_olympiadInstances.remove(index);
		Olympiad.STADIUMS[index].setStadiaFree();
	}

	public OlympiadGame getOlympiadInstance(int index)
	{
		return _olympiadInstances.get(index);
	}

	public FastMap<Integer, OlympiadGame> getOlympiadGames()
	{
		return _olympiadInstances;
	}

	private GCSArray<Integer> nextOpponents(GCSArray<Integer> list, CompType type)
	{
		GCSArray<Integer> opponents = new GCSArray<Integer>();

		Integer noble;

		for(int i = 0; i < type.getMinSize(); i++)
		{
			noble = list.remove(Rnd.get(list.size()));
			opponents.add(noble);
			removeOpponent(noble);
		}

		return opponents;
	}

	private GCSArray<Integer> nextTeamOpponents(Collection<GCSArray<Integer>> list, CompType type)
	{
		GCSArray<Integer> opponents = new GCSArray<Integer>();
		GArray<GCSArray<Integer>> a = new GArray<GCSArray<Integer>>();
		a.addAll(list);

		for(int i = 0; i < type.getMinSize(); i++)
		{
			if(a.isEmpty())
			{
				System.out.println("OlympiadManager[182] error!");
				Thread.dumpStack();
				return null;
			}
			GCSArray<Integer> team = a.remove(Rnd.get(a.size()));
			if(team.size() == 3)
				for(Integer noble : team)
				{
					opponents.add(noble);
					removeOpponent(noble);
				}
			else
			// Дисквалифицируем команды с количеством менее 3-х
			{
				for(Integer noble : team)
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
		Olympiad._teamRandomBasedRegisters.remove(noble);
		Olympiad._teamBasedRegisters.removeValue(noble);
	}
}