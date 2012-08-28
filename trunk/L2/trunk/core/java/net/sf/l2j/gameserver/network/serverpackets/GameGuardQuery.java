package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.protection.nProtect;

public class GameGuardQuery extends L2GameServerPacket
{
    private static final String _S__F9_GAMEGUARDQUERY = "[S] F9 GameGuardQuery";

    public GameGuardQuery()
    {

    }

    @Override
	public void runImpl()
    {
        getClient().setGameGuardOk(false);
    }

    @Override
	public void writeImpl()
    {
        writeC(0xf9);
        nProtect.getInstance().sendGameGuardQuery(this);
    }

    @Override
	public String getType()
    {
        return _S__F9_GAMEGUARDQUERY;
    }
}
