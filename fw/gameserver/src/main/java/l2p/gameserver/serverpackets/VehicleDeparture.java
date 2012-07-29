package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket {
    private int _moveSpeed, _rotationSpeed;
    private int _boatObjId;
    private Location _loc;

    public VehicleDeparture(Boat boat) {
        _boatObjId = boat.getObjectId();
        _moveSpeed = boat.getMoveSpeed();
        _rotationSpeed = boat.getRotationSpeed();
        _loc = boat.getDestination();
    }

    @Override
    protected final void writeImpl() {
        writeC(0x6c);
        writeD(_boatObjId);
        writeD(_moveSpeed);
        writeD(_rotationSpeed);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
    }
}