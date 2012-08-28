package l2m.loginserver.serverpackets;

import l2m.loginserver.SessionKey;

public final class PlayOk extends L2LoginServerPacket
{
  private int _playOk1;
  private int _playOk2;

  public PlayOk(SessionKey sessionKey)
  {
    _playOk1 = sessionKey.playOkID1;
    _playOk2 = sessionKey.playOkID2;
  }

  protected void writeImpl()
  {
    writeC(7);
    writeD(_playOk1);
    writeD(_playOk2);
  }
}