package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class CastPotions
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 65, 725, 726, 727, 728, 734, 735, 1060, 1061, 1062, 1073, 1374, 1375, 1539, 1540, 5591, 5592, 6035, 6036, 8622, 8623, 8624, 8625, 8626, 8627, 8628, 8629, 8630, 8631, 8632, 8633, 8634, 8635, 8636, 8637, 8638, 8639, 10000, 10001, 10002, 10003 };

  public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if ((playable instanceof L2PetInstance))
      return;
    L2PcInstance activeChar;
    if ((playable instanceof L2PcInstance)) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if ((playable instanceof L2PetInstance))
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(activeChar.getObjectId())) && (!Config.TVT_EVENT_POTIONS_ALLOWED))
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return;
    }

    if (activeChar.isPotionsDisabled())
    {
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    int itemId = item.getItemId();

    switch (itemId)
    {
    case 65:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2001, 1);
      return;
    case 725:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2002, 1);
      return;
    case 726:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.MANA_HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 9000, 1);
      return;
    case 727:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2032, 1);
      return;
    case 728:
      activeChar.setCurrentMp(activeChar.getCurrentMp() + Config.MP_RESTORE);
      StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
      su.addAttribute(11, (int)activeChar.getCurrentMp());
      activeChar.sendPacket(su);
      MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2005, 1, 0, 0);
      activeChar.broadcastPacket(MSU);
      SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1);
      sm1.addItemName(itemId);
      activeChar.sendPacket(sm1);
      activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
      return;
    case 734:
      ResPotionCast(activeChar, item, 2011, 1);
      return;
    case 735:
      ResPotionCast(activeChar, item, 2012, 1);
      return;
    case 1060:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2031, 1);
      return;
    case 1073:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2031, 1);
      return;
    case 1061:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2032, 1);
      return;
    case 1062:
      ResPotionCast(activeChar, item, 2033, 1);
      return;
    case 1374:
      ResPotionCast(activeChar, item, 2034, 1);
      return;
    case 1375:
      ResPotionCast(activeChar, item, 2035, 1);
      return;
    case 1539:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId))
        return;
      ResPotionCast(activeChar, item, 2037, 1);
      return;
    case 1540:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2038, 1);
      return;
    case 5591:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2166, 1);
      return;
    case 5592:
      if (!isEffectReplaceable(activeChar, L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME, itemId)) return;
      ResPotionCast(activeChar, item, 2166, 2);
      return;
    case 6035:
      ResPotionCast(activeChar, item, 2169, 1);
      return;
    case 6036:
      ResPotionCast(activeChar, item, 2169, 2);
      return;
    case 8622:
    case 8623:
    case 8624:
    case 8625:
    case 8626:
    case 8627:
      if (((itemId == 8622) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8623) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8624) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8625) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8626) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8627) && (activeChar.getExpertiseIndex() == 5)))
      {
        ResPotionCast(activeChar, item, 2287, activeChar.getExpertiseIndex() + 1);
        return;
      }

      SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE);
      sm.addItemName(itemId);
      activeChar.sendPacket(sm);
      return;
    case 8628:
    case 8629:
    case 8630:
    case 8631:
    case 8632:
    case 8633:
      if (((itemId == 8628) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8629) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8630) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8631) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8632) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8633) && (activeChar.getExpertiseIndex() == 5)))
      {
        ResPotionCast(activeChar, item, 2288, activeChar.getExpertiseIndex() + 1);
        return;
      }

      SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE);
      sm.addItemName(itemId);
      activeChar.sendPacket(sm);
      return;
    case 8634:
    case 8635:
    case 8636:
    case 8637:
    case 8638:
    case 8639:
      if (((itemId == 8634) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8635) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8636) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8637) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8638) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8639) && (activeChar.getExpertiseIndex() == 5)))
      {
        ResPotionCast(activeChar, item, 2289, activeChar.getExpertiseIndex() + 1);
        return;
      }

      SystemMessage sm = new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE);
      sm.addItemName(itemId);
      activeChar.sendPacket(sm);
      return;
    case 10000:
      L2Skill skill = SkillTable.getInstance().getInfo(4699, 13);
      activeChar.stopSkillEffects(skill.getId());
      skill.getEffects(activeChar, activeChar);
      MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 4352, 1, 0, 0);
      activeChar.broadcastPacket(MSU);

      SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
      sm.addSkillName(skill.getId());
      activeChar.sendPacket(sm);

      return;
    case 10001:
      L2Skill skill = SkillTable.getInstance().getInfo(1355, 1);
      activeChar.stopSkillEffects(skill.getId());
      skill.getEffects(activeChar, activeChar);
      MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 4352, 1, 0, 0);
      activeChar.broadcastPacket(MSU);

      SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
      sm.addSkillName(skill.getId());
      activeChar.sendPacket(sm);

      return;
    case 10002:
      L2Skill skill = SkillTable.getInstance().getInfo(365, 1);
      activeChar.stopSkillEffects(skill.getId());
      skill.getEffects(activeChar, activeChar);
      MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 4352, 1, 0, 0);
      activeChar.broadcastPacket(MSU);

      SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
      sm.addSkillName(skill.getId());
      activeChar.sendPacket(sm);

      return;
    case 10003:
      L2Skill skill = SkillTable.getInstance().getInfo(1363, 1);
      activeChar.stopSkillEffects(skill.getId());
      skill.getEffects(activeChar, activeChar);
      MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 4352, 1, 0, 0);
      activeChar.broadcastPacket(MSU);

      SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
      sm.addSkillName(skill.getId());
      activeChar.sendPacket(sm);

      return;
    }
  }

  private boolean isEffectReplaceable(L2PcInstance activeChar, Enum effectType, int itemId)
  {
    L2Effect[] effects = activeChar.getAllEffects();

    if (effects == null) {
      return true;
    }
    for (L2Effect e : effects)
    {
      if ((e.getEffectType() != effectType) || (!e.getSkill().isPotion()))
        continue;
      if (e.getTaskTime() > e.getSkill().getBuffDuration() * 67 / 100000)
        return true;
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
      sm.addItemName(itemId);
      activeChar.sendPacket(sm);
      return false;
    }

    return true;
  }

  public void ResPotionCast(L2PcInstance activeChar, L2ItemInstance item, int skillId, int skillLvl)
  {
    L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
    int reuse = skill.getReuseDelay();
    L2Skill.SkillType type = skill.getSkillType();
    if (activeChar.isSkillDisabled(skill))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
      sm.addSkillName(skillId, skillLvl);
      activeChar.sendPacket(sm);
      return;
    }

    if (type == L2Skill.SkillType.MANAHEAL)
    {
      activeChar.setCurrentMp(skill.getPower() + activeChar.getCurrentMp());
      StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
      su.addAttribute(11, (int)activeChar.getCurrentMp());
      activeChar.sendPacket(su);
    }
    else if ((type == L2Skill.SkillType.HEAL) || (type == L2Skill.SkillType.HEAL_STATIC))
    {
      activeChar.setCurrentHp(skill.getPower() + activeChar.getCurrentHp());
      StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
      su.addAttribute(9, (int)activeChar.getCurrentHp());
      activeChar.sendPacket(su);
    }
    else if (type == L2Skill.SkillType.COMBATPOINTHEAL)
    {
      activeChar.setCurrentCp(skill.getPower() + activeChar.getCurrentCp());
      StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
      su.addAttribute(33, (int)activeChar.getCurrentCp());
      activeChar.sendPacket(su);
    }
    else
    {
      activeChar.stopSkillEffects(skillId);
      skill.getEffects(activeChar, activeChar);
    }

    if (reuse > 10)
    {
      activeChar.sendPacket(new ExUseSharedGroupItem(item.getItemId(), skill.getReuseGroupId(), reuse, reuse));
      if (reuse > 299999)
      {
        if (skill.getReuseGroupId() > 0)
        {
          for (L2Skill sk : skill.getReuseGroup()) {
            if (!activeChar.isSkillDisabled(sk))
              activeChar.disableSkill(sk.getId(), reuse);
          }
        }
      }
      else {
        activeChar.disableSkill(skillId, reuse);
      }
    }

    MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, skill.getId(), skill.getLevel(), 0, 0);
    activeChar.broadcastPacket(MSU);

    SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
    sm.addItemName(item.getItemId());
    activeChar.sendPacket(sm);

    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}