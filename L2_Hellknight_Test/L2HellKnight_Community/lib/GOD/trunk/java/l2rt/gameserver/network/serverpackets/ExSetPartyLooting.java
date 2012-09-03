package l2rt.gameserver.network.serverpackets;

/**
 * @author : Ragnarok
 * @date : 19.12.10    13:37
 */
public class ExSetPartyLooting extends L2GameServerPacket {
    int lootType;
    int result;

    public ExSetPartyLooting(int lootType, int result) {
        this.lootType = lootType;
        this.result = result;
    }

    @Override
    protected void writeImpl() {
		writeC(EXTENDED_PACKET);
        writeH(0xBF);
        writeD(result);// 01 - посылает желтую мессагу о смене лута, 0 - просто меняет значение лута
        writeD(lootType);//тип 0-нашедшему, 1-случайно, 2-случайно+присвоить, 3-по очереди, 4-по очереди+присвоить
    }
}
