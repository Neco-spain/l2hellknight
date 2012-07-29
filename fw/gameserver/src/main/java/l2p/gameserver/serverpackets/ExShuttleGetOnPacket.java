package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Playable;
import l2p.gameserver.model.entity.boat.Shuttle;
import l2p.gameserver.utils.Location;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 30.05.12
 * Time: 13:07
 */
public class ExShuttleGetOnPacket extends L2GameServerPacket
{
    private int _playerObjectId, _shuttleId;
    private Location _loc;

    public ExShuttleGetOnPacket(Playable cha, Shuttle shuttle, Location loc)
    {
        _playerObjectId = cha.getObjectId();
        _shuttleId = shuttle.getBoatId();
        _loc = loc;
    }

    @Override
    protected final void writeImpl()
    {
        writeEx(0xCB);
        writeD(_playerObjectId); // Player ObjID
        writeD(_shuttleId); // Shuttle ID (Arkan: 1,2; Cruma: 3)
        writeD(_loc.x); // X in shuttle
        writeD(_loc.y); // Y in shuttle
        writeD(_loc.z); // Z in shuttle
    }
}
