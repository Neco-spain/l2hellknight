package l2m.gameserver.network.clientpackets;

import l2m.gameserver.listener.actor.player.OnAnswerListener;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import org.apache.commons.lang3.tuple.Pair;

public class ConfirmDlg extends L2GameClientPacket
{
  private int _answer;
  private int _requestId;

  protected void readImpl()
  {
    readD();
    _answer = readD();
    _requestId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Pair entry = activeChar.getAskListener(true);
    if ((entry == null) || (((Integer)entry.getKey()).intValue() != _requestId)) {
      return;
    }
    OnAnswerListener listener = (OnAnswerListener)entry.getValue();
    if (_answer == 1)
      listener.sayYes();
    else
      listener.sayNo();
  }
}