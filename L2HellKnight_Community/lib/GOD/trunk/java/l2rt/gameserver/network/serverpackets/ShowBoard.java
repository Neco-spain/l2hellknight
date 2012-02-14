package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.util.GArray;

import java.util.logging.Logger;

public class ShowBoard extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(ShowBoard.class.getName());
	private String _htmlCode;
	private String _id;
	private GArray<String> _arg;

	static final ShowBoard CACHE_NULL_102 = new ShowBoard(null, "102"), CACHE_NULL_103 = new ShowBoard(null, "103");

	public static void separateAndSend(String html, L2Player activeChar)
	{
		activeChar.cleanBypasses(true);
		html = activeChar.encodeBypasses(html, true);

		if(html.length() < 8180)
			activeChar.sendPacket(new ShowBoard(html, "101"), CACHE_NULL_102, CACHE_NULL_103);
		else if(html.length() < 8180 * 2)
			activeChar.sendPacket(new ShowBoard(html.substring(0, 8180), "101"), new ShowBoard(html.substring(8180, html.length()), "102"), CACHE_NULL_103);
		else if(html.length() < 8180 * 3)
			activeChar.sendPacket(new ShowBoard(html.substring(0, 8180), "101"), new ShowBoard(html.substring(8180, 8180 * 2), "102"), new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
	}

	public static void send1001(String html, L2Player activeChar)
	{
		if(html.length() < 8180)
			activeChar.sendPacket(new ShowBoard(html, "1001"));
	}

	public static void send1002(L2Player activeChar, String string, String string2, String string3)
	{
		GArray<String> _arg = new GArray<String>();
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add("0");
		_arg.add(activeChar.getName());
		_arg.add(Integer.toString(activeChar.getObjectId()));
		_arg.add(activeChar.getAccountName());
		_arg.add("9");
		_arg.add(string2);
		_arg.add(string2);
		_arg.add(string);
		_arg.add(string3);
		_arg.add(string3);
		_arg.add("0");
		_arg.add("0");
		activeChar.sendPacket(new ShowBoard(_arg));
	}

	private ShowBoard(String htmlCode, String id)
	{
		if(htmlCode != null && htmlCode.length() > 8192) // html code must not exceed 8192 bytes
		{
			_log.warning("Html '" + htmlCode + "' is too long! this will crash the client!");
			_htmlCode = "<html><body>Html was too long</body></html>";
			return;
		}
		_id = id;
		_htmlCode = htmlCode;
	}

	private ShowBoard(GArray<String> arg)
	{
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7b);
		writeC(0x01); //c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _maillist_0_1_0_"); // mail
		writeS("bypass _friendlist_0_"); // friends
		writeS("");
		String str = _id + "\u0008";
		if(_id.equals("1002"))
			for(String arg : _arg)
				str += arg + " \u0008";
		else if(_htmlCode != null)
			str += _htmlCode;
		writeS(str);
	}
}