package l2p.gameserver.serverpackets;

/**
 * @author : Ragnarok
 * @date : 10.02.12  0:45
 * <p/>
 * Ответ на {@link l2p.gameserver.clientpackets.RequestCommissionInfo}
 * Помещает предмет в окно регистрации.
 */
public class ExResponseCommissionInfo extends L2GameServerPacket {
    private int response;

    public ExResponseCommissionInfo(int response) {
        this.response = response;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xF3);
        writeD(response);// предположительно размер нижеследующего списка.
        if (response == 1) {
            writeD(0);// unknown, always 0, предположительно ObjectId продаваемого предмета
            writeQ(0);// цена продажи
            writeQ(0);// начальное кол-во
            writeD(-1);// кол-во дней, -1 = кол-во дней не изменяется, 0 = 1 day, 1 = 3 days, 2 = 5 days, 3 = 7 days
            writeD(0);// unknown, always 0
        }
    }
}
