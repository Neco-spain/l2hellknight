package com.l2js.gameserver.network.serverpackets;


import com.l2js.gameserver.model.actor.instance.L2PcInstance;

public class ExFlyMove extends L2GameServerPacket
{
    private static final String _S__FE_E7_EXFLYMOVE = "[S] FE:E7 ExFlyMove";

    private int _objectId;
    private int _type;
    private L2PcInstance _activeChar;

    public ExFlyMove(L2PcInstance cha, int type)
    {

        _activeChar = cha;
        _objectId = cha.getObjectId();
        _type = type;
    }

    @Override
    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0xe7);
        System.out.println("_objectId = " + _objectId);
        System.out.println("_activeChar = " + _activeChar.getObjectId());
        writeD(_objectId);
        //==
        if (_type == 1) //прыжек без стрелки
        {
            writeD(_activeChar.getX());
            writeD(_activeChar.getY());
            writeD(_activeChar.getZ());
        }
        else
        {
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
        }

        writeD(0x01);
        writeD(0x00);   //???
        writeD(0x00);   //???
        // возможно 3й тип прыжка подразумевает получение координаты клика мышкой и прыжок в ту сторону.
        writeD(_activeChar.getTarget().getX());
        writeD(_activeChar.getTarget().getY());
        writeD(_activeChar.getTarget().getZ());
    }

    public String getType()
	{
		return _S__FE_E7_EXFLYMOVE;
	}
}
