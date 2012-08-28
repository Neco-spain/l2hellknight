package l2p.gameserver.model.items.listeners;

import l2p.gameserver.listener.inventory.OnEquipListener;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.SkillCoolTime;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.item.ItemTemplate;

public final class ItemSkillsListener
  implements OnEquipListener
{
  private static final ItemSkillsListener _instance = new ItemSkillsListener();

  public static ItemSkillsListener getInstance()
  {
    return _instance;
  }

  public void onUnequip(int slot, ItemInstance item, Playable actor)
  {
    Player player = (Player)actor;

    Skill[] itemSkills = null;
    Skill enchant4Skill = null;

    ItemTemplate it = item.getTemplate();

    itemSkills = it.getAttachedSkills();

    enchant4Skill = it.getEnchant4Skill();

    player.removeTriggers(it);

    if ((itemSkills != null) && (itemSkills.length > 0)) {
      for (Skill itemSkill : itemSkills)
        if ((itemSkill.getId() >= 26046) && (itemSkill.getId() <= 26048))
        {
          int level = player.getSkillLevel(Integer.valueOf(itemSkill.getId()));
          int newlevel = level - 1;
          if (newlevel > 0)
            player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
          else
            player.removeSkillById(Integer.valueOf(itemSkill.getId()));
        }
        else
        {
          player.removeSkill(itemSkill, false);
        }
    }
    if (enchant4Skill != null) {
      player.removeSkill(enchant4Skill, false);
    }
    if ((itemSkills.length > 0) || (enchant4Skill != null))
    {
      player.sendPacket(new SkillList(player));
      player.updateStats();
    }
  }

  public void onEquip(int slot, ItemInstance item, Playable actor)
  {
    Player player = (Player)actor;

    Skill[] itemSkills = null;
    Skill enchant4Skill = null;

    ItemTemplate it = item.getTemplate();

    itemSkills = it.getAttachedSkills();

    if (item.getEnchantLevel() >= 4) {
      enchant4Skill = it.getEnchant4Skill();
    }

    if ((it.getType2() == 0) && (player.getWeaponsExpertisePenalty() > 0)) {
      return;
    }
    player.addTriggers(it);

    boolean needSendInfo = false;
    if (itemSkills.length > 0) {
      for (Skill itemSkill : itemSkills) {
        if ((itemSkill.getId() >= 26046) && (itemSkill.getId() <= 26048))
        {
          int level = player.getSkillLevel(Integer.valueOf(itemSkill.getId()));
          int newlevel = level;
          if (level > 0)
          {
            if (SkillTable.getInstance().getInfo(itemSkill.getId(), level + 1) != null)
              newlevel = level + 1;
          }
          else
            newlevel = 1;
          if (newlevel != level)
          {
            player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
          }
        } else {
          if (player.getSkillLevel(Integer.valueOf(itemSkill.getId())) >= itemSkill.getLevel())
            continue;
          player.addSkill(itemSkill, false);

          if (!itemSkill.isActive())
            continue;
          long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
          reuseDelay = Math.min(reuseDelay, 30000L);

          if ((reuseDelay <= 0L) || (player.isSkillDisabled(itemSkill)))
            continue;
          player.disableSkill(itemSkill, reuseDelay);
          needSendInfo = true;
        }
      }
    }

    if (enchant4Skill != null) {
      player.addSkill(enchant4Skill, false);
    }
    if ((itemSkills.length > 0) || (enchant4Skill != null))
    {
      player.sendPacket(new SkillList(player));
      player.updateStats();
      if (needSendInfo)
        player.sendPacket(new SkillCoolTime(player));
    }
  }
}