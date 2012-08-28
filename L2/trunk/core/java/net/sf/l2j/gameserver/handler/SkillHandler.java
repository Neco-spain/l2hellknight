//L2DDT
package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.skillhandlers.*;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

public class SkillHandler
{
	private static Logger _log = Logger.getLogger(SkillHandler.class.getName());
	private static SkillHandler _instance;

	private Map<L2Skill.SkillType, ISkillHandler> _datatable;

	public static SkillHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillHandler();
		}
		return _instance;
	}

	private SkillHandler()
	{
		_datatable = new TreeMap<SkillType, ISkillHandler>();
		registerSkillHandler(new Blow());
		registerSkillHandler(new Pdam());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new Heal());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new Charge());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new Resurrect());
		registerSkillHandler(new Spoil());
		registerSkillHandler(new Sweep());
		registerSkillHandler(new StrSiegeAssault());
		registerSkillHandler(new SummonFriend());
		registerSkillHandler(new SummonTreasureKey());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new Recall());
		registerSkillHandler(new SiegeFlag());
		registerSkillHandler(new TakeCastle());
		registerSkillHandler(new Unlock());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Craft());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Sow());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new GetPlayer());
		_log.config("SkillHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerSkillHandler(ISkillHandler handler)
	{
		SkillType[] types = handler.getSkillIds();
		for (SkillType t : types)
		{
			_datatable.put(t, handler);
		}
	}

	public ISkillHandler getSkillHandler(SkillType skillType)
	{
		return _datatable.get(skillType);
	}

    public int size()
    {
        return _datatable.size();
    }
}
