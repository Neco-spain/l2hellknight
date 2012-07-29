package l2p.gameserver.serverpackets;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 25.05.12
 * Time: 21:05
 * запрос выбранному чару на вступление в пати
 */
public class ExRegistWaitingSubstituteOk extends L2GameServerPacket {
    private String _partyLeader;

    public ExRegistWaitingSubstituteOk(String partyLeader) {
        _partyLeader = partyLeader;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x104);
        writeS(_partyLeader);
        // должны быть еще тип заменяемого чара (воин, маг, сумонер, етц) и имя самого чара вроде.
    }
}
