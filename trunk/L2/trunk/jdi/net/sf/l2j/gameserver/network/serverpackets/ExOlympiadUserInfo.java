package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
  private static final String _S__FE_29_OLYMPIADUSERINFO = "[S] FE:2C OlympiadUserInfo";
  private static L2PcInstance _activeChar;

  public ExOlympiadUserInfo(L2PcInstance player)
  {
    _activeChar = player;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(44);
    writeD(_activeChar.getObjectId());
    writeS(_activeChar.getName());
    writeD(_activeChar.getClassId().getId());
    writeD((int)_activeChar.getCurrentHp());
    writeD(_activeChar.getMaxHp());
    writeD((int)_activeChar.getCurrentCp());
    writeD(_activeChar.getMaxCp());
  }

  public String getType()
  {
    return "[S] FE:2C OlympiadUserInfo";
  }
}