package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetitionCancel extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (PetitionManager.getInstance().isPlayerInConsultation(player))
    {
      if (player.isGM())
        PetitionManager.getInstance().endActivePetition(player);
      else {
        player.sendPacket(Static.PETITION_UNDER_PROCESS);
      }

    }
    else if (PetitionManager.getInstance().isPlayerPetitionPending(player))
    {
      if (PetitionManager.getInstance().cancelActivePetition(player))
      {
        int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(player);

        player.sendPacket(SystemMessage.id(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY).addString(String.valueOf(numRemaining)));

        String msgContent = player.getName() + " has canceled a pending petition.";
        GmListTable.broadcastToGMs(new CreatureSay(player.getObjectId(), 17, "Petition System", msgContent));
      }
      else {
        player.sendPacket(Static.FAILED_CANCEL_PETITION_TRY_LATER);
      }
    }
    else player.sendPacket(Static.PETITION_NOT_SUBMITTED);
  }

  public String getType()
  {
    return "[C] PetitionCancel";
  }
}