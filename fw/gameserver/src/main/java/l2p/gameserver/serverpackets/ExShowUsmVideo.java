package l2p.gameserver.serverpackets;

public class ExShowUsmVideo extends L2GameServerPacket {
    private int _usmVideo;

    public static int Q001 = 1; // Какие то врата красные
    public static int GD1_INTRO = 2;
    public static int Q002 = 3; // Какие то врата синие
    public static int Q003 = 4; // Какие то типа церберы - квест на вторую профессию
    public static int Q004 = 5; // Ниче не показывает
    public static int Q005 = 6; // Богиня разрушения предлагает славу тьмы
    public static int Q006 = 7; // Богиня разрушения... отомстить...
    public static int Q007 = 8; // Богиня разрушения... Принеси тьме велику. жертву
    public static int Q009 = 9; // ниче нету
    public static int Q010 = 10; // Пробуждение, начало

    public static int AWAKE_1 = 139;
    public static int AWAKE_2 = 140;
    public static int AWAKE_3 = 141;
    public static int AWAKE_4 = 142;
    public static int AWAKE_5 = 143;
    public static int AWAKE_6 = 144;
    public static int AWAKE_7 = 145;
    public static int AWAKE_8 = 146;

    public ExShowUsmVideo(int usmVideo) {
        _usmVideo = usmVideo;
    }

    @Override
    protected void writeImpl() {
        writeEx(0x10D);
        writeD(_usmVideo);
    }
}