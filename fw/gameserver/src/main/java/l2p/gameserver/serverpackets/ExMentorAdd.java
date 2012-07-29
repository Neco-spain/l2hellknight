package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

/**
 * @author Cain
 */
public class ExMentorAdd extends L2GameServerPacket {
    private String _newMentorName;
    private int _newMentorClassId, _newMentorLvl;

    public ExMentorAdd(Player newMentor) {
        _newMentorName = newMentor.getName();
        _newMentorClassId = newMentor.getClassId().getId();
        _newMentorLvl = newMentor.getLevel();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x121);
        writeS(_newMentorName);
        writeD(_newMentorClassId);
        writeD(_newMentorLvl);
    }
}