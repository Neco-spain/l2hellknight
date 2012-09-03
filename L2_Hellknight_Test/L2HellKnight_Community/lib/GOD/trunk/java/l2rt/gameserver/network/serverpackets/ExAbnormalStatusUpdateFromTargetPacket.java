package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.util.EffectsComparator;
import l2rt.util.GArray;

import java.util.Arrays;

public class ExAbnormalStatusUpdateFromTargetPacket extends L2GameServerPacket
{
    private static final String _S__E5_EXABNORMALSTATUSUPDATEFROMTARGETPACKET = "[S] E5 ExAbnormalStatusUpdateFromTargetPacket";

	private GArray<Effect> _effects;
	private int char_obj_id = 0;
	
    public ExAbnormalStatusUpdateFromTargetPacket(L2Character target)
    {
		char_obj_id = target.getObjectId();
		
		_effects = new GArray<Effect>();		
		
		L2Effect[] effects = target.getEffectList().getAllFirstEffects();
		
		Arrays.sort(effects, EffectsComparator.getInstance());		
		
		for(L2Effect effect : effects)
			if(effect != null && effect.isInUse())
				effect.addSpelledIcon(this);
    }

    @Override
    protected final void writeImpl()
    {
		writeC(EXTENDED_PACKET);
        writeH(0xe5);
        writeD(char_obj_id);
		writeH(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(0);  
			writeD(temp._duration);
			writeD(0); 
        }
    }

	public void addSpelledEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	class Effect
	{
		final int _skillId;
		final int _level;
		final int _duration;

		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
	
    @Override
    public String getType()
    {
        return _S__E5_EXABNORMALSTATUSUPDATEFROMTARGETPACKET;
    }
}