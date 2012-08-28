package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Scrolls
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 3926, 3927, 3928, 3929, 3930, 3931, 3932, 3933, 3934, 3935, 4218, 5593, 5594, 5595, 6037, 5703, 5803, 5804, 5805, 5806, 5807, 8515, 8516, 8517, 8518, 8519, 8520, 8594, 8595, 8596, 8597, 8598, 8599, 8954, 8955, 8956, 9146, 9147, 9148, 9149, 9150, 9151, 9152, 9153, 9154, 9155 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
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
    if (activeChar.isAllSkillsDisabled())
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return;
    }

    int itemId = item.getItemId();

    if ((itemId >= 8594) && (itemId <= 8599))
    {
      if (activeChar.getKarma() > 0) return;

      if (((itemId == 8594) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8595) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8596) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8597) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8598) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8599) && (activeChar.getExpertiseIndex() == 5)))
      {
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
          return;
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2286, 1, 1, 0));
        activeChar.reduceDeathPenaltyBuffLevel();
        useScroll(activeChar, 2286, itemId - 8593);
      }
      else {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE));
      }return;
    }
    if ((itemId == 5703) || ((itemId >= 5803) && (itemId <= 5807)))
    {
      if (((itemId == 5703) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 5803) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 5804) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 5805) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 5806) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 5807) && (activeChar.getExpertiseIndex() == 5)))
      {
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
          return;
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2168, activeChar.getExpertiseIndex() + 1, 1, 0));
        useScroll(activeChar, 2168, activeChar.getExpertiseIndex() + 1);
        activeChar.setCharmOfLuck(true);
      }
      else {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE));
      }return;
    }
    if ((itemId >= 8515) && (itemId <= 8520))
    {
      if (((itemId == 8515) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8516) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8517) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8518) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8519) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8520) && (activeChar.getExpertiseIndex() == 5)))
      {
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
          return;
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 5041, 1, 1, 0));
        useScroll(activeChar, 5041, 1);
        activeChar.setCharmOfCourage(true);
      }
      else {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.INCOMPATIBLE_ITEM_GRADE));
      }return;
    }
    if ((itemId >= 8954) && (itemId <= 8956))
    {
      if (activeChar.getLevel() < 76) return;
      if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
        return;
      switch (itemId)
      {
      case 8954:
        activeChar.sendPacket(new MagicSkillUser(playable, playable, 2306, 1, 1, 0));
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2306, 1, 1, 0));
        activeChar.addExpAndSp(0L, 50000);
        break;
      case 8955:
        activeChar.sendPacket(new MagicSkillUser(playable, playable, 2306, 2, 1, 0));
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2306, 2, 1, 0));
        activeChar.addExpAndSp(0L, 100000);
        break;
      case 8956:
        activeChar.sendPacket(new MagicSkillUser(playable, playable, 2306, 3, 1, 0));
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2306, 3, 1, 0));
        activeChar.addExpAndSp(0L, 200000);
        break;
      }

      return;
    }

    if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
      return;
    }
    switch (itemId)
    {
    case 3926:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2050, 1, 1, 0));
      useScroll(activeChar, 2050, 1);
      break;
    case 3927:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2051, 1, 1, 0));
      useScroll(activeChar, 2051, 1);
      break;
    case 3928:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2052, 1, 1, 0));
      useScroll(activeChar, 2052, 1);
      break;
    case 3929:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2053, 1, 1, 0));
      useScroll(activeChar, 2053, 1);
      break;
    case 3930:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2054, 1, 1, 0));
      useScroll(activeChar, 2054, 1);
      break;
    case 3931:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2055, 1, 1, 0));
      useScroll(activeChar, 2055, 1);
      break;
    case 3932:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2056, 1, 1, 0));
      useScroll(activeChar, 2056, 1);
      break;
    case 3933:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2057, 1, 1, 0));
      useScroll(activeChar, 2057, 1);
      break;
    case 3934:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2058, 1, 1, 0));
      useScroll(activeChar, 2058, 1);
      break;
    case 3935:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2059, 1, 1, 0));
      useScroll(activeChar, 2059, 1);
      break;
    case 4218:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2064, 1, 1, 0));
      useScroll(activeChar, 2064, 1);
      break;
    case 5593:
      activeChar.sendPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.addExpAndSp(0L, 500);
      break;
    case 5594:
      activeChar.sendPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.addExpAndSp(0L, 5000);
      break;
    case 5595:
      activeChar.sendPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
      activeChar.addExpAndSp(0L, 100000);
      break;
    case 6037:
      activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2170, 1, 1, 0));
      useScroll(activeChar, 2170, 1);
      break;
    case 9146:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2050, 1, 1, 0));
      useScroll(activeChar, 2050, 1);
      break;
    case 9147:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2051, 1, 1, 0));
      useScroll(activeChar, 2051, 1);
      break;
    case 9148:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2052, 1, 1, 0));
      useScroll(activeChar, 2052, 1);
      break;
    case 9149:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2053, 1, 1, 0));
      useScroll(activeChar, 2053, 1);
      break;
    case 9150:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2054, 1, 1, 0));
      useScroll(activeChar, 2054, 1);
      break;
    case 9151:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2055, 1, 1, 0));
      useScroll(activeChar, 2055, 1);
      break;
    case 9152:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2056, 1, 1, 0));
      useScroll(activeChar, 2056, 1);
      break;
    case 9153:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2057, 1, 1, 0));
      useScroll(activeChar, 2057, 1);
      break;
    case 9154:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2058, 1, 1, 0));
      useScroll(activeChar, 2058, 1);
      break;
    case 9155:
      activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2059, 1, 1, 0));
      useScroll(activeChar, 2059, 1);
      break;
    }
  }

  public void useScroll(L2PcInstance activeChar, int magicId, int level)
  {
    L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
    if (skill != null)
      activeChar.doCast(skill);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}