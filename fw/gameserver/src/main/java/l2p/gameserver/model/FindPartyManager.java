package l2p.gameserver.model;

import l2p.gameserver.serverpackets.ExWaitWaitingSubStituteInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 23.05.12
 * Time: 22:51
 */
public class FindPartyManager {
    public static Logger _log = Logger.getLogger(FindPartyManager.class.getName());
    static List<Player> _partyFind = new ArrayList<Player>();

    public static void searchPartyOn(Player activeChar) {
        _partyFind.add(activeChar);
        //_log.info(String.valueOf(_partyFind.size()));
        activeChar.sendPacket(new ExWaitWaitingSubStituteInfo(true));
    }

    public static void searchPartyOff(Player activeChar) {
        _partyFind.remove(activeChar);
        //_log.info(String.valueOf(_partyFind.size()));
        activeChar.sendPacket(new ExWaitWaitingSubStituteInfo(false));
    }

    public static void checkStatusOnParty(Player activeChar) {
        //_log.info(String.valueOf(_partyFind.size()));
        if (!activeChar.isInParty())
            activeChar.sendPacket(new ExWaitWaitingSubStituteInfo(true));

        if (_partyFind.contains(activeChar)) {
            _partyFind.remove(activeChar);
            //_log.info("contains " + _partyFind.size());
            activeChar.sendPacket(new ExWaitWaitingSubStituteInfo(true));
        }
    }

    /*
        Возвращает id найденого для замены чара
     */
    public static int findForSubstitute(int substChar) {
        Player substitutePlayer = World.getPlayer(substChar);
        //[TODO] Cain подбор чара на замену по лвлу и классу.
        return 0;
    }

    public static boolean isSearchParty(Player player) {
        return _partyFind.contains(player);
    }
}
