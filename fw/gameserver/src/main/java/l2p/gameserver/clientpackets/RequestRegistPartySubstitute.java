package l2p.gameserver.clientpackets;

import l2p.gameserver.model.FindPartyManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.serverpackets.ExRegistPartySubstitute;
import l2p.gameserver.serverpackets.ExRegistWaitingSubstituteOk;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 25.05.12
 * Time: 21:22
 * запрос от лидера на замену чара
 */
public class RequestRegistPartySubstitute extends L2GameClientPacket {
    private int _changeCharId;

    @Override
    protected void readImpl() {
        _changeCharId = readD();
    }

    @Override
    protected void runImpl() {
        int substChar = FindPartyManager.findForSubstitute(_changeCharId);
        Player substPlayer = World.getPlayer(substChar);
        substPlayer.sendPacket(new ExRegistWaitingSubstituteOk("partyLeaderName"));
    }
}
