package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.tables.CharNameTable;
import l2rt.gameserver.network.serverpackets.ExIsCharNameCreatable;
import l2rt.util.Util;

public class RequestCharacterNameCreatable extends L2GameClientPacket
{
    private static final String _C__D0_B0_REQUESTCHARACTERNAMECREATABLE = "[C] D0:B0 RequestCharacterNameCreatable";
    private String _nickname;

    @Override
	protected void readImpl()
	{
        _nickname = readS();
	}

    @Override
	protected void runImpl()
	{
		if(CharNameTable.getInstance().accountCharNumber(getClient().getLoginName()) >= 8)
		{
			sendPacket(new ExIsCharNameCreatable(ExIsCharNameCreatable.REASON_TOO_MANY_CHARACTERS));
			return;
		}
		if(!Util.isMatchingRegexp(_nickname, Config.CNAME_TEMPLATE))
		{
			sendPacket(new ExIsCharNameCreatable(ExIsCharNameCreatable.REASON_16_ENG_CHARS));
			return;
		}
		if(CharNameTable.getInstance().doesCharNameExist(_nickname))
		{
			sendPacket(new ExIsCharNameCreatable(ExIsCharNameCreatable.REASON_NAME_ALREADY_EXISTS));
			return;
		}
		
		sendPacket(new ExIsCharNameCreatable(ExIsCharNameCreatable.REASON_CREATION_OK));
	}

    @Override
	public String getType()
	{
		return _C__D0_B0_REQUESTCHARACTERNAMECREATABLE;
	}
}
