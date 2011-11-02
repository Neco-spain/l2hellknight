package com.l2js.gameserver.network.serverpackets;

public class ExTacticalSign extends L2GameServerPacket
{
    private static final String _S__F_F_EXACQUIRABLESKILLLISTBUCLASS = "[S] FE:FF ExAcquirableSkillListByClass";

    private int targetId;
    private int signId;

    public ExTacticalSign(int target, int sign)
    {
        this.targetId = target;
        this.signId = sign;
    }

    @Override
    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0xff);
        writeD(targetId);
        writeD(signId);/*id 1-4 Привет айон*/
    }

    public String getType()
	{
		return _S__F_F_EXACQUIRABLESKILLLISTBUCLASS;
	}
}
