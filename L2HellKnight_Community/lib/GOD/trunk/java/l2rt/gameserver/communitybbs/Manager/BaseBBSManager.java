package l2rt.gameserver.communitybbs.Manager;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ShowBoard;

public abstract class BaseBBSManager
{
	public abstract void parsecmd(String command, L2Player activeChar);

	public abstract void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar);

	protected void separateAndSend(String html, L2Player activeChar)
	{
		ShowBoard.separateAndSend(html, activeChar);
	}

	protected void send1001(String html, L2Player activeChar)
	{
		ShowBoard.send1001(html, activeChar);
	}

	protected void send1002(L2Player activeChar)
	{
		ShowBoard.send1002(activeChar, " ", " ", "0");
	}

	protected void send1002(L2Player activeChar, String string, String string2, String string3)
	{
		ShowBoard.send1002(activeChar, string, string2, string3);
	}
}