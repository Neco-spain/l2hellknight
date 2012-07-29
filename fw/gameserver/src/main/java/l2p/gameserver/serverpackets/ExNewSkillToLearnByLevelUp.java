package l2p.gameserver.serverpackets;

// Показывает иконку для перехода в панель изучения скила
public class ExNewSkillToLearnByLevelUp extends L2GameServerPacket {
    @Override
    protected void writeImpl() {
        writeEx(0xFC);
    }
}
