package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCharge;

public class EnergyStone
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5589 };
  private EffectCharge _effect;
  private L2SkillCharge _skill;

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance activeChar;
    if ((playable instanceof L2PcInstance))
    {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if ((playable instanceof L2PetInstance))
      {
        activeChar = ((L2PetInstance)playable).getOwner();
      }
      else return;
    }
    L2PcInstance activeChar;
    if (item.getItemId() != 5589) return;
    int classid = activeChar.getClassId().getId();

    if ((classid == 2) || (classid == 48) || (classid == 88) || (classid == 114))
    {
      if (activeChar.isAllSkillsDisabled())
      {
        ActionFailed af = new ActionFailed();
        activeChar.sendPacket(af);
        return;
      }

      if (activeChar.isSitting())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
        return;
      }

      _skill = getChargeSkill(activeChar);
      if (_skill == null)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
        sm.addItemName(5589);
        activeChar.sendPacket(sm);
        return;
      }

      _effect = activeChar.getChargeEffect();

      if (_effect == null)
      {
        L2Skill dummy = SkillTable.getInstance().getInfo(_skill.getId(), _skill.getLevel());
        if (dummy != null)
        {
          dummy.getEffects(activeChar, activeChar);
          MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, _skill.getId(), 1, 1, 0);
          activeChar.sendPacket(MSU);
          activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
          return;
        }
        return;
      }
      if (_effect.numCharges < 2)
      {
        MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, _skill.getId(), 1, 1, 0);
        activeChar.sendPacket(MSU);
        activeChar.broadcastPacket(MSU);
        _effect.addNumCharges(1);
        activeChar.sendPacket(new EtcStatusUpdate(activeChar));
        SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
        sm.addNumber(_effect.getLevel());
        activeChar.sendPacket(sm);
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
      }
      else
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
      }

      return;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
    sm.addItemName(5589);
    activeChar.sendPacket(sm);
  }

  private L2SkillCharge getChargeSkill(L2PcInstance activeChar)
  {
    L2Skill[] skills = activeChar.getAllSkills();
    for (L2Skill s : skills) {
      if ((s.getId() == 50) || (s.getId() == 8)) {
        return (L2SkillCharge)s;
      }
    }
    return null;
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}