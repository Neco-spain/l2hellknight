package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.Ex2ndPasswordCheck;

/**
 * Format: (ch)
 */
public class RequestEx2ndPasswordCheck extends L2GameClientPacket {
    @Override
    protected void readImpl() {

    }

    @Override
    protected void runImpl() {

        if (!Config.SECOND_AUTH_ENABLED || getClient().getSecondaryAuth().isAuthed())
        {
            sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_OK, 0));
            return;
        }

        getClient().getSecondaryAuth().openDialog();
    }
}
