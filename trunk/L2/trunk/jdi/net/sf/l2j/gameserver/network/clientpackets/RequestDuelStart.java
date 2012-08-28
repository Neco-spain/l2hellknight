package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelAskStart;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelStart extends L2GameClientPacket
{
  private static final String _C__D0_27_REQUESTDUELSTART = "[C] D0:27 RequestDuelStart";
  private static Logger _log = Logger.getLogger(RequestDuelStart.class.getName());
  private String _player;
  private int _partyDuel;

  protected void readImpl()
  {
    _player = readS();
    _partyDuel = readD();
  }

  protected void runImpl()
  {
    if (!Config.DUEL_ALLOW) return;
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2PcInstance targetChar = L2World.getInstance().getPlayer(_player);
    if (activeChar == null)
      return;
    if (targetChar == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL));
      return;
    }
    if (activeChar == targetChar)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL));
      return;
    }

    if (!activeChar.canDuel())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME));
      return;
    }
    if (!targetChar.canDuel())
    {
      activeChar.sendPacket(targetChar.getNoDuelReason());
      return;
    }

    if (!activeChar.isInsideRadius(targetChar, 250, false, false))
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY);
      msg.addString(targetChar.getName());
      activeChar.sendPacket(msg);
      return;
    }

    if (_partyDuel == 1)
    {
      if ((!activeChar.isInParty()) || (!activeChar.isInParty()) || (!activeChar.getParty().isLeader(activeChar)))
      {
        activeChar.sendMessage("You have to be the leader of a party in order to request a party duel.");
        return;
      }

      if (!targetChar.isInParty())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY));
        return;
      }

      if (activeChar.getParty().getPartyMembers().contains(targetChar))
      {
        activeChar.sendMessage("This player is a member of your own party.");
        return;
      }

      for (L2PcInstance temp : activeChar.getParty().getPartyMembers())
      {
        if (!temp.canDuel())
        {
          activeChar.sendMessage("Not all the members of your party are ready for a duel.");
          return;
        }
      }
      L2PcInstance partyLeader = null;
      for (L2PcInstance temp : targetChar.getParty().getPartyMembers())
      {
        if (partyLeader == null) partyLeader = temp;
        if (!temp.canDuel())
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL));
          return;
        }

      }

      if (!partyLeader.isProcessingRequest())
      {
        activeChar.onTransactionRequest(partyLeader);
        partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));

        if (Config.DEBUG) {
          _log.fine(activeChar.getName() + " requested a duel with " + partyLeader.getName());
        }
        SystemMessage msg = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
        msg.addString(partyLeader.getName());
        activeChar.sendPacket(msg);

        msg = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
        msg.addString(activeChar.getName());
        targetChar.sendPacket(msg);
      }
      else
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
        msg.addString(partyLeader.getName());
        activeChar.sendPacket(msg);
      }

    }
    else if (!targetChar.isProcessingRequest())
    {
      activeChar.onTransactionRequest(targetChar);
      targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));

      if (Config.DEBUG) {
        _log.fine(activeChar.getName() + " requested a duel with " + targetChar.getName());
      }
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
      msg.addString(targetChar.getName());
      activeChar.sendPacket(msg);

      msg = new SystemMessage(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL);
      msg.addString(activeChar.getName());
      targetChar.sendPacket(msg);
    }
    else
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
      msg.addString(targetChar.getName());
      activeChar.sendPacket(msg);
    }
  }

  public String getType()
  {
    return "[C] D0:27 RequestDuelStart";
  }
}