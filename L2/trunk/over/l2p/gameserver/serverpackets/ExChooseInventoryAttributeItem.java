package l2p.gameserver.serverpackets;

import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.ItemFunctions;

public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
  private int _itemId;
  private boolean _disableFire;
  private boolean _disableWater;
  private boolean _disableEarth;
  private boolean _disableWind;
  private boolean _disableDark;
  private boolean _disableHoly;
  private int _stoneLvl;

  public ExChooseInventoryAttributeItem(ItemInstance item)
  {
    _itemId = item.getItemId();
    _disableFire = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.FIRE);
    _disableWater = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.WATER);
    _disableWind = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.WIND);
    _disableEarth = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.EARTH);
    _disableHoly = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.HOLY);
    _disableDark = (ItemFunctions.getEnchantAttributeStoneElement(item.getItemId(), false) == Element.UNHOLY);
    _stoneLvl = (item.getTemplate().isAttributeCrystal() ? 6 : 3);
  }

  protected final void writeImpl()
  {
    writeEx(98);
    writeD(_itemId);
    writeD(_disableFire ? 1 : 0);
    writeD(_disableWater ? 1 : 0);
    writeD(_disableWind ? 1 : 0);
    writeD(_disableEarth ? 1 : 0);
    writeD(_disableHoly ? 1 : 0);
    writeD(_disableDark ? 1 : 0);
    writeD(_stoneLvl);
  }
}