package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.network.serverpackets.L2GameServerPacket;

public class ExNewSkillToLearnByLevelUp extends L2GameServerPacket
{
    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0xfc);
    }
}
