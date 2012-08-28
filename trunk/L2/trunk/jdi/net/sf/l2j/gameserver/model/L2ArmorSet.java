package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class L2ArmorSet
{
  private final int _chest;
  private final int _legs;
  private final int _head;
  private final int _gloves;
  private final int _feet;
  private final int _skillId;
  private final int _shield;
  private final int _shieldSkillId;
  private final int _enchant6Skill;

  public L2ArmorSet(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield, int shield_skill_id, int enchant6skill)
  {
    _chest = chest;
    _legs = legs;
    _head = head;
    _gloves = gloves;
    _feet = feet;
    _skillId = skill_id;

    _shield = shield;
    _shieldSkillId = shield_skill_id;

    _enchant6Skill = enchant6skill;
  }

  public boolean containAll(L2PcInstance player)
  {
    Inventory inv = player.getInventory();

    L2ItemInstance legsItem = inv.getPaperdollItem(11);
    L2ItemInstance headItem = inv.getPaperdollItem(6);
    L2ItemInstance glovesItem = inv.getPaperdollItem(9);
    L2ItemInstance feetItem = inv.getPaperdollItem(12);

    int legs = 0;
    int head = 0;
    int gloves = 0;
    int feet = 0;

    if (legsItem != null) legs = legsItem.getItemId();
    if (headItem != null) head = headItem.getItemId();
    if (glovesItem != null) gloves = glovesItem.getItemId();
    if (feetItem != null) feet = feetItem.getItemId();

    return containAll(_chest, legs, head, gloves, feet);
  }

  public boolean containAll(int chest, int legs, int head, int gloves, int feet)
  {
    if ((_chest != 0) && (_chest != chest))
      return false;
    if ((_legs != 0) && (_legs != legs))
      return false;
    if ((_head != 0) && (_head != head))
      return false;
    if ((_gloves != 0) && (_gloves != gloves)) {
      return false;
    }
    return (_feet == 0) || (_feet == feet);
  }

  public boolean containItem(int slot, int itemId)
  {
    switch (slot)
    {
    case 10:
      return _chest == itemId;
    case 11:
      return _legs == itemId;
    case 6:
      return _head == itemId;
    case 9:
      return _gloves == itemId;
    case 12:
      return _feet == itemId;
    case 7:
    case 8: } return false;
  }

  public int getSkillId()
  {
    return _skillId;
  }

  public boolean containShield(L2PcInstance player) {
    Inventory inv = player.getInventory();

    L2ItemInstance shieldItem = inv.getPaperdollItem(8);

    return (shieldItem != null) && (shieldItem.getItemId() == _shield);
  }

  public boolean containShield(int shield_id)
  {
    if (_shield == 0) {
      return false;
    }
    return _shield == shield_id;
  }

  public int getShieldSkillId() {
    return _shieldSkillId;
  }

  public int getEnchant6skillId() {
    return _enchant6Skill;
  }

  public boolean isEnchanted6(L2PcInstance player)
  {
    if (!containAll(player)) {
      return false;
    }
    Inventory inv = player.getInventory();

    L2ItemInstance chestItem = inv.getPaperdollItem(10);
    L2ItemInstance legsItem = inv.getPaperdollItem(11);
    L2ItemInstance headItem = inv.getPaperdollItem(6);
    L2ItemInstance glovesItem = inv.getPaperdollItem(9);
    L2ItemInstance feetItem = inv.getPaperdollItem(12);

    if (chestItem.getEnchantLevel() < 6)
      return false;
    if ((_legs != 0) && (legsItem.getEnchantLevel() < 6))
      return false;
    if ((_gloves != 0) && (glovesItem.getEnchantLevel() < 6))
      return false;
    if ((_head != 0) && (headItem.getEnchantLevel() < 6)) {
      return false;
    }
    return (_feet == 0) || (feetItem.getEnchantLevel() >= 6);
  }
}