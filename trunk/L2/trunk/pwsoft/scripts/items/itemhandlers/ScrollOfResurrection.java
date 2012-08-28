package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.items.IItemHandler;

public class ScrollOfResurrection
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 737, 3936, 3959, 6387 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }

    L2PcInstance activeChar = (L2PcInstance)playable;
    if (activeChar.isSitting()) {
      activeChar.sendPacket(Static.CANT_MOVE_SITTING);
      return;
    }
    if ((activeChar.isHippy()) || (activeChar.isMovementDisabled())) {
      return;
    }

    L2Object trg = activeChar.getTarget();

    if ((trg != null) && (trg.isL2Character())) {
      L2Character target = (L2Character)trg;
      if (!target.isDead()) {
        activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
        return;
      }

      if (!activeChar.isInsideRadius(target, 600, false, false)) {
        activeChar.sendPacket(Static.TARGET_TOO_FAR);
        return;
      }

      int itemId = item.getItemId();

      boolean humanScroll = (itemId == 3936) || (itemId == 3959) || (itemId == 737);
      boolean petScroll = (itemId == 6387) || (itemId == 737);

      L2PcInstance targetPlayer = null;
      if (target.isPlayer()) {
        targetPlayer = (L2PcInstance)target;
      }

      L2PetInstance targetPet = null;
      if (target.isPet()) {
        targetPet = (L2PetInstance)target;
      }

      if ((targetPlayer != null) || (targetPet != null))
      {
        Castle castle = null;
        if (targetPlayer != null)
          castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
        else {
          castle = CastleManager.getInstance().getCastle(targetPet.getX(), targetPet.getY(), targetPet.getZ());
        }

        if ((castle != null) && (castle.getSiege().getIsInProgress())) {
          activeChar.sendPacket(Static.CANNOT_BE_RESURRECTED_DURING_SIEGE);
          return;
        }

        if (targetPet != null) {
          if (targetPet.getOwner() != activeChar) {
            if (targetPet.getOwner().isReviveRequested()) {
              if (targetPet.getOwner().isRevivingPet())
                activeChar.sendPacket(Static.RES_HAS_ALREADY_BEEN_PROPOSED);
              else {
                activeChar.sendPacket(Static.PET_CANNOT_RES);
              }
              return;
            }
          } else if (!petScroll) {
            activeChar.sendMessage("You do not have the correct scroll");
            return;
          }
        } else {
          if (targetPlayer.isFestivalParticipant())
          {
            activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
            return;
          }
          if (targetPlayer.isReviveRequested()) {
            if (targetPlayer.isRevivingPet())
              activeChar.sendPacket(Static.MASTER_CANNOT_RES);
            else {
              activeChar.sendPacket(Static.RES_HAS_ALREADY_BEEN_PROPOSED);
            }
            return;
          }if (!humanScroll) {
            activeChar.sendMessage("You do not have the correct scroll");
            return;
          }
        }

        if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
          return;
        }

        int skillId = 0;

        switch (itemId) {
        case 737:
          skillId = 2014;
          break;
        case 3936:
          skillId = 2049;
          break;
        case 3959:
          skillId = 2062;
          break;
        case 6387:
          skillId = 2179;
        }

        if (skillId != 0) {
          activeChar.doCast(SkillTable.getInstance().getInfo(skillId, 1));
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
        }
      }
      return;
    }
    activeChar.sendPacket(Static.TARGET_IS_INCORRECT);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}