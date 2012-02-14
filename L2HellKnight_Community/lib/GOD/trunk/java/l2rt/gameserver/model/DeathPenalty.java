package l2rt.gameserver.model;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.serverpackets.EtcStatusUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.util.Rnd;

public class DeathPenalty
{
	// Дыхание Шилен
	private static final int _skillId = 14571;
	private static final int _fortuneOfNobleseSkillId = 1325;
	private static final int _charmOfLuckSkillId = 2168;

	private long playerStoreId = 0;
	private byte _level;
	private boolean _hasCharmOfLuck;

	public DeathPenalty(L2Player player, byte level)
	{
		playerStoreId = player.getStoredId();
		_level = player.isGM() ? 0 : level;
	}

	public L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(playerStoreId);
	}

	/*
	 * For common usage
	 */
	public int getLevel()
	{
		/*// Some checks if admin set incorrect value at database
		if(_level > 5)
			_level = 5;

		if(_level < 0)
			_level = 0;

		return ConfigSystem.getBoolean("EnableDeathPenaltyC5") ? _level : 0;*/
		return 0;
	}

	/*
	 * Used only when saving DB if admin for some reasons disabled it in config after it was enabled.
	 * In if we will use getLevel() it will be reseted to 0
	 */
	public int getLevelOnSaveDB()
	{
		if(_level > 5)
			_level = 5;

		if(_level < 0)
			_level = 0;

		return _level;
	}

	public void notifyDead(L2Character killer)
	{
		/*if(!ConfigSystem.getBoolean("EnableDeathPenaltyC5"))
			return;

		if(_hasCharmOfLuck)
		{
			_hasCharmOfLuck = false;
			return;
		}

		L2Player player = getPlayer();
		if(player == null || player.getLevel() <= 9)
			return;

		int karmaBonus = player.getKarma() / ConfigSystem.getInt("DeathPenaltyC5RateKarma");
		if(karmaBonus < 0)
			karmaBonus = 0;

		if(Rnd.chance(ConfigSystem.getInt("DeathPenaltyC5Chance") + karmaBonus) && !killer.isPlayable())
			addLevel();*/
	}

	public void addLevel()
	{
		/*L2Player player = getPlayer();
		if(player == null || getLevel() >= 5 || player.isGM())
			return;

		if(getLevel() != 0)
		{
			L2Skill remove = getCurrentSkill();
			if(remove != null)
				player.removeSkill(remove, true);
		}

		_level++;

		L2Skill newSkill = SkillTable.getInstance().getInfo(_skillId, getLevel());
		for (EffectTemplate et : newSkill.getEffectTemplates())
		{
			Env env = new Env(player, player, newSkill);
			L2Effect effect = et.getEffect(env);
			player.getEffectList().addEffect(effect);
		}
		
		player.sendPacket(new EtcStatusUpdate(player), new SystemMessage(SystemMessage.THE_LEVEL_S1_DEATH_PENALTY_WILL_BE_ASSESSED).addNumber(getLevel()));
		player.broadcastUserInfo(true);*/
	}

	public void reduceLevel()
	{
		/*L2Player player = getPlayer();
		if(player == null || getLevel() <= 0)
			return;

		L2Skill remove = getCurrentSkill();
		if(remove != null)
			player.removeSkill(remove, true);

		_level--;

		if(getLevel() > 0)
		{
			
			L2Skill newSkill = SkillTable.getInstance().getInfo(_skillId, getLevel());
			for (EffectTemplate et : newSkill.getEffectTemplates())
			{
				Env env = new Env(player, player, newSkill);
				L2Effect effect = et.getEffect(env);
				player.getEffectList().addEffect(effect);
			}
		
			player.sendPacket(new EtcStatusUpdate(player), new SystemMessage(SystemMessage.THE_LEVEL_S1_DEATH_PENALTY_WILL_BE_ASSESSED).addNumber(getLevel()));
			player.broadcastUserInfo(true);
		}
		else
		{
			player.sendPacket(new EtcStatusUpdate(player), Msg.THE_DEATH_PENALTY_HAS_BEEN_LIFTED);
			player.broadcastUserInfo(true);
		}*/
	}

	public L2Skill getCurrentSkill()
	{
		/*L2Player player = getPlayer();
		if(player != null)
			for(L2Skill s : player.getAllSkills())
				if(s.getId() == _skillId)
					return s;*/
		return null;
	}

	public void checkCharmOfLuck()
	{
		/*L2Player player = getPlayer();
		if(player != null)
			for(L2Effect e : player.getEffectList().getAllEffects())
				if(e.getSkill().getId() == _charmOfLuckSkillId || e.getSkill().getId() == _fortuneOfNobleseSkillId)
				{
					_hasCharmOfLuck = true;
					return;
				}

		_hasCharmOfLuck = false;*/
	}
}