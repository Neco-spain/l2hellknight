package l2rt.loginserver.gameservercon.lspackets;

import l2rt.loginserver.L2LoginClient;

public class PointConnectionL extends ServerBasePacket
{

    public PointConnectionL(L2LoginClient client)
    {
        writeC(29);
        writeS(client.getAccount());
        writeD(client.getPointL());
    }
}
