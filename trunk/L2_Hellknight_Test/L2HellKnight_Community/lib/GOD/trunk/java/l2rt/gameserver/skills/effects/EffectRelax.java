package l2rt.gameserver.skills.effects;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class EffectRelax extends L2Effect
{
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(!_effected.isPlayer())
			return false;
		L2Player player = (L2Player) _effected;
		if(player.isMounted())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		L2Player player = (L2Player) _effected;
		player.setRelax(true);
		player.sitDown();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		L2Player player = (L2Player) _effected;
		player.setRelax(false);
	}

	@Override
	public boolean onActionTime()
	{
		L2Player player = (L2Player) _effected;

		if(player.isDead() || !player.isSitting())
		{
			player.setRelax(false);
			return false;
		}

		if(player.isCurrentHpFull() && getSkill().isToggle())
		{
			player.sendPacket(Msg.HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED);
			player.setRelax(false);
			return false;
		}

		double manaDam = calc();
		if(getSkill().isMagic())
			manaDam = player.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, manaDam, null, getSkill());
		else
			manaDam = player.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, manaDam, null, getSkill());

		if(manaDam > player.getCurrentMp() && getSkill().isToggle())
		{
			player.sendPacket(Msg.NOT_ENOUGH_MP, new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			player.setRelax(false);
			return false;
		}

		player.reduceCurrentMp(manaDam, null);
		return true;
	}
}