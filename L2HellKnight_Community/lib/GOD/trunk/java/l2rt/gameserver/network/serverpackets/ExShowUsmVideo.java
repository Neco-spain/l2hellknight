package l2rt.gameserver.network.serverpackets;

/**
 * RusDev
 * @author OSTIN
 */
 
public class ExShowUsmVideo extends L2GameServerPacket
{
	private static final String _S__FE_10D_EXSHOWUSM = "[S] FE:10D ExShowUsm";
	int _usmVideo;
	
	public static int GD1_INTRO = 2;
	
	public static int Q001 = 0x01; // Какие то врата красные
	public static int Q002 = 0x03; // Какие то врата синие
	public static int Q003 = 0x04; // Какие то типа церберы
	public static int Q004 = 0x05; // Ниче не показывает
	public static int Q005 = 0x06; // Богиня разрушения предлагает славу тьмы
	public static int Q006 = 0x07; // Богиня разрушения... отомстить...
	public static int Q007 = 0x08; // Богиня разрушения... Принеси тьме велику. жертву
	public static int Q009 = 0x09; // ниче нету
	public static int Q010 = 0x0A; // Пробуждение, начало
	
	public static int AWAKE_1 = 0x8B;
	public static int AWAKE_2 = 0x8C;
	public static int AWAKE_3 = 0x8D;
	public static int AWAKE_4 = 0x8E;
	public static int AWAKE_5 = 0x8F;
	public static int AWAKE_6 = 0x90;
	public static int AWAKE_7 = 0x91;
	public static int AWAKE_8 = 0x92;
	
	public ExShowUsmVideo(int usmVideo)
	{
		_usmVideo = usmVideo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x10D);
		writeD(_usmVideo);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_10D_EXSHOWUSM;
	}
}