package l2p.gameserver.clientpackets;

/**
 * @author : Ragnarok
 * @date : 15.03.12  14:15
 *
 * Приходит при нажатии наставником кнопки "+" в окне учеников
 * Ответом на пакет является {@link l2p.gameserver.serverpackets.ListMenteeWaiting}
 */
public class RequestMenteeWaitingList extends L2GameClientPacket {
    @Override
    protected void readImpl() throws Exception {
        readD();// unk, always 1
        readD();// min level?
        readD();// max level
    }

    @Override
    protected void runImpl() throws Exception {
    }
}
