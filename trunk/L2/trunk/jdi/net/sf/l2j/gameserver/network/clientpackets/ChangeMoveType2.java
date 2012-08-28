package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class ChangeMoveType2 extends L2GameClientPacket
{
  private static final String _C__1C_CHANGEMOVETYPE2 = "[C] 1C ChangeMoveType2";
  private boolean _typeRun;

  protected void readImpl()
  {
    _typeRun = (readD() == 1);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    if (_typeRun)
      player.setRunning();
    else
      player.setWalking();
  }

  public String getType()
  {
    return "[C] 1C ChangeMoveType2";
  }
}