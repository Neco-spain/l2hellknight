package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
  private L2PcInstance _activeChar;

  public ExDuelUpdateUserInfo(L2PcInstance cha)
  {
    _activeChar = cha;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(79);
    writeS(_activeChar.getName());
    writeD(_activeChar.getObjectId());
    writeD(_activeChar.getClassId().getId());
    writeD(_activeChar.getLevel());
    writeD((int)_activeChar.getCurrentHp());
    writeD(_activeChar.getMaxHp());
    writeD((int)_activeChar.getCurrentMp());
    writeD(_activeChar.getMaxMp());
    writeD((int)_activeChar.getCurrentCp());
    writeD(_activeChar.getMaxCp());
  }
}