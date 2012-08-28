package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ScrollOfResurrection
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 737, 3936, 3959, 6387 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;

    L2PcInstance activeChar = (L2PcInstance)playable;
    if (activeChar.isSitting())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
      return;
    }
    if ((activeChar.isInOlympiadMode()) || (activeChar.inObserverMode()))
    {
      activeChar.sendMessage("You Cannot Use This Item In Olympiad Mode!");
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if (activeChar.isMovementDisabled()) return;

    int itemId = item.getItemId();

    boolean humanScroll = (itemId == 3936) || (itemId == 3959) || (itemId == 737);
    boolean petScroll = (itemId == 6387) || (itemId == 737);

    L2Character target = (L2Character)activeChar.getTarget();

    if ((target != null) && (target.isDead()))
    {
      L2PcInstance targetPlayer = null;

      if ((target instanceof L2PcInstance)) {
        targetPlayer = (L2PcInstance)target;
      }
      L2PetInstance targetPet = null;

      if ((target instanceof L2PetInstance)) {
        targetPet = (L2PetInstance)target;
      }
      if ((targetPlayer != null) || (targetPet != null))
      {
        boolean condGood = true;

        Castle castle = null;

        if (targetPlayer != null)
          castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
        else {
          castle = CastleManager.getInstance().getCastle(targetPet.getX(), targetPet.getY(), targetPet.getZ());
        }
        if ((castle != null) && (castle.getSiege().getIsInProgress()))
        {
          condGood = false;
          activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
        }

        if (targetPet != null)
        {
          if (targetPet.getOwner() != activeChar)
          {
            if (targetPet.getOwner().isReviveRequested())
            {
              if (targetPet.getOwner().isRevivingPet())
                activeChar.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED));
              else
                activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_RES));
              condGood = false;
            }
          }
          else if (!petScroll)
          {
            condGood = false;
            activeChar.sendMessage("You do not have the correct scroll");
          }
        }
        else
        {
          if (targetPlayer.isFestivalParticipant())
          {
            condGood = false;
            activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
          }
          if (targetPlayer.isReviveRequested())
          {
            if (targetPlayer.isRevivingPet())
              activeChar.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES));
            else
              activeChar.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED));
            condGood = false;
          }
          else if (!humanScroll)
          {
            condGood = false;
            activeChar.sendMessage("You do not have the correct scroll");
          }
        }

        if (condGood)
        {
          if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
            return;
          }
          int skillId = 0;
          int skillLevel = 1;

          switch (itemId) { case 737:
            skillId = 2014; break;
          case 3936:
            skillId = 2049; break;
          case 3959:
            skillId = 2062; break;
          case 6387:
            skillId = 2179;
          }

          if (skillId != 0)
          {
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
            activeChar.useMagic(skill, true, true);

            SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(itemId);
            activeChar.sendPacket(sm);
          }
        }
      }
    }
    else
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}