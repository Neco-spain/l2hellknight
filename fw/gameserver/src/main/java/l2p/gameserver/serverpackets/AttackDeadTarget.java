package l2p.gameserver.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        // just trigger - без аргументов
        writeC(0x04);
    }
}