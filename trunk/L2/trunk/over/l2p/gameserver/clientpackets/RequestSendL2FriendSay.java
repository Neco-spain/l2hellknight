package l2p.gameserver.clientpackets;

import java.util.Map;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.actor.instances.player.FriendList;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.L2FriendSay;
import l2p.gameserver.utils.Log;

public class RequestSendL2FriendSay extends L2GameClientPacket
{
  private String _message;
  private String _reciever;

  protected void readImpl()
  {
    _message = readS(2048);
    _reciever = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.getNoChannel() != 0L)
    {
      if ((activeChar.getNoChannelRemained() > 0L) || (activeChar.getNoChannel() < 0L))
      {
        activeChar.sendPacket(Msg.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER);
        return;
      }
      activeChar.updateNoChannel(0L);
    }

    Player targetPlayer = World.getPlayer(_reciever);
    if (targetPlayer == null)
    {
      activeChar.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
      return;
    }

    if (targetPlayer.isBlockAll())
    {
      activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
      return;
    }

    if (!activeChar.getFriendList().getList().containsKey(Integer.valueOf(targetPlayer.getObjectId()))) {
      return;
    }
    Log.LogChat("FRIENDTELL", activeChar.getName(), _reciever, _message);

    L2FriendSay frm = new L2FriendSay(activeChar.getName(), _reciever, _message);
    targetPlayer.sendPacket(frm);
  }
}