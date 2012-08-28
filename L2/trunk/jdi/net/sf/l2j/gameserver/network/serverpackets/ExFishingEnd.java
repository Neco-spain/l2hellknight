package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExFishingEnd extends L2GameServerPacket
{
  private static final String _S__FE_14_EXFISHINGEND = "[S] FE:14 ExFishingEnd";
  private boolean _win;
  L2Character _activeChar;

  public ExFishingEnd(boolean win, L2PcInstance character)
  {
    _win = win;
    _activeChar = character;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(20);
    writeD(_activeChar.getObjectId());
    writeC(_win ? 1 : 0);
  }

  public String getType()
  {
    return "[S] FE:14 ExFishingEnd";
  }
}