package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.boat.Shuttle;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 30.05.12
 * Time: 13:08
 */
public class ExShuttleMovePacket extends L2GameServerPacket
{
    private final Shuttle _shuttle;

    public ExShuttleMovePacket(Shuttle shuttle)
    {
        _shuttle = shuttle;
    }

    @Override
    protected final void writeImpl()
    {
        writeEx(0xCD);
        writeD(_shuttle.getBoatId()); // Shuttle ID (Arkan: 1,2; Cruma: 3)
        writeD(_shuttle.getMoveSpeed()); // Speed
        writeD(0x00); //unk: 0 (0x00000000)
        writeD(_shuttle.getDestination().x); // Destination X
        writeD(_shuttle.getDestination().y); // Destination Y
        writeD(_shuttle.getDestination().z); // Destination Z
    }
}
