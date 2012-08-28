package l2p.gameserver.serverpackets;

import l2p.gameserver.serverpackets.components.NpcString;

public class ExShowScreenMessage extends NpcStringContainer
{
  public static final int SYSMSG_TYPE = 0;
  public static final int STRING_TYPE = 1;
  private int _type;
  private int _sysMessageId;
  private boolean _big_font;
  private boolean _effect;
  private ScreenMessageAlign _text_align;
  private int _time;

  @Deprecated
  public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font)
  {
    this(text, time, text_align, big_font, 1, -1, false);
  }

  @Deprecated
  public ExShowScreenMessage(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect) {
    super(NpcString.NONE, new String[] { text });
    _type = type;
    _sysMessageId = messageId;
    _time = time;
    _text_align = text_align;
    _big_font = big_font;
    _effect = showEffect;
  }

  public ExShowScreenMessage(NpcString t, int time, ScreenMessageAlign text_align, String[] params)
  {
    this(t, time, text_align, true, 1, -1, false, params);
  }

  public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, String[] params)
  {
    this(npcString, time, text_align, big_font, 1, -1, false, params);
  }

  public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, boolean showEffect, String[] params)
  {
    this(npcString, time, text_align, big_font, 1, -1, showEffect, params);
  }

  public ExShowScreenMessage(NpcString npcString, int time, ScreenMessageAlign text_align, boolean big_font, int type, int systemMsg, boolean showEffect, String[] params)
  {
    super(npcString, params);
    _type = type;
    _sysMessageId = systemMsg;
    _time = time;
    _text_align = text_align;
    _big_font = big_font;
    _effect = showEffect;
  }

  protected final void writeImpl()
  {
    writeEx(57);
    writeD(_type);
    writeD(_sysMessageId);
    writeD(_text_align.ordinal() + 1);
    writeD(0);
    writeD(_big_font ? 0 : 1);
    writeD(0);
    writeD(0);
    writeD(_effect ? 1 : 0);
    writeD(_time);
    writeD(1);
    writeElements();
  }

  public static enum ScreenMessageAlign
  {
    TOP_LEFT, 
    TOP_CENTER, 
    TOP_RIGHT, 
    MIDDLE_LEFT, 
    MIDDLE_CENTER, 
    MIDDLE_RIGHT, 
    BOTTOM_CENTER, 
    BOTTOM_RIGHT;
  }
}