package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class DlgAnswer extends L2GameClientPacket
{
  private int _messageId;
  private int _answer;
  private int _unk;

  protected void readImpl()
  {
    _messageId = readD();
    _answer = readD();
    _unk = readD();
  }

  public void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    switch (_messageId)
    {
    case 614:
      player.engageAnswer(_answer);
      break;
    case 1510:
      player.reviveAnswer(_answer);
      break;
    case 1842:
      player.sfAnswer(_answer);
    }
  }
}