package l2rt.gameserver.model.entity.olympiad;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Log;

public class OlympiadTeam
{
	private OlympiadGame _game;
	private GCSArray<TeamMember> _members;
	private String _name = "";
	private int _side;
	private double _damage;

	public OlympiadTeam(OlympiadGame game, int side)
	{
		_game = game;
		_side = side;
		_members = new GCSArray<TeamMember>();
	}

	public void addMember(int obj_id)
	{
		String player_name = "";
		L2Player player = L2ObjectsStorage.getPlayer(obj_id);
		if(player != null)
			player_name = player.getName();
		else
		{
			StatsSet noble = Olympiad._nobles.get(new Integer(obj_id));
			if(noble != null)
				player_name = noble.getString(Olympiad.CHAR_NAME, "");
		}

		_members.add(new TeamMember(obj_id, player_name, _game, _side));

		switch(_game.getType())
		{
			case CLASSED:
			case NON_CLASSED:
				_name = player_name;
				break;
			case TEAM_RANDOM:
				_name = "Team " + _side;
				break;
			case TEAM:
				if(_name.isEmpty()) // Берется имя первого игрока в команде
					_name = player_name + " team";
				break;
		}
	}

	public void addDamage(double damage)
	{
		_damage += damage;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getName()
	{
		return _name;
	}

	public void portPlayersToArena()
	{
		for(TeamMember member : _members)
			member.portPlayerToArena();
	}

	public void stopEffect()  
	{  
		for (TeamMember member : _members)  
			member.stopEffect();  
	}  

	public void portPlayersBack()
	{
		for(TeamMember member : _members)
			member.portPlayerBack();
	}

	public void preparePlayers()
	{
		for(TeamMember member : _members)
			member.preparePlayer();

		if(_members.size() <= 1)
			return;

		GArray<L2Player> list = new GArray<L2Player>();
		for(TeamMember member : _members)
		{
			L2Player player = member.getPlayer();
			if(player != null)
			{
				list.add(player);
				if(player.getParty() != null)
				{
					L2Party party = player.getParty();
					party.oustPartyMember(player);
				}
			}
		}

		if(list.size() <= 1)
			return;

		L2Player leader = list.get(0);
		if(leader == null)
			return;

		L2Party party = new L2Party(leader, 0);
		leader.setParty(party);

		for(L2Player player : list)
			if(player != leader)
				player.joinParty(party);
	}

	public void takePointsForCrash()
	{
		for(TeamMember member : _members)
			member.takePointsForCrash();
	}

	public boolean checkPlayers()
	{
		for(TeamMember member : _members)
			if(member.checkPlayer())
				return true;
		return false;
	}

	public boolean isAllDead()
	{
		for(TeamMember member : _members)
			if(!member.isDead() && member.checkPlayer())
				return false;
		return true;
	}

	public boolean contains(int objId)
	{
		for(TeamMember member : _members)
			if(member.getObjId() == objId)
				return true;
		return false;
	}

	public GArray<L2Player> getPlayers()
	{
		GArray<L2Player> players = new GArray<L2Player>();
		for(TeamMember member : _members)
		{
			L2Player player = member.getPlayer();
			if(player != null)
				players.add(player);
		}
		return players;
	}

	public GCSArray<TeamMember> getMembers()
	{
		return _members;
	}

	public void broadcast(L2GameServerPacket p)
	{
		for(TeamMember member : _members)
		{
			L2Player player = member.getPlayer();
			if(player != null)
				player.sendPacket(p);
		}
	}

	public void broadcastInfo()
	{
		for(TeamMember member : _members)
		{
			L2Player player = member.getPlayer();
			if(player != null)
				player.broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
		}
	}

	public boolean logout(L2Player player)
	{
		if(player != null)
			for(TeamMember member : _members)
			{
				L2Player pl = member.getPlayer();
				if(pl != null && pl == player)
					member.logout();
			}
		return checkPlayers();
	}

	public boolean doDie(L2Player player)
	{
		if(player != null)
			for(TeamMember member : _members)
			{
				L2Player pl = member.getPlayer();
				if(pl != null && pl == player)
					member.doDie();
			}
		return isAllDead();
	}

	public void winGame(OlympiadTeam looseTeam)
	{
		int pointDiff = 0;

		for(int i = 0; i < _members.size(); i++)
			try
			{
				pointDiff += transferPoints(looseTeam.getMembers().get(i).getStat(), getMembers().get(i).getStat());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		for(L2Player player : getPlayers())
			try
			{
				L2ItemInstance item = player.getInventory().addItem(Config.ALT_OLY_BATTLE_REWARD_ITEM, _game.getType().getReward());
				player.sendPacket(SystemMessage.obtainItems(item.getItemId(), _game.getType().getReward(), 0));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		_game.broadcastPacket(new SystemMessage(SystemMessage.S1_HAS_WON_THE_GAME).addString(getName()), true, true);
		_game.broadcastPacket(new SystemMessage(SystemMessage.C1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES).addString(getName()).addNumber(pointDiff), true, false);
		_game.broadcastPacket(new SystemMessage(SystemMessage.C1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES).addString(looseTeam.getName()).addNumber(pointDiff), true, false);

		Log.add("Olympiad Result: " + getName() + " vs " + looseTeam.getName() + " ... (" + (int) _damage + " vs " + (int) looseTeam.getDamage() + ") " + getName() + " win " + pointDiff + " points", "olympiad");
	}

	public void tie(OlympiadTeam otherTeam)
	{
		for(int i = 0; i < _members.size(); i++)
			try
			{
				StatsSet stat1 = getMembers().get(i).getStat();
				StatsSet stat2 = otherTeam.getMembers().get(i).getStat();
				stat1.set(Olympiad.POINTS, stat1.getInteger(Olympiad.POINTS) - 2);
				stat2.set(Olympiad.POINTS, stat2.getInteger(Olympiad.POINTS) - 2);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		_game.broadcastPacket(Msg.THE_GAME_ENDED_IN_A_TIE, true, true);

		Log.add("Olympiad Result: " + getName() + " vs " + otherTeam.getName() + " ... tie", "olympiad");
	}

	private int transferPoints(StatsSet from, StatsSet to)
	{
		int fromPoints = from.getInteger(Olympiad.POINTS);
		int fromLoose = from.getInteger(Olympiad.COMP_LOOSE);
		int fromPlayed = from.getInteger(Olympiad.COMP_DONE);

		int toPoints = to.getInteger(Olympiad.POINTS);
		int toWin = to.getInteger(Olympiad.COMP_WIN);
		int toPlayed = to.getInteger(Olympiad.COMP_DONE);

		int pointDiff = Math.max(1, Math.min(fromPoints, toPoints) / _game.getType().getLooseMult());
		pointDiff = pointDiff > OlympiadGame.MAX_POINTS_LOOSE ? OlympiadGame.MAX_POINTS_LOOSE : pointDiff;

		from.set(Olympiad.POINTS, fromPoints - pointDiff);
		from.set(Olympiad.COMP_LOOSE, fromLoose + 1);
		from.set(Olympiad.COMP_DONE, fromPlayed + 1);

		to.set(Olympiad.POINTS, toPoints + pointDiff);
		to.set(Olympiad.COMP_WIN, toWin + 1);
		to.set(Olympiad.COMP_DONE, toPlayed + 1);

		return pointDiff;
	}

	public void saveNobleData()
	{
		for(TeamMember member : _members)
			member.saveNobleData();
	}
}