package l2rt.gameserver.communitybbs.Manager;

import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

public class FailBBSManager extends BaseBBSManager
{
	public static FailBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		if (command.equals("_bbsbash;"))
		{
			String content = Files.read("data/html/CommunityBoardPVP/10.htm", player);
			content = content.replace("%name%", player.getName());
			separateAndSend(content, player);
		}
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
	}

	private static class SingletonHolder
	{
		protected static final FailBBSManager _instance = new FailBBSManager();
	}
}