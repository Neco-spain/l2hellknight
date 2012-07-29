package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.JumpHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.jump.JumpLocation;
import l2p.gameserver.serverpackets.ExFlyMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RequestFlyMoveStart extends L2GameClientPacket {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected void readImpl() {
        // Do nothing
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (!activeChar.isInZone(Zone.ZoneType.jump)) {
            return;
        }

        String zoneName = activeChar.getZone(Zone.ZoneType.jump).getName();
        List<JumpLocation> jumpLocs = JumpHolder.getInstance().getJumpLocations(zoneName);
        if (jumpLocs.isEmpty()) {
            log.error("Not found JumpLocs for jump zone: " + zoneName);
            return;
        }

        activeChar.setJumpLocId(-1);

        if (jumpLocs.size() == 1) {
            activeChar.sendPacket(new ExFlyMove(ExFlyMove.FLY_TYPE_JUMP, activeChar.getObjectId(), jumpLocs));
        } else if (jumpLocs.size() > 1) {
            activeChar.sendPacket(new ExFlyMove(ExFlyMove.FLY_TYPE_CHOOSE, activeChar.getObjectId(), jumpLocs));
        }
    }
}