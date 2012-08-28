package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.CommandChannel;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.model.World;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExAskJoinMPCC;
import l2m.gameserver.network.serverpackets.SystemMessage;

public class RequestExMPCCAskJoin extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isProcessingRequest())
    {
      activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
      return;
    }

    if (!activeChar.isInParty())
    {
      activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
      return;
    }

    Player target = World.getPlayer(_name);

    if (target == null)
    {
      activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
      return;
    }

    if ((activeChar == target) || (!target.isInParty()) || (activeChar.getParty() == target.getParty()))
    {
      activeChar.sendPacket(Msg.YOU_HAVE_INVITED_WRONG_TARGET);
      return;
    }

    if ((target.isInParty()) && (!target.getParty().isLeader(target))) {
      target = target.getParty().getPartyLeader();
    }
    if (target == null)
    {
      activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
      return;
    }

    if (target.getParty().isInCommandChannel())
    {
      activeChar.sendPacket(new SystemMessage(1594).addString(target.getName()));
      return;
    }

    if (target.isBusy())
    {
      activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
      return;
    }

    Party activeParty = activeChar.getParty();

    if (activeParty.isInCommandChannel())
    {
      if (activeParty.getCommandChannel().getChannelLeader() != activeChar)
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
        return;
      }

      sendInvite(activeChar, target);
    }
    else if (CommandChannel.checkAuthority(activeChar)) {
      sendInvite(activeChar, target);
    }
  }

  private void sendInvite(Player requestor, Player target) {
    new Request(Request.L2RequestType.CHANNEL, requestor, target).setTimeout(10000L);
    target.sendPacket(new ExAskJoinMPCC(requestor.getName()));
    requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
  }
}