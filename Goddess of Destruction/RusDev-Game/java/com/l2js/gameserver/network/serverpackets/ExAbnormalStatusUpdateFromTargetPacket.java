package com.l2js.gameserver.network.serverpackets;

import com.l2js.gameserver.model.L2Object;

import javolution.util.FastList;

import java.util.List;

public class ExAbnormalStatusUpdateFromTargetPacket extends L2GameServerPacket
{
    private List<Effect> _effects;
    private int _objectId;

    private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;

		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}

    public ExAbnormalStatusUpdateFromTargetPacket(L2Object object)
	{
		_effects = new FastList<Effect>();
        _objectId = object.getObjectId();
	}

    public ExAbnormalStatusUpdateFromTargetPacket(int objectId)
	{
		_effects = new FastList<Effect>();
        _objectId = objectId;
	}

    public void addEffect(int skillId, int level, int duration)
	{
		if (skillId == 2031 ||skillId == 2032 ||skillId == 2037 || skillId == 26025 || skillId == 26026)
			return;
		_effects.add(new Effect(skillId, level, duration));
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xE5);
        writeD(_objectId);
        writeH(_effects.size());

        for (Effect temp : _effects)
        {
            writeD(temp._skillId);
            writeH(temp._level);
            writeD(0); // ??
            if (temp._duration == -1)
				writeD(-1);
			else
				writeD(temp._duration / 1000);
            writeD(0); // ??
        }
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:E5 ExAbnormalStatusUpdateFromTargetPacket";
	}
}