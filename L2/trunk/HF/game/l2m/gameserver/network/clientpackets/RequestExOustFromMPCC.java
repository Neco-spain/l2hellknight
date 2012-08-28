package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.CommandChannel;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;

public class RequestExOustFromMPCC extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (!activeChar.isInParty()) || (!activeChar.getParty().isInCommandChannel())) {
      return;
    }
    Player target = World.getPlayer(_name);

    if (target == null)
    {
      activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
      return;
    }

    if (activeChar == target) {
      return;
    }

    if ((!target.isInParty()) || (!target.getParty().isInCommandChannel()) || (activeChar.getParty().getCommandChannel() != target.getParty().getCommandChannel()))
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    if (activeChar.getParty().getCommandChannel().getChannelLeader() != activeChar)
    {
      activeChar.sendPacket(Msg.ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND);
      return;
    }

    target.getParty().getCommandChannel().getChannelLeader().sendPacket(new SystemMessage(1584).addString(target.getName()));
    target.getParty().getCommandChannel().removeParty(target.getParty());
    target.getParty().broadCast(new IStaticPacket[] { Msg.YOU_WERE_DISMISSED_FROM_THE_COMMAND_CHANNEL });
  }
}