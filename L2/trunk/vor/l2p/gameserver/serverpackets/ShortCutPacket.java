package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.ShortCut;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.templates.item.ItemTemplate;

public abstract class ShortCutPacket extends L2GameServerPacket
{
  public static ShortcutInfo convert(Player player, ShortCut shortCut)
  {
    ShortcutInfo shortcutInfo = null;
    int page = shortCut.getSlot() + shortCut.getPage() * 12;
    switch (shortCut.getType())
    {
    case 1:
      int reuseGroup = -1; int currentReuse = 0; int reuse = 0; int augmentationId = 0;
      ItemInstance item = player.getInventory().getItemByObjectId(shortCut.getId());
      if (item != null)
      {
        augmentationId = item.getAugmentationId();
        reuseGroup = item.getTemplate().getDisplayReuseGroup();
        if (item.getTemplate().getReuseDelay() > 0)
        {
          TimeStamp timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
          if (timeStamp != null)
          {
            currentReuse = (int)(timeStamp.getReuseCurrent() / 1000L);
            reuse = (int)(timeStamp.getReuseBasic() / 1000L);
          }
        }
      }
      shortcutInfo = new ItemShortcutInfo(shortCut.getType(), page, shortCut.getId(), reuseGroup, currentReuse, reuse, augmentationId, shortCut.getCharacterType());
      break;
    case 2:
      shortcutInfo = new SkillShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getLevel(), shortCut.getCharacterType());
      break;
    default:
      shortcutInfo = new ShortcutInfo(shortCut.getType(), page, shortCut.getId(), shortCut.getCharacterType());
    }

    return shortcutInfo;
  }

  protected static class ShortcutInfo
  {
    protected final int _type;
    protected final int _page;
    protected final int _id;
    protected final int _characterType;

    public ShortcutInfo(int type, int page, int id, int characterType)
    {
      _type = type;
      _page = page;
      _id = id;
      _characterType = characterType;
    }

    protected void write(ShortCutPacket p)
    {
      p.writeD(_type);
      p.writeD(_page);
      write0(p);
    }

    protected void write0(ShortCutPacket p)
    {
      p.writeD(_id);
      p.writeD(_characterType);
    }
  }

  protected static class SkillShortcutInfo extends ShortCutPacket.ShortcutInfo
  {
    private final int _level;

    public SkillShortcutInfo(int type, int page, int id, int level, int characterType)
    {
      super(page, id, characterType);
      _level = level;
    }

    public int getLevel()
    {
      return _level;
    }

    protected void write0(ShortCutPacket p)
    {
      p.writeD(_id);
      p.writeD(_level);
      p.writeC(0);
      p.writeD(_characterType);
    }
  }

  protected static class ItemShortcutInfo extends ShortCutPacket.ShortcutInfo
  {
    private int _reuseGroup;
    private int _currentReuse;
    private int _basicReuse;
    private int _augmentationId;

    public ItemShortcutInfo(int type, int page, int id, int reuseGroup, int currentReuse, int basicReuse, int augmentationId, int characterType)
    {
      super(page, id, characterType);
      _reuseGroup = reuseGroup;
      _currentReuse = currentReuse;
      _basicReuse = basicReuse;
      _augmentationId = augmentationId;
    }

    protected void write0(ShortCutPacket p)
    {
      p.writeD(_id);
      p.writeD(_characterType);
      p.writeD(_reuseGroup);
      p.writeD(_currentReuse);
      p.writeD(_basicReuse);
      p.writeD(_augmentationId);
    }
  }
}