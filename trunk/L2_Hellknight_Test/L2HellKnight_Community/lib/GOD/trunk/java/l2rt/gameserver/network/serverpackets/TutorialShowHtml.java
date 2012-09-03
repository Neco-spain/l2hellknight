package l2rt.gameserver.network.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
	public static int NORMAL_WINDOW = 0x01;
	public static int LARGE_WINDOW = 0x02;

	private int _windowType;

	/**
	 * <html><head><body><center>
	 * <font color="LEVEL">Quest</font>
	 * </center>
	 * <br>
	 * Speak to the <font color="LEVEL"> Paagrio Priests </font>
	 * of the Temple of Paagrio. They will explain the basics of combat through quests.
	 * <br>
	 * You must visit them, for they will give you a useful gift after you complete a quest.
	 * <br>
	 * They are marked in yellow on the radar, at the upper-right corner of the screen.
	 * You must visit them if you wish to advance.
	 * <br>
	 * <a action="link tutorial_close_0">Close Window</a>
	 * </body></html>
	 *
	 * ВНИМАНИЕ!!! Клиент отсылает назад action!!! Используется как БАЙПАСС В RequestTutorialLinkHtml!!!
	 */
	private String _html;

	public TutorialShowHtml(String html, int windowType)
	{
		_html = html;
		_windowType = windowType;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xa6);
		writeD(_windowType);
		writeS(_html);
	}
}