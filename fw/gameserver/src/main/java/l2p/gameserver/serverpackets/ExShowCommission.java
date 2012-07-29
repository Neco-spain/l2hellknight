package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 09.02.12  12:19
 *
 * После получения этого пакета клиентом, открывается коммиссионный магазин.
 */
public class ExShowCommission extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xF1);
        writeD(1);// unk
    }
}