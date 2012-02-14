package l2rt.gameserver.model.entity.olympiad;

import l2rt.extensions.network.MMOConnection;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.instances.L2CubicInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.skills.SkillTimeStamp;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.Location;
import l2rt.util.Log;

import java.util.concurrent.ConcurrentSkipListSet;

public class TeamMember
{
	private OlympiadGame _game;
	private L2Player _player;
	private int _objId;
	private String _name = "";
	private CompType _type;
	private int _side;
	private Location _returnLoc;
	private long _returnRef;
	private boolean _isDead;

	public boolean isDead()
	{
		return _isDead;
	}

	public void doDie()
	{
		_isDead = true;
	}

	public TeamMember(int obj_id, String name, OlympiadGame game, int side)
	{
		_objId = obj_id;
		_name = name;
		_game = game;
		_type = game.getType();
		_side = side;

		L2Player player = L2ObjectsStorage.getPlayer(obj_id);
		if(player == null)
			return;

		_player = player;

		try
		{
			if(player.inObserverMode())
				if(player.getOlympiadObserveId() > 0)
					player.leaveOlympiadObserverMode();
				else
					player.leaveObserverMode();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		player.setOlympiadSide(side);
		player.setOlympiadGameId(game.getId());
	}

	public StatsSet getStat()
	{
		return Olympiad._nobles.get(_objId);
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
			try
			{
				StatsSet stat = getStat();
				int points = stat.getInteger(Olympiad.POINTS);
				int diff = Math.min(OlympiadGame.MAX_POINTS_LOOSE, points / _type.getLooseMult());
				stat.set(Olympiad.POINTS, points - diff);
				Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

				// TODO: Снести подробный лог после исправления беспричинного отъёма очков.
				L2Player player = _player;
				if(player == null)
					Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
				else
				{
					if(player.isLogoutStarted())
						Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
					if(!player.isOnline())
						Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
					if(player.getOlympiadGameId() == -1)
						Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGameId() == -1", "olympiad");
					if(player.getOlympiadObserveId() > 0)
						Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadObserveId() > 0", "olympiad");
					L2GameClient client = player.getNetConnection();
					if(client == null)
						Log.add("Olympiad info: " + _name + " crashed: client == null", "olympiad");
					else
					{
						MMOConnection conn = client.getConnection();
						if(conn == null)
							Log.add("Olympiad info: " + _name + " crashed coz conn == null", "olympiad");
						else if(conn.isClosed())
							Log.add("Olympiad info: " + _name + " crashed coz conn.isClosed()", "olympiad");
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public boolean checkPlayer()
	{
		L2Player player = _player;
		if(player == null || player.isLogoutStarted() || !player.isOnline() || player.getOlympiadGameId() == -1 || player.getOlympiadObserveId() > 0)
			return false;
		L2GameClient client = player.getNetConnection();
		if(client == null)
			return false;
		MMOConnection conn = client.getConnection();
		if(conn == null || conn.isClosed())
			return false;
		return true;
	}

	public void stopEffect()  
	{  
		L2Player player = _player;  
		if(player == null || !checkPlayer())  
		{  
			_player = null;  
			return;  
		}                       
		try  
		{  
			player.getEffectList().stopAllEffects();  
		}  
		catch(Exception e)  
		{  
			e.printStackTrace();  
		}  
	}  
	
	public void portPlayerToArena()
	{
		L2Player player = _player;
		if(!checkPlayer() || player == null || player.isTeleporting())
		{
			_player = null;
			return;
		}

		try
		{
			_returnLoc = player.getLoc();
			_returnRef = player.getReflection().getId();

			if(player.isDead())
				player.setIsPendingRevive(true);
			if(player.isSitting())
				player.standUp();

			player.setTarget(null);
			player.setIsInOlympiadMode(true);

			if(player.getParty() != null)
			{
				L2Party party = player.getParty();
				party.oustPartyMember(player);
			}

			L2Zone zone = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.OlympiadStadia, 3001 + _game.getId(), false);
			int[] tele = zone.getSpawns().get(_side - 1);

			player.teleToLocation(tele[0], tele[1], tele[2], 0);

			if(_type == CompType.TEAM_RANDOM || _type == CompType.TEAM)
				player.setTeam(_side, true);

			player.sendPacket(new ExOlympiadMode(_side));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void portPlayerBack()
	{
		L2Player player = _player;
		if(player == null)
			return;

		try
		{
			player.setIsInOlympiadMode(false);
			player.setOlympiadSide(-1);
			player.setOlympiadGameId(-1);

			if(_type == CompType.TEAM_RANDOM || _type == CompType.TEAM)
				player.setTeam(0, true);

			player.getEffectList().stopAllEffects();

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentMp(player.getMaxMp());

			if(player.isDead())
			{
				player.setCurrentHp(player.getMaxHp(), true);
				player.broadcastPacket(new Revive(player));
			}
			else
				player.setCurrentHp(player.getMaxHp(), false);

			// Add clan skill
			if(player.getClan() != null)
				for(L2Skill skill : player.getClan().getAllSkills())
					if(skill.getMinPledgeClass() <= player.getPledgeClass())
						player.addSkill(skill, false);

			// Add Hero Skills
			if(player.isHero())
				Hero.addSkills(player);

			// Обновляем скилл лист, после добавления скилов
			player.sendPacket(new SkillList(player));
			player.sendPacket(new ExOlympiadMode(0));
			player.sendPacket(new ExOlympiadMatchEnd());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if(_returnLoc != null)
			{
				player.setReflection(_returnRef);
				player.teleToLocation(_returnLoc);
			}
			else
			{
				player.setReflection(0);
				player.teleToClosestTown();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void preparePlayer()
	{
		L2Player player = _player;
		if(player == null)
			return;

		try
		{
			// Remove Buffs
			player.getEffectList().stopAllEffects();

			// Сброс кулдауна скилов с базовым реюзом 15 мин и менее
			boolean reseted = false;
			for(SkillTimeStamp sts : player.getSkillReuseTimeStamps().values())
				if(sts.getReuseBasic() <= 15 * 60 * 1000 && !SkillTable.getInstance().getInfo(sts.getSkill(), 1).isItemSkill())
				{
					player.enableSkill(sts.getSkill());
					reseted = true;
				}
			if(reseted)
				player.sendPacket(new SkillCoolTime(player));

			// Remove clan skill
			if(player.getClan() != null)
				for(L2Skill skill : player.getClan().getAllSkills())
					player.removeSkill(skill, false);

			// Remove Hero Skills
			if(player.isHero())
				Hero.removeSkills(player);

			// Abort casting if player casting
			if(player.isCastingNow())
				player.abortCast(true);

			// Удаляем чужие кубики
			for(L2CubicInstance cubic : player.getCubics())
				if(cubic.isGivenByOther())
					cubic.deleteMe(false);

			// Remove Summon's Buffs
			if(player.getPet() != null)
			{
				L2Summon summon = player.getPet();
				if(summon.isPet())
					summon.unSummon();
				else
					summon.getEffectList().stopAllEffects();
			}

			// unsummon agathion
			if(player.getAgathion() != null)
				player.setAgathion(0);

			// Обновляем скилл лист, после удаления скилов
			player.sendPacket(new SkillList(player));

			// Remove Hero weapons
			L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if(wpn != null && wpn.isHeroWeapon())
			{
				player.getInventory().unEquipItem(wpn);
				player.abortAttack(true, true);
				player.validateItemExpertisePenalties(false, false, true);
			}

			// remove bsps/sps/ss automation
			ConcurrentSkipListSet<Integer> activeSoulShots = player.getAutoSoulShot();
			for(int itemId : activeSoulShots)
			{
				player.removeAutoSoulShot(itemId);
				player.sendPacket(new ExAutoSoulShot(itemId, false));
			}

			// Разряжаем заряженные соул и спирит шоты
			L2ItemInstance weapon = player.getActiveWeaponInstance();
			if(weapon != null)
			{
				weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}

			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());

			player.broadcastUserInfo(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void saveNobleData()
	{
		OlympiadDatabase.saveNobleData(_objId);
	}

	public void logout()
	{
		_player = null;
	}

	public L2Player getPlayer()
	{
		return _player;
	}

	public int getObjId()
	{
		return _objId;
	}

	public String getName()
	{
		return _name;
	}
}