package l2rt.gameserver.communitybbs.Manager;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

/**
 * @author IDEASERV
 */

public class AddFavBBSManager extends BaseBBSManager
{
	public static AddFavBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		String content = Files.read(ConfigSystem.get("CommunityBoardHtmlRoot") + "getfav.htm", player);
		separateAndSend(content, player);
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
	}

	private static class SingletonHolder
	{
		protected static final AddFavBBSManager _instance = new AddFavBBSManager();
	}
}
