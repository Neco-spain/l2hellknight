package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.JumpHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.jump.JumpLocation;
import l2p.gameserver.serverpackets.ExFlyMove;
import l2p.gameserver.serverpackets.ExFlyMoveBroadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class RequestFlyMove extends L2GameClientPacket {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private int locId; // -1 после приземления

    @Override
    protected void readImpl() {
        locId = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }

        if (locId == -1) { // Приходит после приземления игрока
            return;
        }

        if (locId == activeChar.getJumpLocId()) { // Уже прыгнул в эту локацию
            return;
        }

        JumpLocation jumpLoc = JumpHolder.getInstance().getJumpLocationById(locId);
        if (jumpLoc == null) {
            log.error("Not found JumpLocation " + locId);
            return;
        }

        activeChar.broadcastPacketToOthers(new ExFlyMoveBroadcast(activeChar, 2, jumpLoc.getLocation()));
        activeChar.setLoc(jumpLoc.getLocation());
        activeChar.setJumpLocId(locId);

        if (jumpLoc.isLast()) {
            return;
        }

        List<JumpLocation> locations = new ArrayList<JumpLocation>();
        for (int route : jumpLoc.getRoutes()) {
            JumpLocation nextJumpLoc = JumpHolder.getInstance().getJumpLocationById(route);
            if (nextJumpLoc == null) {
                log.error("Not found next JumpLocation " + route);
                continue;
            }
            locations.add(nextJumpLoc);
        }

        if (locations.size() == 1) {
            activeChar.sendPacket(new ExFlyMove(ExFlyMove.FLY_TYPE_JUMP, activeChar.getObjectId(), locations));
        } else if (locations.size() > 1) {
            activeChar.sendPacket(new ExFlyMove(ExFlyMove.FLY_TYPE_CHOOSE, activeChar.getObjectId(), locations));
        } else {
            log.error("Not found next jump location for id " + locId);
        }
    }
}
