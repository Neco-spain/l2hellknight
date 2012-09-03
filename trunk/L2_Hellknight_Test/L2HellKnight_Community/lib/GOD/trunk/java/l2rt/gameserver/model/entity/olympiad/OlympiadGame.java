package l2rt.gameserver.model.entity.olympiad;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.clientpackets.Say2C;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.instances.L2OlympiadManagerInstance;
import l2rt.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Log;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class OlympiadGame
{
	private static final Logger _log = Logger.getLogger(OlympiadGame.class.getName());

	public static final int MAX_POINTS_LOOSE = 10;

	public boolean validated = false;

	private int _winner = 0;
	private int _state = 0;

	private int _id;
	private CompType _type;

	private OlympiadTeam _team1;
	private OlympiadTeam _team2;

	private GCSArray<L2Player> _spectators = new GCSArray<L2Player>();
	private GArray<L2Spawn> _buffers;

	public OlympiadGame(int id, CompType type, GCSArray<Integer> opponents)
	{
		_type = type;
		_id = id;

		_team1 = new OlympiadTeam(this, 1);
		_team2 = new OlympiadTeam(this, 2);

		for(int i = 0; i < opponents.size() / 2; i++)
			_team1.addMember(opponents.get(i));

		for(int i = opponents.size() / 2; i < opponents.size(); i++)
			_team2.addMember(opponents.get(i));

		Log.add("Olympiad System: Game - " + id + ": " + _team1.getName() + " Vs " + _team2.getName(), "olympiad");
	}

	public void addBuffers()
	{
		if(!_type.hasBuffer())
			return;

		L2Zone zone = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.OlympiadStadia, 3001 + _id, false);
		if(zone == null || zone.getSpawns() == null || zone.getSpawns().size() == 0)
		{
			_log.warning("Olympiad zone or spawns is null!!!");
			return;
		}

		_buffers = new GArray<L2Spawn>();

		for(int[] loc : zone.getSpawns())
			try
			{
				L2NpcTemplate template = NpcTable.getTemplate(36402); // Olympiad Host
				//TODO исправить координаты и heading
				L2Spawn buffer = new L2Spawn(template);
				buffer.setLocx(loc[0]);
				buffer.setLocy(loc[1]);
				buffer.setLocz(loc[2]);
				buffer.setRespawnDelay(10);
				buffer.spawnOne();
				_buffers.add(buffer);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public void deleteBuffers()
	{
		if(_buffers == null)
			return;
		for(L2Spawn spawn : _buffers)
			spawn.despawnAll();
		_buffers.clear();
		_buffers = null;
	}

	public void managerShout()
	{
		try
		{
			for(L2OlympiadManagerInstance npc : Olympiad.getNpcs())
			{
				if(_type == CompType.CLASSED)
					say(npc, "l2rt.gameserver.model.entity.OlympiadGame.OlympiadClassed", _id + 1);
				else if(_type == CompType.TEAM || _type == CompType.TEAM_RANDOM)
					say(npc, "l2rt.gameserver.model.entity.OlympiadGame.OlympiadTeam", _id + 1);
				else
					say(npc, "l2rt.gameserver.model.entity.OlympiadGame.OlympiadNonClassed", _id + 1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void say(L2OlympiadManagerInstance npc, String msg, int arenaId)
	{
		for(L2Player player : L2World.getAroundPlayers(npc, 4000, 1000))
			if(player != null && !player.isBlockAll())
			{
				String[] m = new CustomMessage(msg, player).addNumber(arenaId).toString().split(": ");
				player.sendPacket(new Say2(0, Say2C.SHOUT, m[0], m[1]));
			}
	}

	public void clearArena()
	{
		L2Zone zone = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.OlympiadStadia, 3001 + getId(), false);
		if(zone != null)
			for(L2Player player : zone.getInsidePlayers())
				player.teleToClosestTown();
	}
	
	public void portPlayersToArena()
	{
		_team1.portPlayersToArena();
		_team2.portPlayersToArena();
	}

	public void preparePlayers()
	{
		_team1.preparePlayers();
		_team2.preparePlayers();
	}

	public void portPlayersBack()
	{
		_team1.portPlayersBack();
		_team2.portPlayersBack();
	}

	public void validateWinner(boolean aborted) throws Exception
	{
		int state = _state;
		_state = 0;

		if(validated)
		{
			Log.add("Olympiad Result: " + _team1.getName() + " vs " + _team2.getName() + " ... double validate check!!!", "olympiad");
			return;
		}
		validated = true;

		// Если игра закончилась до телепортации на стадион, то забираем очки у вышедших из игры, не засчитывая никому победу
		if(state < 1 && aborted)
		{
			_team1.takePointsForCrash();
			_team2.takePointsForCrash();
			broadcastPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME, true, false);
			return;
		}

		boolean teamOneCheck = _team1.checkPlayers();
		boolean teamTwoCheck = _team2.checkPlayers();

		if(_winner <= 0)
			if(!teamOneCheck && !teamTwoCheck)
				_winner = 0;
			else if(!teamTwoCheck)
				_winner = 1; // Выиграла первая команда
			else if(!teamOneCheck)
				_winner = 2; // Выиграла вторая команда
			else if(_team1.getDamage() < _team2.getDamage()) // Вторая команда нанесла вреда меньше, чем первая
				_winner = 1; // Выиграла первая команда
			else if(_team1.getDamage() > _team2.getDamage()) // Вторая команда нанесла вреда больше, чем первая
				_winner = 2; // Выиграла вторая команда

		try
		{
			if(_winner == 1) // Выиграла первая команда
				_team1.winGame(_team2);
			else if(_winner == 2) // Выиграла вторая команда
				_team2.winGame(_team1);
			else
				_team1.tie(_team2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		_team1.saveNobleData();
		_team2.saveNobleData();
		_team1.stopEffect();
		_team2.stopEffect();

		broadcastPacket(new SystemMessage(SystemMessage.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S).addNumber(20), true, true);
	}

	public void openDoors() throws Exception
	{
		for(int n : Olympiad.STADIUMS[_id].getDoors())
			DoorTable.getInstance().getDoor(n).openMe();
	}

	public void closeDoors()
	{
		for(int n : Olympiad.STADIUMS[_id].getDoors())
			DoorTable.getInstance().getDoor(n).closeMe();
	}

	public int getId()
	{
		return _id;
	}

	public String getTitle()
	{
		return _team1.getName() + " vs " + _team2.getName();
	}

	public String getTeam1Title()
	{
		String title = "";
		for(TeamMember member : _team1.getMembers())
			title += (title.isEmpty() ? "" : ", ") + member.getName();
		return "<font color=\"blue\">" + title + "</font>";
	}

	public String getTeam2Title()
	{
		String title = "";
		for(TeamMember member : _team2.getMembers())
			title += (title.isEmpty() ? "" : ", ") + member.getName();
		return "<font color=\"red\">" + title + "</font>";
	}

	public boolean isRegistered(int objId)
	{
		return _team1.contains(objId) || _team2.contains(objId);
	}

	public GCSArray<L2Player> getSpectators()
	{
		return _spectators;
	}

	public void addSpectator(L2Player spec)
	{
		_spectators.add(spec);
	}

	public void removeSpectator(L2Player spec)
	{
		_spectators.remove(spec);
	}

	public void clearSpectators()
	{
		for(L2Player pc : _spectators)
			if(pc != null && pc.inObserverMode())
				pc.leaveOlympiadObserverMode();
		_spectators.clear();
	}

	public void broadcastInfo(L2Player sender, L2Player receiver, boolean onlyToSpectators)
	{
		// TODO заюзать пакеты:
		// ExEventMatchCreate
		// ExEventMatchFirecracker
		// ExEventMatchManage
		// ExEventMatchMessage
		// ExEventMatchObserver
		// ExEventMatchScore
		// ExEventMatchTeamInfo
		// ExEventMatchTeamUnlocked
		// ExEventMatchUserInfo

		if(sender != null)
			if(receiver != null)
				receiver.sendPacket(new ExOlympiadUserInfo(sender, sender.getOlympiadSide()));
			else
				broadcastPacket(new ExOlympiadUserInfo(sender, sender.getOlympiadSide()), !onlyToSpectators, true);
		else
		{
			// Рассылаем информацию о первой команде
			for(L2Player player : _team1.getPlayers())
				if(receiver != null)
					receiver.sendPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
				else
					broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()), !onlyToSpectators, true);

			// Рассылаем информацию о второй команде
			for(L2Player player : _team2.getPlayers())
				if(receiver != null)
					receiver.sendPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
				else
					broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()), !onlyToSpectators, true);
		}
	}

	public void broadcastPacket(L2GameServerPacket packet, boolean toTeams, boolean toSpectators)
	{
		if(toTeams)
		{
			_team1.broadcast(packet);
			_team2.broadcast(packet);
		}

		if(toSpectators && _spectators != null)
			for(L2Player spec : _spectators)
				if(spec != null)
					spec.sendPacket(packet);
	}

	public void setWinner(int val)
	{
		_winner = val;
	}

	public void setState(int val)
	{
		_state = val;
	}

	public int getState()
	{
		return _state;
	}

	public GArray<L2Player> getTeamMembers(L2Player player)
	{
		return player.getOlympiadSide() == 1 ? _team1.getPlayers() : _team2.getPlayers();
	}

	public void addDamage(L2Player player, double damage)
	{
		if(player.getOlympiadSide() == 1)
			_team1.addDamage(damage);
		else
			_team2.addDamage(damage);
	}

	public boolean doDie(L2Player player)
	{
		return player.getOlympiadSide() == 1 ? _team1.doDie(player) : _team2.doDie(player);
	}

	public boolean checkPlayersOnline()
	{
		return _team1.checkPlayers() && _team2.checkPlayers();
	}

	public boolean logoutPlayer(L2Player player)
	{
		if(player == null)
			return false;
		return player.getOlympiadSide() == 1 ? _team1.logout(player) : _team2.logout(player);
	}

	OlympiadGameTask _task;
	ScheduledFuture<?> _shedule;

	public synchronized void sheduleTask(OlympiadGameTask task)
	{
		if(_shedule != null)
			_shedule.cancel(false);
		_task = task;
		_shedule = task.shedule();
	}

	public OlympiadGameTask getTask()
	{
		return _task;
	}

	public BattleStatus getStatus()
	{
		if(_task != null)
			return _task.getStatus();
		return BattleStatus.Begining;
	}

	public void endGame(long time, boolean aborted)
	{
		try
		{
			validateWinner(aborted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		sheduleTask(new OlympiadGameTask(this, BattleStatus.Ending, 0, time));
	}

	public CompType getType()
	{
		return _type;
	}
}