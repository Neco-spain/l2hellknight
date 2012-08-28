package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetition extends L2GameClientPacket
{
  private String _content;
  private int _type;

  protected void readImpl()
  {
    try
    {
      _content = readS();
      _type = readD();
    }
    catch (BufferUnderflowException e)
    {
      _content = "n-no";
    }
  }

  protected void runImpl()
  {
    if (_content.equalsIgnoreCase("n-no")) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (!GmListTable.getInstance().isGmOnline(false))
    {
      player.sendPacket(Static.NO_GM_PROVIDING_SERVICE_NOW);
      return;
    }

    if (!PetitionManager.getInstance().isPetitioningAllowed())
    {
      player.sendPacket(Static.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER);
      return;
    }

    if (PetitionManager.getInstance().isPlayerPetitionPending(player))
    {
      player.sendPacket(Static.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
      return;
    }

    if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
    {
      player.sendPacket(Static.PETITION_SYSTEM_CURRENT_UNAVAILABLE);
      return;
    }

    int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(player) + 1;

    if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
    {
      player.sendPacket(SystemMessage.id(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY).addNumber(totalPetitions));
      return;
    }

    if (_content.length() > 255)
    {
      player.sendPacket(Static.PETITION_MAX_CHARS_255);
      return;
    }

    int petitionId = PetitionManager.getInstance().submitPetition(player, _content, _type);

    player.sendPacket(SystemMessage.id(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(petitionId));
    player.sendPacket(SystemMessage.id(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT).addNumber(totalPetitions).addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions));
    player.sendPacket(SystemMessage.id(SystemMessageId.S1_PETITION_ON_WAITING_LIST).addNumber(PetitionManager.getInstance().getPendingPetitionCount()));
  }
}