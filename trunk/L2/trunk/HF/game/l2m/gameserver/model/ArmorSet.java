package l2m.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2m.gameserver.model.items.Inventory;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.data.tables.SkillTable;

public final class ArmorSet
{
  private final int _set_id;
  private final List<Integer> _chest = new ArrayList(1);
  private final List<Integer> _legs = new ArrayList(1);
  private final List<Integer> _head = new ArrayList(1);
  private final List<Integer> _gloves = new ArrayList(1);
  private final List<Integer> _feet = new ArrayList(1);
  private final List<Integer> _shield = new ArrayList(1);
  private final List<Skill> _skills = new ArrayList(1);
  private final List<Skill> _shieldSkills = new ArrayList(1);
  private final List<Skill> _enchant6skills = new ArrayList(1);

  public ArmorSet(int set_id, String[] chest, String[] legs, String[] head, String[] gloves, String[] feet, String[] skills, String[] shield, String[] shield_skills, String[] enchant6skills)
  {
    _set_id = set_id;

    if (chest != null) {
      for (String chestId : chest)
        _chest.add(Integer.valueOf(Integer.parseInt(chestId)));
    }
    if (legs != null) {
      for (String legsId : legs)
        _legs.add(Integer.valueOf(Integer.parseInt(legsId)));
    }
    if (head != null) {
      for (String headId : head)
        _head.add(Integer.valueOf(Integer.parseInt(headId)));
    }
    if (gloves != null) {
      for (String glovesId : gloves)
        _gloves.add(Integer.valueOf(Integer.parseInt(glovesId)));
    }
    if (feet != null) {
      for (String feetId : feet)
        _feet.add(Integer.valueOf(Integer.parseInt(feetId)));
    }
    if (shield != null) {
      for (String shieldId : shield)
        _shield.add(Integer.valueOf(Integer.parseInt(shieldId)));
    }
    if (skills != null) {
      for (String skill : skills)
      {
        StringTokenizer st = new StringTokenizer(skill, "-");
        if (st.hasMoreTokens())
        {
          int skillId = Integer.parseInt(st.nextToken());
          int skillLvl = Integer.parseInt(st.nextToken());
          _skills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
        }

        _skills.add(SkillTable.getInstance().getInfo(3006, 1));
      }
    }
    if (shield_skills != null) {
      for (String skill : shield_skills)
      {
        StringTokenizer st = new StringTokenizer(skill, "-");
        if (!st.hasMoreTokens())
          continue;
        int skillId = Integer.parseInt(st.nextToken());
        int skillLvl = Integer.parseInt(st.nextToken());
        _shieldSkills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
      }
    }

    if (enchant6skills != null)
      for (String skill : enchant6skills)
      {
        StringTokenizer st = new StringTokenizer(skill, "-");
        if (!st.hasMoreTokens())
          continue;
        int skillId = Integer.parseInt(st.nextToken());
        int skillLvl = Integer.parseInt(st.nextToken());
        _enchant6skills.add(SkillTable.getInstance().getInfo(skillId, skillLvl));
      }
  }

  public boolean containAll(Player player)
  {
    Inventory inv = player.getInventory();

    ItemInstance chestItem = inv.getPaperdollItem(10);
    ItemInstance legsItem = inv.getPaperdollItem(11);
    ItemInstance headItem = inv.getPaperdollItem(6);
    ItemInstance glovesItem = inv.getPaperdollItem(9);
    ItemInstance feetItem = inv.getPaperdollItem(12);

    int chest = 0;
    int legs = 0;
    int head = 0;
    int gloves = 0;
    int feet = 0;

    if (chestItem != null)
      legs = chestItem.getItemId();
    if (legsItem != null)
      legs = legsItem.getItemId();
    if (headItem != null)
      head = headItem.getItemId();
    if (glovesItem != null)
      gloves = glovesItem.getItemId();
    if (feetItem != null) {
      feet = feetItem.getItemId();
    }
    return containAll(chest, legs, head, gloves, feet);
  }

  public boolean containAll(int chest, int legs, int head, int gloves, int feet)
  {
    if ((_chest.isEmpty()) && (!_chest.contains(Integer.valueOf(chest))))
      return false;
    if ((!_legs.isEmpty()) && (!_legs.contains(Integer.valueOf(legs))))
      return false;
    if ((!_head.isEmpty()) && (!_head.contains(Integer.valueOf(head))))
      return false;
    if ((!_gloves.isEmpty()) && (!_gloves.contains(Integer.valueOf(gloves)))) {
      return false;
    }
    return (_feet.isEmpty()) || (_feet.contains(Integer.valueOf(feet)));
  }

  public boolean containItem(int slot, int itemId)
  {
    switch (slot)
    {
    case 10:
      return _chest.contains(Integer.valueOf(itemId));
    case 11:
      return _legs.contains(Integer.valueOf(itemId));
    case 6:
      return _head.contains(Integer.valueOf(itemId));
    case 9:
      return _gloves.contains(Integer.valueOf(itemId));
    case 12:
      return _feet.contains(Integer.valueOf(itemId));
    case 7:
    case 8: } return false;
  }

  public int getSetById()
  {
    return _set_id;
  }

  public List<Integer> getChestItemIds()
  {
    return _chest;
  }

  public List<Skill> getSkills()
  {
    return _skills;
  }

  public List<Skill> getShieldSkills()
  {
    return _shieldSkills;
  }

  public List<Skill> getEnchant6skills()
  {
    return _enchant6skills;
  }

  public boolean containShield(Player player)
  {
    Inventory inv = player.getInventory();

    ItemInstance shieldItem = inv.getPaperdollItem(8);

    return (shieldItem != null) && (_shield.contains(Integer.valueOf(shieldItem.getItemId())));
  }

  public boolean containShield(int shield_id)
  {
    if (_shield.isEmpty()) {
      return false;
    }
    return _shield.contains(Integer.valueOf(shield_id));
  }

  public boolean isEnchanted6(Player player)
  {
    if (!containAll(player)) {
      return false;
    }
    Inventory inv = player.getInventory();

    ItemInstance chestItem = inv.getPaperdollItem(10);
    ItemInstance legsItem = inv.getPaperdollItem(11);
    ItemInstance headItem = inv.getPaperdollItem(6);
    ItemInstance glovesItem = inv.getPaperdollItem(9);
    ItemInstance feetItem = inv.getPaperdollItem(12);

    if ((!_chest.isEmpty()) && (chestItem.getEnchantLevel() < 6))
      return false;
    if ((!_legs.isEmpty()) && (legsItem.getEnchantLevel() < 6))
      return false;
    if ((!_gloves.isEmpty()) && (glovesItem.getEnchantLevel() < 6))
      return false;
    if ((!_head.isEmpty()) && (headItem.getEnchantLevel() < 6)) {
      return false;
    }
    return (_feet.isEmpty()) || (feetItem.getEnchantLevel() >= 6);
  }
}