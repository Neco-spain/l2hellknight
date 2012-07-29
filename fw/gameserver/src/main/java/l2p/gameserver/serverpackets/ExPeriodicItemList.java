package l2p.gameserver.serverpackets;

public class ExPeriodicItemList extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x87);
        writeD(0); // count of dd
        {
            //writeD(0x00);//itemObjectId
            //writeD(0x00);//expireTime
        }
    }
}