package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Shuttle;
import l2p.gameserver.utils.Location;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 30.05.12
 * Time: 13:04
 */
public class ExMTLInShuttlePacket extends L2GameServerPacket {
    private int _playerObjectId, _shuttleId;
    private Location _origin, _destination;

    public ExMTLInShuttlePacket(Player player, Shuttle shuttle, Location origin, Location destination)
    {
        _playerObjectId = player.getObjectId();
        _shuttleId = shuttle.getBoatId();
        _origin = origin;
        _destination = destination;
    }

    @Override
    protected final void writeImpl()
    {
        writeEx(0xCE);
        writeD(_playerObjectId); // Player ObjID
        writeD(_shuttleId); // Shuttle ID (Arkan: 1,2; Cruma: 3)
        writeD(_destination.x); // Destination X in shuttle
        writeD(_destination.y); // Destination Y in shuttle
        writeD(_destination.z); // Destination Z in shuttle
        writeD(_origin.x); // X in shuttle
        writeD(_origin.y); // Y in shuttle
        writeD(_origin.z); // Z in shuttle
    }
}
