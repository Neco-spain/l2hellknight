package l2p.gameserver.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket {
    public static final int TYPE_HTML = 1;
    public static final int TYPE_WINDOW = 2;

    public static final String QT_001 = "..\\L2Text\\QT_001_Radar_01.htm";
    public static final String QT_002 = "..\\L2Text\\QT_002_Guide_01.htm";
    public static final String QT_003 = "..\\L2Text\\QT_003_bullet_01.htm";
    public static final String QT_004 = "..\\L2Text\\QT_004_skill_01.htm";
    public static final String QT_009 = "..\\L2Text\\QT_009_enchant_01.htm";
    public static final String GUIDE = "..\\L2Text\\Guide_Ad.htm"; // Для книжки, которая выдается после получения второй профы

    private String html;
    private int type;

    public TutorialShowHtml(String html, int type) {
        this.html = html;
        this.type = type;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xA6);

        writeD(type);
        writeS(html);
    }
}