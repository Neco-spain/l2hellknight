package l2rt.gameserver.skills.skillclasses;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class Spoil extends L2Skill
{
	public Spoil(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(!activeChar.isPlayer())
			return;

		int ss = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : (activeChar.getChargedSoulShot() ? 2 : 0)) : 0;
		if(ss > 0 && getPower() > 0)
			activeChar.unChargeShots(false);

		for(L2Character target : targets)
			if(target != null && !target.isDead())
			{
				if(target.isMonster())
					if(((L2MonsterInstance) target).isSpoiled())
						activeChar.sendPacket(Msg.ALREADY_SPOILED);
					else
					{
						L2MonsterInstance monster = (L2MonsterInstance) target;
						boolean success;
						if(!Config.ALT_SPOIL_FORMULA)
						{
							int monsterLevel = monster.getLevel();
							int modifier = Math.abs(monsterLevel - activeChar.getLevel());
							double rateOfSpoil = Config.BASE_SPOIL_RATE;

							if(modifier > 8)
								rateOfSpoil = rateOfSpoil - rateOfSpoil * (modifier - 8) * 9 / 100;

							rateOfSpoil = rateOfSpoil * getMagicLevel() / monsterLevel;

							if(rateOfSpoil < Config.MINIMUM_SPOIL_RATE)
								rateOfSpoil = Config.MINIMUM_SPOIL_RATE;
							else if(rateOfSpoil > 99.)
								rateOfSpoil = 99.;

							activeChar.sendMessage(new CustomMessage("l2rt.gameserver.skills.skillclasses.Spoil.Chance", activeChar).addNumber((long) rateOfSpoil));
							success = Rnd.chance(rateOfSpoil);
						}
						else
							success = Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate());

						if(success)
						{
							monster.setSpoiled(true, (L2Player) activeChar);
							activeChar.sendPacket(Msg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
						}
						else
							activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addSkillName(_id, getDisplayLevel()));
					}

				if(getPower() > 0)
				{
					double damage = isMagic() ? Formulas.calcMagicDam(activeChar, target, this, ss) : Formulas.calcPhysDam(activeChar, target, this, false, false, ss > 0, false).damage;
					target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);
				}

				getEffects(activeChar, target, false, false);

				target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Math.max(_effectPoint, 1));
			}
	}
}