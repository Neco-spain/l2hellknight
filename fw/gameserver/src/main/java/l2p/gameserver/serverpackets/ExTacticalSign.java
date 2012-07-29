package l2p.gameserver.serverpackets;

/**
 * Created by  Cain
 * Date: 23.05.2012 Time: 12:47
 */

public class ExTacticalSign extends L2GameServerPacket {
    private int _targetId;
    private int _signId;

    public ExTacticalSign(int target, int sign)
    {
        _targetId = target;
        _signId = sign;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xFF);
        writeD(_targetId);
        writeD(_signId);
    }
}