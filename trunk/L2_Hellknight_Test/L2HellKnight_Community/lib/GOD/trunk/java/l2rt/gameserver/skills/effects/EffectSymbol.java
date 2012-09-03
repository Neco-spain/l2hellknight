package l2rt.gameserver.skills.effects;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillLaunched;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.util.logging.Logger;

public final class EffectSymbol extends L2Effect
{
	private static final Logger log = Logger.getLogger(EffectSymbol.class.getName());

	L2RoundTerritoryWithSkill _territory;
	L2NpcInstance _symbol;

	public EffectSymbol(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getSkill().getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
		{
			log.severe("Symbol skill with target != self, id = " + getSkill().getId());
			return false;
		}

		L2Skill skill = getSkill().getFirstAddedSkill();
		if(skill == null)
		{
			log.severe("Not implemented symbol skill, id = " + getSkill().getId());
			return false;
		}

		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		L2Skill skill = getSkill().getFirstAddedSkill();

		// Затычка, в клиенте они почему-то не совпадают.
		skill.setIsMagic(getSkill().isMagic());

		Location loc = _effected.getLoc();
		if(_effected.isPlayer() && ((L2Player) _effected).getGroundSkillLoc() != null)
		{
			loc = ((L2Player) _effected).getGroundSkillLoc();
			((L2Player) _effected).setGroundSkillLoc(null);
		}

		_territory = new L2RoundTerritoryWithSkill(_effected.getObjectId(), loc.x, loc.y, _skill.getSkillRadius(), loc.z - 200, loc.z + 200, _effector, skill);
		L2World.addTerritory(_territory);

		L2NpcTemplate template = NpcTable.getTemplate(_skill.getSymbolId());

		try
		{
			L2Spawn spawn = new L2Spawn(template);
			spawn.setLoc(loc);
			spawn.setReflection(_effected.getReflection().getId());
			spawn.setAmount(1);
			spawn.init();
			spawn.stopRespawn();
			_symbol = spawn.getLastSpawn();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		for(L2Character cha : L2World.getAroundCharacters(_symbol, _skill.getSkillRadius() + 200, 400))
			cha.updateTerritories();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		L2World.removeTerritory(_territory);
		if(_symbol == null)
			return;
		for(L2Character cha : L2World.getAroundCharacters(_symbol, _skill.getSkillRadius() + 200, 400))
			cha.updateTerritories();
		_symbol.deleteMe();
		_symbol = null;
	}

	@Override
	public boolean onActionTime()
	{
		if(_template._counter <= 1)
			return false;

		L2Character effector = getEffector();
		L2Skill skill = getSkill().getFirstAddedSkill();
		L2NpcInstance symbol = _symbol;
		double mpConsume = getSkill().getMpConsume();

		if(effector == null || skill == null || symbol == null)
			return false;

		if(mpConsume > effector.getCurrentMp())
		{
			effector.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		effector.reduceCurrentMp(mpConsume, effector);

		// Использовать разрешено только скиллы типа TARGET_ONE
		for(L2Character cha : L2World.getAroundCharacters(symbol, getSkill().getSkillRadius(), 200))
			if(cha.getEffectList().getEffectsBySkill(skill) == null && skill.checkTarget(effector, cha, cha, false, false) == null)
			{
				if(skill.isOffensive() && !GeoEngine.canSeeTarget(symbol, cha, false))
					continue;
				GArray<L2Character> targets = new GArray<L2Character>(1);
				targets.add(cha);
				effector.callSkill(skill, targets, false);
				effector.broadcastPacket(new MagicSkillLaunched(_symbol.getObjectId(), getSkill().getDisplayId(), getSkill().getDisplayLevel(), cha, true));
				cha.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, effector, 1);
			}

		return true;
	}
}