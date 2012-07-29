package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.serverpackets.ExMentorList;
import l2p.gameserver.utils.Mentoring;

/**
 * @author Cain
 */
public class RequestMentorCancel extends L2GameClientPacket {
    private int _mtype;
    private String _charName;

    @Override
    protected void readImpl() {
        _mtype = readD();       // 00 приходит если ученик разрывает контракт с наставником. 01 приходит, когда наставник разрывает контракт с учеником.
        _charName = readS();    // Name
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        Player menteeChar = World.getPlayer(_charName);

        activeChar.getMenteeList().remove(_charName, _mtype == 1, true);
        activeChar.sendPacket(new ExMentorList(activeChar));
        if (menteeChar != null && menteeChar.isOnline())
        {
            menteeChar.getMenteeList().remove(activeChar.getName(), _mtype != 1, false);
            menteeChar.sendPacket(new ExMentorList(menteeChar));
        }
        Mentoring.applyMentoringCond(activeChar,false);
        Mentoring.setTimePenalty(_mtype == 1 ? activeChar.getObjectId() : activeChar.getMenteeList().getMentor(),System.currentTimeMillis() + 7 * 24 * 3600 * 1000L, -1);
    }
}