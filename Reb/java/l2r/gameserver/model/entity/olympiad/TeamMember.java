package l2r.gameserver.model.entity.olympiad;

import java.util.Set;

import l2r.gameserver.Config;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.base.TeamType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.Reflection;
import l2r.gameserver.model.entity.events.impl.DuelEvent;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import l2r.gameserver.network.serverpackets.ExOlympiadMode;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.InstantZone;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.FixEnchantOlympiad;
import l2r.gameserver.utils.Location;
import l2r.gameserver.utils.Log;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamMember
{
	private static final Logger _log = LoggerFactory.getLogger(TeamMember.class);

	private String _name = StringUtils.EMPTY;
	private String _clanName = StringUtils.EMPTY;
	private int _clanId;
	private int _classId;
	private double _damage;
	private boolean _isDead;

	private final int _objId;
	private final OlympiadGame _game;
	private final CompType _type;
	private final int _side;

	private Player _player;
	private Location _returnLoc = null;

	public boolean isDead()
	{
		return _isDead;
	}

	public void doDie()
	{
		_isDead = true;
	}

	public TeamMember(int obj_id, String name, Player player, OlympiadGame game, int side)
	{
		_objId = obj_id;
		_name = name;
		_game = game;
		_type = game.getType();
		_side = side;

		_player = player;
		if(_player == null)
			return;

		_clanName = player.getClan() == null ? StringUtils.EMPTY : player.getClan().getName();
		_clanId = player.getClan() == null ? 0 : player.getClan().getClanId();
		_classId = player.getActiveClassId();

		player.setOlympiadSide(side);
		player.setOlympiadGame(game);
	}

	public StatsSet getStat()
	{
		return Olympiad._nobles.get(_objId);
	}

	public void incGameCount()
	{
		StatsSet set = getStat();
		switch(_type)
		{
			case TEAM:
				set.set(Olympiad.GAME_TEAM_COUNT, set.getInteger(Olympiad.GAME_TEAM_COUNT) + 1);
				break;
			case CLASSED:
				set.set(Olympiad.GAME_CLASSES_COUNT, set.getInteger(Olympiad.GAME_CLASSES_COUNT) + 1);
				break;
			case NON_CLASSED:
				set.set(Olympiad.GAME_NOCLASSES_COUNT, set.getInteger(Olympiad.GAME_NOCLASSES_COUNT) + 1);
				break;
		}
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
		{
			StatsSet stat = getStat();
			int points = stat.getInteger(Olympiad.POINTS);
			int diff = Math.min(OlympiadGame.MAX_POINTS_LOOSE, points / _type.getLooseMult());
			stat.set(Olympiad.POINTS, points - diff);
			Log.add("Olympiad Result: " + _name + " lost " + diff + " points for crash", "olympiad");

			// TODO: Снести подробный лог после исправления беспричинного отъёма очков.
			Player player = _player;
			if(player == null)
				Log.add("Olympiad info: " + _name + " crashed coz player == null", "olympiad");
			else
			{
				if(player.isLogoutStarted())
					Log.add("Olympiad info: " + _name + " crashed coz player.isLogoutStarted()", "olympiad");
				if (!player.isOnline())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(!player.isConnected())
					Log.add("Olympiad info: " + _name + " crashed coz !player.isOnline()", "olympiad");
				if(player.getOlympiadGame() == null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadGame() == null", "olympiad");
				if(player.getOlympiadObserveGame() != null)
					Log.add("Olympiad info: " + _name + " crashed coz player.getOlympiadObserveGame() != null", "olympiad");
			}
		}
	}

	public boolean checkPlayer()
	{
		Player player = _player;
		if(player == null || player.isLogoutStarted() || !player.isOnline() || player.getOlympiadGame() == null || player.isInObserverMode())
			return false;
		GameClient client = player.getNetConnection();
		if(client == null)
			return false;
		if(!player.isConnected())
			return false;
		return true;
	}

	public void portPlayerToArena()
	{
		Player player = _player;
		if(!checkPlayer() || player.isTeleporting())
		{
			_player = null;
			return;
		}

		DuelEvent duel = player.getEvent(DuelEvent.class);
		if (duel != null)
			duel.abortDuel(player);

		_returnLoc = player._stablePoint == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player._stablePoint;

		if(player.isDead())
			player.setPendingRevive(true);
		if(player.isSitting())
			player.standUp();

		player.setTarget(null);
		player.setIsInOlympiadMode(true);

		player.leaveParty();

		Reflection ref = _game.getReflection();
		InstantZone instantZone = ref.getInstancedZone();

		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());

		player._stablePoint = _returnLoc;
		player.teleToLocation(tele, ref);

		if(_type == CompType.TEAM)
			player.setTeam(_side == 1 ? TeamType.BLUE : TeamType.RED);

		player.sendPacket(new ExOlympiadMode(_side));
	}

	public void portPlayerBack()
	{
		Player player = _player;
		if(player == null)
			return;

		if (_returnLoc == null) // игрока не портнуло на стадион
			return;

		Olympiad._playersIp.remove(player.getIP());
		player.setIsInOlympiadMode(false);
		player.setOlympiadSide(-1);
		player.setOlympiadGame(null);

		if(_type == CompType.TEAM)
			player.setTeam(TeamType.NONE);

		// Удаляем баффы и чужие кубики
		for(Effect e : player.getEffectList().getAllEffects())
			if(e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0)
				e.exit();

		if(player.getPet() != null)
			player.getPet().getEffectList().stopAllEffects();

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new Revive(player));
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);

		// Возвращаем клановые скиллы если репутация положительная.
		if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
			player.getClan().enableSkills(player);

		// Add Hero Skills
		if(player.isHero())
			Hero.addSkills(player);

		// Обновляем скилл лист, после добавления скилов
		player.sendPacket(new SkillList(player));
		player.sendPacket(new ExOlympiadMode(0));
		player.sendPacket(new ExOlympiadMatchEnd());

		player._stablePoint = null;
		player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);

		// Восстанавливаем точку итемов
		if(Config.OLY_ENCH_LIMIT_ENABLE && player.getVar("EnItemOlyRec") != null)
		{
			FixEnchantOlympiad.restoreEnchantItemsOly(player);
		}
	}

	public void preparePlayer()
	{
		Player player = _player;
		
		if(player == null)
			return;

		if(player.isInObserverMode())
			if(player.getOlympiadObserveGame() != null)
				player.leaveOlympiadObserverMode(true);
			else
				player.leaveObserverMode();

		// Un activate clan skills
		if(player.getClan() != null)
			player.getClan().disableSkills(player);

		// Remove Hero Skills
		if(player.isHero())
			Hero.removeSkills(player);

		// Abort casting if player casting
		if(player.isCastingNow())
			player.abortCast(true, true);

		// Удаляем баффы и чужие кубики
		for(Effect e : player.getEffectList().getAllEffects())
			if(e.getEffectType() != EffectType.Cubic || player.getSkillLevel(e.getSkill().getId()) <= 0)
				e.exit();

		// Remove Summon's Buffs
		if(player.getPet() != null)
		{
			Summon summon = player.getPet();
			if(summon.isPet())
				summon.unSummon();
			else
				summon.getEffectList().stopAllEffects();
		}

		// unsummon agathion
		if(player.getAgathionId() > 0)
			player.setAgathion(0);

		// Сброс кулдауна всех скилов, время отката которых меньше 15 минут
		for(TimeStamp sts : player.getSkillReuses())
		{
			if(sts == null)
				continue;
			Skill skill = SkillTable.getInstance().getInfo(sts.getId(), sts.getLevel());
			if(skill == null)
				continue;
			if(sts.getReuseBasic() <= 15 * 60000L)
				player.enableSkill(skill);
		}

		// Обновляем скилл лист, после удаления скилов
		player.sendPacket(new SkillList(player));
		// Обновляем куллдаун, после сброса
		player.sendPacket(new SkillCoolTime(player));

		// Remove Hero weapons
		ItemInstance wpn = player.getActiveWeaponInstance();
		if(wpn != null && wpn.isHeroWeapon())
		{
			player.getInventory().unEquipItem(wpn);
			player.abortAttack(true, true);
		}

		// remove bsps/sps/ss automation
		Set<Integer> activeSoulShots = player.getAutoSoulShot();
		for(int itemId : activeSoulShots)
		{
			player.removeAutoSoulShot(itemId);
			player.sendPacket(new ExAutoSoulShot(itemId, false));
		}

		// Разряжаем заряженные соул и спирит шоты
		ItemInstance weapon = player.getActiveWeaponInstance();
		if(weapon != null)
		{
			weapon.setChargedSpiritshot(ItemInstance.CHARGED_NONE);
			weapon.setChargedSoulshot(ItemInstance.CHARGED_NONE);
		}

		// Проверяем точку итемов
		if(Config.OLY_ENCH_LIMIT_ENABLE)
		{
			FixEnchantOlympiad.storeEnchantItemsOly(player);
		}

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void saveNobleData()
	{
		OlympiadDatabase.saveNobleData(_objId);
	}

	public void logout()
	{
		_player = null;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getName()
	{
		return _name;
	}

	public void addDamage(double d)
	{
		_damage += d;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getObjectId()
	{
		return _objId;
	}
}