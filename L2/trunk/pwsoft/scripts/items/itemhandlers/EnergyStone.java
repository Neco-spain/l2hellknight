package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCharge;
import scripts.items.IItemHandler;

public class EnergyStone
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5589 };
  private EffectCharge _effect;
  private L2SkillCharge _skill;

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar = null;
    if (playable.isPlayer())
      activeChar = (L2PcInstance)playable;
    else if (playable.isPet()) {
      activeChar = ((L2PetInstance)playable).getOwner();
    }
    if (activeChar == null) {
      return;
    }
    if (item.getItemId() != 5589) {
      return;
    }
    switch (activeChar.getClassId().getId())
    {
    case 2:
    case 48:
    case 88:
    case 114:
      if (activeChar.isAllSkillsDisabled())
      {
        activeChar.sendActionFailed();
        return;
      }

      if (activeChar.isSitting())
      {
        activeChar.sendPacket(Static.CANT_MOVE_SITTING);
        return;
      }

      _skill = getChargeSkill(activeChar);
      if (_skill == null)
      {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addItemName(5589));
        return;
      }

      if (activeChar.getCharges() < 2)
      {
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
        activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, _skill.getId(), 1, 1, 0));
        activeChar.increaseCharges();
      }
      else {
        activeChar.sendPacket(Static.FORCE_MAXLEVEL_REACHED);
      }break;
    default:
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addItemName(5589));
    }
  }

  private L2SkillCharge getChargeSkill(L2PcInstance activeChar)
  {
    L2Skill[] skills = activeChar.getAllSkills();
    for (L2Skill s : skills)
    {
      if (s == null) {
        continue;
      }
      if ((s.getId() == 50) || (s.getId() == 8))
        return (L2SkillCharge)s;
    }
    return null;
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}