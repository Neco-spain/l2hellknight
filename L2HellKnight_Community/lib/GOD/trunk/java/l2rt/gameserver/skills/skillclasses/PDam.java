package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.FinishRotating;
import l2rt.gameserver.network.serverpackets.StartRotating;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.skills.Formulas.AttackInfo;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class PDam extends L2Skill
{
	private final boolean _onCrit;
	private final boolean _directHp;
	private final boolean _turner;
	private final boolean _blow;

	public PDam(StatsSet set)
	{
		super(set);
		_onCrit = set.getBool("onCrit", false);
		_directHp = set.getBool("directHp", false);
		_turner = set.getBool("turner", false);
		_blow = set.getBool("blow", false);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();

		for(L2Character target : targets)
			if(target != null && !target.isDead())
			{
				if(_turner && !target.isInvul())
				{
					target.broadcastPacket(new StartRotating(target, target.getHeading(), 1, 65535));
					target.broadcastPacket(new FinishRotating(target, activeChar.getHeading(), 65535));
					target.setHeading(activeChar.getHeading());
					target.sendPacket(new SystemMessage(SystemMessage.S1_S2S_EFFECT_CAN_BE_FELT).addSkillName(_displayId, _displayLevel));
				}

				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				AttackInfo info = Formulas.calcPhysDam(activeChar, target, this, false, _blow, ss, _onCrit);

				if(!info.miss || info.damage >= 1)
					target.reduceCurrentHp(info.damage, activeChar, this, true, true, info.lethal ? false : _directHp, true);

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.onDecay();
		}
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}