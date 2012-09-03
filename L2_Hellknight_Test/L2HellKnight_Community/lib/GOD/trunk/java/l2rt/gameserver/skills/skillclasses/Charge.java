package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class Charge extends L2Skill
{
	public static final int MAX_CHARGE = 8;

	private int _charges;
	private boolean _fullCharge;

	public Charge(StatsSet set)
	{
		super(set);
		_charges = set.getInteger("charges", getLevel());
		_fullCharge = set.getBool("fullCharge", false);
	}

	@Override
	public boolean checkCondition(final L2Character activeChar, final L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		L2Player player = (L2Player) activeChar;

		//Камушки можно юзать даже если заряд > 7, остальное только если заряд < уровень скила
		if(getPower() <= 0 && getId() != 2165 && player.getIncreasedForce() >= _charges)
		{
			activeChar.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return false;
		}
		else if(getId() == 2165)
			player.sendPacket(new MagicSkillUse(player, player, 2165, 1, 0, 0));

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(!activeChar.isPlayer())
			return;

		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss && getTargetType() != SkillTargetType.TARGET_SELF)
			activeChar.unChargeShots(false);

		for(L2Character target : targets)
		{
			if(target.isDead() || target == activeChar)
				continue;

			if(target.checkReflectSkill(activeChar, this))
				target = activeChar;

			if(getPower() > 0) // Если == 0 значит скилл "отключен"
			{
				double damage = Formulas.calcPhysDam(activeChar, target, this, false, false, ss, false).damage;
				target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);
			}

			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		chargePlayer((L2Player) activeChar, getId());
	}

	public void chargePlayer(L2Player player, Integer skillId)
	{
		if(player.getIncreasedForce() >= _charges)
		{
			player.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_);
			return;
		}
		if(_fullCharge)
			player.setIncreasedForce(_charges);
		else
			player.setIncreasedForce(player.getIncreasedForce() + 1);
	}
}