package l2rt.gameserver.communitybbs.Manager;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

public class TopBBSManager extends BaseBBSManager
{
	public void showTopPage(L2Player activeChar, String page, String subcontent)
	{
		if(page == null || page.isEmpty())
			page = "index";
		else
			page = page.replace("../", "").replace("..\\", "");

		page = ConfigSystem.get("CommunityBoardHtmlRoot") + page + ".htm";

		String content = Files.read(page, activeChar);
		if(content == null)
		{
			if(subcontent == null)
				content = "<html><body><br><br><center>404 Not Found: " + page + "</center></body></html>";
			else
				content = "<html><body>%content%</body></html>";
		}
		if(subcontent != null)
			content = content.replace("%content%", subcontent);
		separateAndSend(content, activeChar);
	}

	@Override
	public void parsecmd(String command, L2Player activeChar)
	{
		if(command.equals("_bbstop") || command.equals("_bbshome"))
			showTopPage(activeChar, "index", null);
		else if(command.startsWith("_bbstop;"))
			showTopPage(activeChar, command.replaceFirst("_bbstop;", ""), null);
		else
			separateAndSend("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", activeChar);
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{}

	private static TopBBSManager _Instance = new TopBBSManager();

	public static TopBBSManager getInstance()
	{
		return _Instance;
	}
}