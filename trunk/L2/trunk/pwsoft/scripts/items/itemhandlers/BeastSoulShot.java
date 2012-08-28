package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.util.Broadcast;
import scripts.items.IItemHandler;

public class BeastSoulShot
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6645 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item) {
    if (playable == null) {
      return;
    }

    L2PcInstance activeOwner = null;
    if ((playable instanceof L2Summon)) {
      activeOwner = ((L2Summon)playable).getOwner();
      activeOwner.sendPacket(Static.PET_CANNOT_USE_ITEM);
      return;
    }if (playable.isPlayer()) {
      activeOwner = (L2PcInstance)playable;
    }

    if (activeOwner == null) {
      return;
    }
    L2Summon activePet = activeOwner.getPet();

    if (activePet == null) {
      activeOwner.sendPacket(Static.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
      return;
    }

    if (activePet.isDead()) {
      activeOwner.sendPacket(Static.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
      return;
    }

    if (activePet.getChargedSoulShot() != 0) {
      return;
    }

    activePet.setChargedSoulShot(1);

    activeOwner.sendPacket(Static.PET_USE_THE_POWER_OF_SPIRIT);

    Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, 2033, 1, 0, 0), 360000L);
  }

  public int[] getItemIds() {
    return ITEM_IDS;
  }
}