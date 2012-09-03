package l2rt.gameserver.model.base;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.FuncAdd;
import l2rt.gameserver.tables.AugmentationData;
import l2rt.gameserver.tables.AugmentationData.AugStat;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;

public final class L2Augmentation
{
	private int _effectsId = 0;
	private AugmentationStatBoni _boni = null;
	private L2Skill _skill = null;
	private boolean _isLoaded = false;

	public boolean isLoaded()
	{
		return _isLoaded;
	}

	public L2Augmentation(int effects, L2Skill skill)
	{
		_effectsId = effects;
		_boni = new AugmentationStatBoni(_effectsId);
		_skill = skill;
	}

	public L2Augmentation(int effects, int skill, int skillLevel)
	{
		this(effects, SkillTable.getInstance().getInfo(skill, skillLevel));
	}

	public class AugmentationStatBoni
	{
		private Stats _stats[];
		private float _values[];
		private boolean _active;

		public AugmentationStatBoni(int augmentationId)
		{
			_active = false;
			GArray<AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);
			if(as == null)
				return;

			_stats = new Stats[as.size()];
			_values = new float[as.size()];

			int i = 0;
			for(AugStat aStat : as)
			{
				_stats[i] = aStat.getStat();
				_values[i] = aStat.getValue();
				i++;
			}

			_isLoaded = true;
		}

		public void applyBoni(L2Player player)
		{
			// make sure the boni are not applyed twice..
			if(_active)
				return;

			for(int i = 0; i < _stats.length; i++)
				player.addStatFunc(new FuncAdd(_stats[i], 0x60, this, _values[i]));

			_active = true;
		}

		public void removeBoni(L2Player player)
		{
			// make sure the boni is not removed twice
			if(!_active)
				return;

			player.removeStatsOwner(this);

			_active = false;
		}
	}

	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return _effectsId;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	/**
	 * Applys the boni to the player.
	 * @param player
	 */
	public void applyBoni(L2Player player)
	{
		// При несоотвествии грейда аугмент не применяется 
		if (player.getWeaponsExpertisePenalty() > 0 || player.getArmorExpertisePenalty() > 0)
			return;

		_boni.applyBoni(player);

		// add the skill if any
		if(_skill != null)
		{
			player.addSkill(_skill);
			player.sendPacket(new SkillList(player));
		}
	}

	/**
	 * Removes the augmentation boni from the player.
	 * @param player
	 */
	public void removeBoni(L2Player player)
	{
		_boni.removeBoni(player);

		// remove the skill if any
		if(_skill != null)
		{
			player.removeSkill(_skill);
			player.sendPacket(new SkillList(player));
		}
	}

	@Override
	public String toString()
	{
		return "Augmentation";
	}
}