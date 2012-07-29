package l2p.gameserver.serverpackets;

/**
 * Format: ch (trigger)
 */
public class ExShowAdventurerGuideBook extends L2GameServerPacket {
    @Override
    protected final void writeImpl() {
        writeEx(0x38);
    }
}