package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.utils.Location;

public class ExFlyMoveBroadcast extends L2GameServerPacket {
    private int objId;
    private int flyType;
    private int x, y, z;
    private Location destLoc;

    public ExFlyMoveBroadcast(Player player, int flyType, Location destLoc) {
        objId = player.getObjectId();
        this.flyType = flyType;
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        this.destLoc = destLoc;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x10C);
        writeD(objId);

        writeD(flyType);
        writeD(0x00);//unknown

        writeD(x);
        writeD(y);
        writeD(z);
        writeD(0x00);

        writeD(destLoc.getX());
        writeD(destLoc.getY());
        writeD(destLoc.getZ());
    }
}
