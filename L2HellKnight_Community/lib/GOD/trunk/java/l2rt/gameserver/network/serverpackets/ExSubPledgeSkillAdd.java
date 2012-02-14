package l2rt.gameserver.network.serverpackets;

public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _skillId;
	private final int _skillLevel;

	public ExSubPledgeSkillAdd(int type, int skillId, int skillLevel)
	{
		_type = type;
		_skillId = skillId;
		_skillLevel = skillLevel;
	}

	@Override
	public void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x76);
		writeD(_type);
		writeD(_skillId);
		writeD(_skillLevel);
	}

	@Override
	public String getType()
	{
		return "[S] FE:76 ExSubPledgeSkillAdd".intern();
	}

}