package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExMentorList;

/**
 * Приходит при нажатии кнопки Friends в клиенте.
 * Не имеет структуры, ответом на этот запрос является пакет {@link l2p.gameserver.serverpackets.ExMentorList}
 */
public class RequestMentorList extends L2GameClientPacket {

    @Override
    protected void runImpl() {
        //triggger
    }

    @Override
    protected void readImpl() {
        Player activeChar = getClient().getActiveChar();
        sendPacket(new ExMentorList(activeChar));
    }
}