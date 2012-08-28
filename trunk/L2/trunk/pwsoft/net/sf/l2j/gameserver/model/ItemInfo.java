package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.L2Item;

public class ItemInfo
{
  private int _objectId;
  private L2Item _item;
  private int _enchant;
  private int _augmentation;
  private int _count;
  private int _price;
  private int _type1;
  private int _type2;
  private int _equipped;
  private int _change;
  private int _mana;

  public ItemInfo(L2ItemInstance item)
  {
    if (item == null) return;

    _objectId = item.getObjectId();

    _item = item.getItem();

    _enchant = item.getEnchantLevel();

    if (item.isAugmented()) _augmentation = item.getAugmentation().getAugmentationId(); else {
      _augmentation = 0;
    }

    _count = item.getCount();

    _type1 = item.getCustomType1();
    _type2 = item.getCustomType2();

    _equipped = (item.isEquipped() ? 1 : 0);

    switch (item.getLastChange()) {
    case 1:
      _change = 1; break;
    case 2:
      _change = 2; break;
    case 3:
      _change = 3;
    }

    _mana = item.getMana();
  }

  public ItemInfo(L2ItemInstance item, int change)
  {
    if (item == null) return;

    _objectId = item.getObjectId();

    _item = item.getItem();

    _enchant = item.getEnchantLevel();

    if (item.isAugmented()) _augmentation = item.getAugmentation().getAugmentationId(); else {
      _augmentation = 0;
    }

    _count = item.getCount();

    _type1 = item.getCustomType1();
    _type2 = item.getCustomType2();

    _equipped = (item.isEquipped() ? 1 : 0);

    _change = change;

    _mana = item.getMana();
  }

  public int getObjectId() {
    return _objectId; } 
  public L2Item getItem() { return _item; } 
  public int getEnchant() { return _enchant; } 
  public int getAugemtationBoni() { return _augmentation; } 
  public int getCount() { return _count; } 
  public int getPrice() { return _price; } 
  public int getCustomType1() { return _type1; } 
  public int getCustomType2() { return _type2; } 
  public int getEquipped() { return _equipped; } 
  public int getChange() { return _change; } 
  public int getMana() { return _mana;
  }
}