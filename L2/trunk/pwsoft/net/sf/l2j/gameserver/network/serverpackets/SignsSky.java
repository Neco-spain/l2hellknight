package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.SevenSigns;

public class SignsSky extends L2GameServerPacket
{
  private int _state = 0;

  public SignsSky()
  {
    int compWinner = SevenSigns.getInstance().getCabalHighestScore();

    if (SevenSigns.getInstance().isSealValidationPeriod())
      if (compWinner == 2)
        _state = 2;
      else if (compWinner == 1)
        _state = 1;
  }

  public SignsSky(int state)
  {
    _state = state;
  }

  protected final void writeImpl()
  {
    writeC(248);

    if (_state == 2)
      writeH(258);
    else if (_state == 1)
      writeH(257);
  }

  public String getType()
  {
    return "S.SignsSky";
  }
}