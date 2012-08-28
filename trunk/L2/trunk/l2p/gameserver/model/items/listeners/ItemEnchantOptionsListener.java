package l2p.gameserver.model.items.listeners;

import l2p.gameserver.data.xml.holder.OptionDataHolder;
import l2p.gameserver.listener.inventory.OnEquipListener;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.stats.triggers.TriggerInfo;
import l2p.gameserver.templates.OptionDataTemplate;

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