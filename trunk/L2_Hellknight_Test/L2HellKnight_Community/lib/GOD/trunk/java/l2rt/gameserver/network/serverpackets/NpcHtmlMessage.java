package l2rt.gameserver.network.serverpackets;

import l2rt.extensions.scripts.Scripts;
import l2rt.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.Strings;
import l2rt.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 */
public class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	//
	private int _npcObjId, _questId;
	private String _html;
	private String _file = null;
	private GArray<String> _replaces = new GArray<String>();
	private int item_id = 0;
	private boolean have_appends = false;
	private final StackTraceElement[] ceatedFrom; //TODO убрать отладку

	public NpcHtmlMessage(L2Player player, L2NpcInstance npc, String filename, int val)
	{
		_npcObjId = npc.getObjectId();

		player.setLastNpc(npc);

		GArray<ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npc.getNpcId());
		if(appends != null && appends.size() > 0)
		{
			have_appends = true;
			if(filename != null && filename.equalsIgnoreCase("data/html/npcdefault.htm"))
				setHtml(""); // контент задается скриптами через DialogAppend_
			else
				setFile(filename);

			String replaces = "";

			// Добавить в конец странички текст, определенный в скриптах.
			Object[] script_args = new Object[] { new Integer(val) };
			for(ScriptClassAndMethod append : appends)
			{
				Object obj = player.callScripts(append.scriptClass, append.method, script_args);
				if(obj != null)
					replaces += obj;
			}

			if(!replaces.equals(""))
				replace("</body>", "\n" + Strings.bbParse(replaces) + "</body>");
		}
		else
			setFile(filename);

		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%festivalMins%", "");
		ceatedFrom = Thread.currentThread().getStackTrace();
	}

	public NpcHtmlMessage(L2Player player, L2NpcInstance npc)
	{
		if(npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
		ceatedFrom = Thread.currentThread().getStackTrace();
	}

	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
		// TODO player.setLastNpc(null);
		ceatedFrom = Thread.currentThread().getStackTrace();
	}

	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>"; //<title>Message:</title> <br><br><br>
		_html = text;
		return this;
	}

	public final NpcHtmlMessage setFile(String file)
	{
		_file = file;
		return this;
	}

	/** WTF is this? Never used. */
	public final NpcHtmlMessage setItemId(int _item_id)
	{
		item_id = _item_id;
		return this;
	}

	public void setQuest(int quest)
	{
		_questId = quest;
	}

	protected String html_load(String name, String lang)
	{
		String content = Files.read(name, lang);
		if(content == null)
			content = "Can't find file'" + name + "'";
		return content;
	}

	public NpcHtmlMessage replace(String pattern, String value)
	{
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}

	private static final Pattern objectId = Pattern.compile("%objectId%");
	private static final Pattern playername = Pattern.compile("%playername%");

	@Override
	protected final void writeImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_file != null) //TODO может быть не очень хорошо сдесь это делать...
		{
			String content = Files.read(_file, player);
			if(content == null)
				setHtml(have_appends && _file.endsWith(".htm") ? "" : _file);
			else
				setHtml(content);
		}

		for(int i = 0; i < _replaces.size(); i += 2)
			_html = _html.replaceAll(_replaces.get(i), _replaces.get(i + 1));

		if(objectId == null)
		{
			System.out.println("objectId == null");
			Thread.dumpStack();
		}

		if(_html == null)
		{
			L2NpcInstance npc = L2ObjectsStorage.getNpc(_npcObjId);
			System.out.println("[WARNING] NpcHtmlMessage, _html == null, npc: " + npc.toString());
			for(StackTraceElement e : ceatedFrom)
				System.out.println("\t" + e);
			System.out.println(Util.dumpObject(this, true, false, true));
			return;
		}

		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		_html = playername.matcher(_html).replaceAll(player.getName());

		player.cleanBypasses(false);
		_html = player.encodeBypasses(_html, false);

		if(_questId > 0)
		{
			writeC(EXTENDED_PACKET);
			writeH(0x8d);
			writeD(_npcObjId);
			writeS(_html);
			writeD(_questId);
		}
		else
		{
			writeC(0x19);
			writeD(_npcObjId);
			writeS(_html);
			writeD(item_id);
		}
	}
}