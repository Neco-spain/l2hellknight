package com.l2js.gameserver.network.serverpackets;

public class ExNewSkillToLearnByLevelUp extends L2GameServerPacket
{
	public ExNewSkillToLearnByLevelUp()
	{
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0xfe);
        writeH(0xfc);
	}
	
	@Override
	public final String getType()
	{
		return "[S] FE:FC ExNewSkillToLearnByLevelUp";
	}
}
