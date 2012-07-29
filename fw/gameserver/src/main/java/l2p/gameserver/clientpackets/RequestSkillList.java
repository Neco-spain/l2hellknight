package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;

public final class RequestSkillList extends L2GameClientPacket {
    private static final String _C__50_REQUESTSKILLLIST = "[C] 50 RequestSkillList";

    @Override
    protected void readImpl() {
        // this is just a trigger packet. it has no content
    }

    @Override
    protected void runImpl() {
        Player cha = getClient().getActiveChar();

        if (cha != null) {
            cha.sendSkillList();
        }
    }

    @Override
    public String getType() {
        return _C__50_REQUESTSKILLLIST;
    }
}
