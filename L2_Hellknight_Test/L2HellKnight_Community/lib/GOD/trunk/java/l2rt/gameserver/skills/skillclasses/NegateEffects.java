package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class NegateEffects extends L2Skill
{
	private Object[][] _negateEffects = null; //<EffectType, Integer>
	private Object[][] _negateStackType = null; //<String, Integer>
	private final boolean _onlyPhysical;
	private final boolean _negateDebuffs;

	public NegateEffects(StatsSet set)
	{
		super(set);

		String[] negateEffectsString = set.getString("negateEffects", "").split(";");
		for(int i = 0; i < negateEffectsString.length; i++)
			if(!negateEffectsString[i].isEmpty())
			{
				String[] entry = negateEffectsString[i].split(":");
				_negateEffects = (Object[][]) Util.addElementToArray(_negateEffects, new Object[] {
						Enum.valueOf(EffectType.class, entry[0]), entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE }, Object[].class);
			}

		String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
		for(int i = 0; i < negateStackTypeString.length; i++)
			if(!negateStackTypeString[i].isEmpty())
			{
				String[] entry = negateStackTypeString[i].split(":");
				_negateStackType = (Object[][]) Util.addElementToArray(_negateStackType, new Object[] { entry[0],
						entry.length > 1 ? Integer.decode(entry[1]) : Integer.MAX_VALUE }, Object[].class);
			}

		_onlyPhysical = set.getBool("onlyPhysical", false);
		_negateDebuffs = set.getBool("negateDebuffs", true);

		if(_negateEffects == null && _negateStackType == null)
			System.out.println("Invalid skill " + getId() + "." + getLevel() + ": NegateEffects must have negateEffects or negateStackType.");
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}

				if(_negateEffects != null)
					for(Object[] stat : _negateEffects)
						negateEffectAtPower(target, (EffectType) stat[0], (Integer) stat[1]);

				if(_negateStackType != null)
					for(Object[] stat : _negateStackType)
						negateEffectAtPower(target, (String) stat[0], (Integer) stat[1]);

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private void negateEffectAtPower(L2Character target, EffectType type, Integer power)
	{
		for(L2Effect e : target.getEffectList().getAllEffects())
		{
			L2Skill skill = e.getSkill();
			if(_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs)
				continue;
			// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
			if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
				continue;
			if(e.getEffectType() == type && e.getStackOrder() <= power)
				e.exit();
		}
	}

	private void negateEffectAtPower(L2Character target, String stackType, Integer power)
	{
		for(L2Effect e : target.getEffectList().getAllEffects())
		{
			L2Skill skill = e.getSkill();
			if(_onlyPhysical && skill.isMagic() || !skill.isCancelable() || skill.isOffensive() && !_negateDebuffs)
				continue;
			// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
			if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
				continue;
			if(e.checkStackType(stackType) && e.getStackOrder() <= power)
				e.exit();
		}
	}
}