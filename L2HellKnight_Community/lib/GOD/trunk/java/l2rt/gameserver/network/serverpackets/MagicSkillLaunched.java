package l2rt.gameserver.network.serverpackets;

import java.util.Collection;

import l2rt.gameserver.model.L2Character;
import l2rt.util.GArray;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _casterId;
	private final int _skillId;
	private final int _skillLevel;
	private final Collection<L2Character> _targets;
	private final boolean _isOffensive;

	public boolean isOffensive()
	{
		return _isOffensive;
	}

	public MagicSkillLaunched(int casterId, int skillId, int skillLevel, L2Character target, boolean isOffensive)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = new GArray<L2Character>();
		_targets.add(target);
		_isOffensive = isOffensive;
	}

	public MagicSkillLaunched(int casterId, int skillId, int skillLevel, Collection<L2Character> targets, boolean isOffensive)
	{
		_casterId = casterId;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
		_isOffensive = isOffensive;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeD(_casterId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(0x00);  //? GOD
		writeD(_targets.size());
		for(L2Character target : _targets)
		{
			if(target != null)
			{
				writeD(target.getObjectId());
			}
		}
	}
}