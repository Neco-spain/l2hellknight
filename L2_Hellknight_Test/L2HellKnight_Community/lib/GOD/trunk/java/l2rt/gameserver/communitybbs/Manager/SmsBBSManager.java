package l2rt.gameserver.communitybbs.Manager;

import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

public class SmsBBSManager extends BaseBBSManager
{
	public static SmsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		String content = Files.read("data/html/CommunityBoardPVP/303.htm", player);
		content = content.replace("%name%", player.getName());
		separateAndSend(content, player);
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
	}

	private static class SingletonHolder
	{
		protected static final SmsBBSManager _instance = new SmsBBSManager();
	}
}