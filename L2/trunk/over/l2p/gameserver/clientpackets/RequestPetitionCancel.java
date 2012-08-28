package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.tables.GmListTable;

public final class RequestPetitionCancel extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
    {
      if (activeChar.isGM())
        PetitionManager.getInstance().endActivePetition(activeChar);
      else
        activeChar.sendPacket(new SystemMessage(407));
    }
    else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
    {
      if (PetitionManager.getInstance().cancelActivePetition(activeChar))
      {
        int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);

        activeChar.sendPacket(new SystemMessage(736).addString(String.valueOf(numRemaining)));

        String msgContent = activeChar.getName() + " has canceled a pending petition.";
        GmListTable.broadcastToGMs(new Say2(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
      }
      else {
        activeChar.sendPacket(new SystemMessage(393));
      }
    }
    else activeChar.sendPacket(new SystemMessage(738));
  }
}