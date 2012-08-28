package l2p.gameserver.model.items.listeners;

import l2p.gameserver.listener.inventory.OnEquipListener;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;

public final class AccessoryListener
  implements OnEquipListener
{
  private static final AccessoryListener _instance = new AccessoryListener();

  public static AccessoryListener getInstance()
  {
    return _instance;
  }

  public void onUnequip(int slot, ItemInstance item, Playable actor)
  {
    if (!item.isEquipable()) {
      return;
    }
    Player player = (Player)actor;

    if ((item.getBodyPart() == 2097152) && (item.getTemplate().getAttachedSkills().length > 0))
    {
      int agathionId = player.getAgathionId();
      int transformNpcId = player.getTransformationTemplate();
      for (Skill skill : item.getTemplate().getAttachedSkills())
      {
        if ((agathionId > 0) && (skill.getNpcId() == agathionId))
          player.setAgathion(0);
        if ((skill.getNpcId() == transformNpcId) && (skill.getSkillType() == Skill.SkillType.TRANSFORMATION)) {
          player.setTransformation(0);
        }
      }
    }
    if ((item.isAccessory()) || (item.getTemplate().isTalisman()) || (item.getTemplate().isBracelet())) {
      player.sendUserInfo(true);
    }
    else
    {
      player.broadcastCharInfo();
    }
  }

  public void onEquip(int slot, ItemInstance item, Playable actor)
  {
    if (!item.isEquipable()) {
      return;
    }
    Player player = (Player)actor;

    if ((item.isAccessory()) || (item.getTemplate().isTalisman()) || (item.getTemplate().isBracelet())) {
      player.sendUserInfo(true);
    }
    else
    {
      player.broadcastCharInfo();
    }
  }
}