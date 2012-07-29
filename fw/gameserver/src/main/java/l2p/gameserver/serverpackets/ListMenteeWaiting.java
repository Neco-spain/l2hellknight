package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 15.03.12  14:08
 */
public class ListMenteeWaiting extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0x122);
    }
}
