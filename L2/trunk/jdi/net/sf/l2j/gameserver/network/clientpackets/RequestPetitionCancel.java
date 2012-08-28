package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetitionCancel extends L2GameClientPacket
{
  private static final String _C__80_REQUEST_PETITIONCANCEL = "[C] 80 RequestPetitionCancel";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
    {
      if (activeChar.isGM())
        PetitionManager.getInstance().endActivePetition(activeChar);
      else {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
      }

    }
    else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
    {
      if (PetitionManager.getInstance().cancelActivePetition(activeChar))
      {
        int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);

        SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY);
        sm.addString(String.valueOf(numRemaining));
        activeChar.sendPacket(sm);
        sm = null;

        String msgContent = activeChar.getName() + " has canceled a pending petition.";
        GmListTable.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), 17, "Petition System", msgContent));
      }
      else
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
      }
    }
    else
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_NOT_SUBMITTED));
    }
  }

  public String getType()
  {
    return "[C] 80 RequestPetitionCancel";
  }
}