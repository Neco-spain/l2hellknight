package l2rt.gameserver.loginservercon;

public class Attribute
{

	public static final String[] statusString = { "Auto", "Good", "Normal", "Full", "Down", "Gm Only" };

	public static final int SERVER_LIST_STATUS = 0x01;
	public static final int SERVER_LIST_CLOCK = 0x02;
	public static final int SERVER_LIST_SQUARE_BRACKET = 0x03;
	public static final int MAX_PLAYERS = 0x04;
	public static final int TEST_SERVER = 0x05;

	public static final int STATUS_AUTO = 0x00;
	public static final int STATUS_GOOD = 0x01;
	public static final int STATUS_NORMAL = 0x02;
	public static final int STATUS_FULL = 0x03;
	public static final int STATUS_DOWN = 0x04;
	public static final int STATUS_GM_ONLY = 0x05;

	public static final int ON = 0x01;
	public static final int OFF = 0x00;

	public int id;
	public int value;

	public Attribute(int id, int value)
	{
		this.id = id;
		this.value = value;
	}
}
