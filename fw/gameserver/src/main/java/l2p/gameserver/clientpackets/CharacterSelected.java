package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.network.GameClient.GameClientState;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.CharSelected;
import l2p.gameserver.utils.AutoBan;

public class CharacterSelected extends L2GameClientPacket {
    private int _charSlot;

    /**
     * Format: cdhddd
     */
    @Override
    protected void readImpl() {
        _charSlot = readD();
    }

    @Override
    protected void runImpl() {
        GameClient client = getClient();

        if (Config.SECOND_AUTH_ENABLED && !client.getSecondaryAuth().isAuthed())
        {
            client.getSecondaryAuth().openDialog();
            return;
        }

        if (client.getActiveChar() != null)
            return;

        int objId = client.getObjectIdForSlot(_charSlot);
        if (AutoBan.isBanned(objId)) {
            sendPacket(ActionFail.STATIC);
            return;
        }

        Player activeChar = client.loadCharFromDisk(_charSlot);
        if (activeChar == null) {
            sendPacket(ActionFail.STATIC);
            return;
        }

        if (activeChar.getAccessLevel() < 0)
            activeChar.setAccessLevel(0);

        client.setState(GameClientState.IN_GAME);

        sendPacket(new CharSelected(activeChar, client.getSessionKey().playOkID1));
    }
}