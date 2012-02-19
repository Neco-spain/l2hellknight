package l2.brick.gameserver.network.serverpackets;

import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.util.Util;

public class OnEventTrigger extends L2GameServerPacket
{

    public OnEventTrigger(L2DoorInstance door, boolean opened)
    {
        _emitterId = door.getEmitter();
        if(Util.contains(_reverse_doors, door.getDoorId()))
            _opened = !opened;
        else
            _opened = opened;
    }

    public OnEventTrigger(int id, boolean opened)
    {
        _emitterId = id;
        _opened = opened;
    }

    protected final void writeImpl()
    {
        writeC(207);
        writeD(_emitterId);
        writeD(_opened ? 0 : 1);
    }

    public String getType()
    {
        return "[S] CF OnEventTrigger".intern();
    }

    private final int _emitterId;
    private final boolean _opened;
    private static final int _reverse_doors[] = {
        0xf73157, 0xf73158, 0xf73159
    };

}