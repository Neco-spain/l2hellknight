package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.ExLoginVitalityEffectInfo;

public class GotoLobby extends L2GameClientPacket {
    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
        ExLoginVitalityEffectInfo vl = new ExLoginVitalityEffectInfo(cl.getCharInfo());
        sendPacket(cl, vl);
    }
}