package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Cancel extends L2Skill
{
	private final String _dispelType;
	private final int _cancelRate;
	private final int _negateCount;

	public Cancel(StatsSet set)
	{
		super(set);
		_dispelType = set.getString("dispelType", "");
		_cancelRate = set.getInteger("cancelRate", 0);
		_negateCount = set.getInteger("negateCount", 5);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				if(_cancelRate <= 0 || Formulas.calcSkillSuccess(activeChar, target, this, _cancelRate))
				{
					byte counter = 0;

					if(_dispelType.equals(""))
					{
						byte antiloop = 24;
						while(counter < _negateCount && antiloop > 0)
						{
							ConcurrentLinkedQueue<L2Effect> eff = target.getEffectList().getAllEffects();
							if(eff.size() == 0)
								break;
							L2Effect e = eff.toArray(new L2Effect[eff.size()])[Rnd.get(eff.size())];
							L2Skill skill = e.getSkill();
							// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
							if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
							{
								counter++;
								antiloop--;
								continue;
							}
							if(skill.isCancelable())
							{
								e.exit();
								counter++;
							}
							antiloop--;
						}
					}
					else
					{
						counter = 0;
						if(_dispelType.contains("negative"))
							for(L2Effect e : target.getEffectList().getAllEffects())
								if(counter < _negateCount && e.getSkill().isOffensive() && e.getSkill().isCancelable())
								{
									e.exit();
									counter++;
								}

						counter = 0;
						if(_dispelType.contains("positive"))
							for(L2Effect e : target.getEffectList().getAllEffects())
							{
								L2Skill skill = e.getSkill();
								if(counter < _negateCount && !skill.isOffensive() && skill.isCancelable())
								{
									// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
									if(skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
									{
										counter++;
										continue;
									}

									e.exit();
									counter++;
								}
							}
					}
				}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return !_dispelType.contains("negative");
	}
}