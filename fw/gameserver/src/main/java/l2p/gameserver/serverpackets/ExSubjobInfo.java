package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.SubClass;

public class ExSubjobInfo extends L2GameServerPacket {
    private Player player;

    public ExSubjobInfo(Player player) {
        this.player = player;
    }

    protected final void writeImpl() {
        writeEx(0xE9);
        writeC(0);
        writeD(player.getClassId().getId());
        writeD(player.getRace().ordinal());
        writeD(player.getSubClasses().size());
        for (SubClass tmp : player.getSubClasses().values()) {
            writeD(player.getSubIndex());
            writeD(tmp.getClassId());
            writeD(tmp.getLevel());
            if (tmp.isBase()) {
                writeC(0); // 0 - main class, 1 - dual class, 2 - sub class
            } else {
                writeC(2);
            }
        }
    }
}
