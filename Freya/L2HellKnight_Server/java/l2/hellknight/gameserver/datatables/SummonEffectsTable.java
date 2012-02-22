package l2.hellknight.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;

import java.util.List;

import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class SummonEffectsTable
{
	private TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>> _servitorEffects = new TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>>();

	public TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>> getServitorEffectsOwner()
	{
		return _servitorEffects;
	}
	
	public TIntObjectHashMap<List<SummonEffect>> getServitorEffects(L2PcInstance owner)
	{
		final TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	
	/** Pets **/
	private TIntObjectHashMap<List<SummonEffect>> _petEffects = new TIntObjectHashMap<List<SummonEffect>>(); // key: petItemObjectId, value: Effects list
	
	public TIntObjectHashMap<List<SummonEffect>> getPetEffects()
	{
		return _petEffects;
	}
	
	
	/** Common **/
	public static SummonEffectsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public class SummonEffect
	{
		L2Skill _skill;
		int _effectCount;
		int _effectCurTime;
		
		public SummonEffect(L2Skill skill, int effectCount, int effectCurTime)
		{
			_skill = skill;
			_effectCount = effectCount;
			_effectCurTime = effectCurTime;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		public int getEffectCount()
		{
			return _effectCount;
		}
		
		public int getEffectCurTime()
		{
			return _effectCurTime;
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SummonEffectsTable _instance = new SummonEffectsTable();
	}
}