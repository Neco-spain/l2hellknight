package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.instancemanager.PetitionManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.Say2;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.data.tables.GmListTable;

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