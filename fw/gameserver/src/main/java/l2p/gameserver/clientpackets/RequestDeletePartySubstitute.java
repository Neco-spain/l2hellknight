package l2p.gameserver.clientpackets;

import l2p.gameserver.model.FindPartyManager;
import l2p.gameserver.model.Player;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 23.05.12
 * Time: 22:59
 */
public class RequestDeletePartySubstitute extends L2GameClientPacket{
    Player player = getClient().getActiveChar();

    @Override
    protected void readImpl(){
    }

    @Override
    protected void runImpl(){
        FindPartyManager.checkStatusOnParty(player);
    }
}
