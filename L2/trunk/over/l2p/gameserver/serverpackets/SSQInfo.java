package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
  private int _state = 0;

  public SSQInfo()
  {
    int compWinner = SevenSigns.getInstance().getCabalHighestScore();
    if (SevenSigns.getInstance().isSealValidationPeriod())
      if (compWinner == 2)
        _state = 2;
      else if (compWinner == 1)
        _state = 1;
  }

  public SSQInfo(int state)
  {
    _state = state;
  }

  protected final void writeImpl()
  {
    writeC(115);
    switch (_state)
    {
    case 1:
      writeH(257);
      break;
    case 2:
      writeH(258);
      break;
    default:
      writeH(256);
    }
  }
}