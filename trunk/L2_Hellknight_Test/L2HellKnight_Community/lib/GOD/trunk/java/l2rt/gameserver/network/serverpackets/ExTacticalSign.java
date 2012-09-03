package l2rt.gameserver.network.serverpackets;

/**
 * 
 * @author KID
 *
 */ 
public class ExTacticalSign extends L2GameServerPacket
{
    private int targetId;
    private int signId;

    public ExTacticalSign(int target, int sign)
    {
        this.targetId = target;
        this.signId = sign;
    }
    
    @Override
    protected final void writeImpl()
    {
        writeC(EXTENDED_PACKET);
        writeH(0xff);
        writeD(targetId);
        writeD(signId);
    }
}  