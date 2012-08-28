package l2m.gameserver.model.items.listeners;

import l2m.gameserver.data.xml.holder.OptionDataHolder;
import l2m.gameserver.listener.inventory.OnEquipListener;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.skills.triggers.TriggerInfo;
import l2m.gameserver.templates.OptionDataTemplate;

public final class ItemEnchantOptionsListener
  implements OnEquipListener
{
  private static final ItemEnchantOptionsListener _instance = new ItemEnchantOptionsListener();

  public static ItemEnchantOptionsListener getInstance()
  {
    return _instance;
  }

  public void onEquip(int slot, ItemInstance item, Playable actor)
  {
    if (!item.isEquipable())
      return;
    Player player = actor.getPlayer();

    boolean needSendInfo = false;
    for (int i : item.getEnchantOptions())
    {
      OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
      if (template == null) {
        continue;
      }
      player.addStatFuncs(template.getStatFuncs(template));
      for (Skill skill : template.getSkills())
      {
        player.addSkill(skill, false);
        needSendInfo = true;
      }
      for (TriggerInfo triggerInfo : template.getTriggerList()) {
        player.addTrigger(triggerInfo);
      }
    }
    if (needSendInfo)
      player.sendPacket(new SkillList(player));
    player.sendChanges();
  }

  public void onUnequip(int slot, ItemInstance item, Playable actor)
  {
    if (!item.isEquipable()) {
      return;
    }
    Player player = actor.getPlayer();

    boolean needSendInfo = false;
    for (int i : item.getEnchantOptions())
    {
      OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
      if (template == null) {
        continue;
      }
      player.removeStatsOwner(template);
      for (Skill skill : template.getSkills())
      {
        player.removeSkill(skill, false);
        needSendInfo = true;
      }
      for (TriggerInfo triggerInfo : template.getTriggerList()) {
        player.removeTrigger(triggerInfo);
      }
    }
    if (needSendInfo)
      player.sendPacket(new SkillList(player));
    player.sendChanges();
  }
}