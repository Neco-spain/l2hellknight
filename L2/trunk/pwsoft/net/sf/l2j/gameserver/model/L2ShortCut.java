package net.sf.l2j.gameserver.model;

public class L2ShortCut
{
  public static final int TYPE_ITEM = 1;
  public static final int TYPE_SKILL = 2;
  public static final int TYPE_ACTION = 3;
  public static final int TYPE_MACRO = 4;
  public static final int TYPE_RECIPE = 5;
  private final int _slot;
  private final int _page;
  private final int _type;
  private final int _id;
  private final int _level;

  public L2ShortCut(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int unknown)
  {
    _slot = slotId;
    _page = pageId;
    _type = shortcutType;
    _id = shortcutId;
    _level = shortcutLevel;
  }

  public int getId()
  {
    return _id;
  }

  public int getLevel()
  {
    return _level;
  }

  public int getPage()
  {
    return _page;
  }

  public int getSlot()
  {
    return _slot;
  }

  public int getType()
  {
    return _type;
  }
}