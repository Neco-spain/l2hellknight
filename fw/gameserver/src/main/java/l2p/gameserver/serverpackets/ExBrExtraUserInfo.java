package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExBrExtraUserInfo extends L2GameServerPacket {
    private int _charId;

    public ExBrExtraUserInfo(Player cha) {
        _charId = cha.getObjectId();
    }

    @Override
    protected void writeImpl() {
        writeEx(0xCF);
        writeD(_charId);
        writeD(0);
        //writeC(0); // Event Flag
    }
}