package l2rt.gameserver.communitybbs.Manager;

import javolution.text.TextBuilder;
import l2rt.config.ConfigSystem;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Files;

public class LinkBBSManager extends BaseBBSManager
{
	public static LinkBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		TextBuilder html = new TextBuilder();
		html.append("<center>Последние сообщения с форума</center>");
		html.append("<img src=L2UI.SquareWhite width=810 height=1>");
		html.append("<table width=810 bgcolor=CCCCCC>");
		html.append("<tr>");
		html.append("<td width=110>Дата</td>");
		html.append("<td width=300>Форум</td>");
		html.append("<td width=300>Последняя тема</td>");
		html.append("<td width=100>Перейти</td>");
		html.append("</tr>");
		html.append("</table>");

		String content = Files.read(ConfigSystem.get("CommunityBoardHtmlRoot") + "links.htm", player);
		content = content.replace("%forum_links%", html.toString());
		separateAndSend(content, player);
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
	}

	private static class SingletonHolder
	{
		protected static final LinkBBSManager _instance = new LinkBBSManager();
	}
}