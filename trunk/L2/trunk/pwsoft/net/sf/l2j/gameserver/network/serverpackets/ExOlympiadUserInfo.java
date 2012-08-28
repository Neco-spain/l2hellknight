package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
  private String _name;
  private int _side;
  private int _objId;
  private int _classId;
  private int _curHp;
  private int _maxHp;
  private int _curCp;
  private int _maxCp;

  public ExOlympiadUserInfo(L2PcInstance player)
  {
    _side = player.getOlympiadSide();
    _objId = player.getObjectId();
    _classId = player.getClassId().getId();
    _curHp = (int)player.getCurrentHp();
    _maxHp = player.getMaxHp();
    _curCp = (int)player.getCurrentCp();
    _maxCp = player.getMaxCp();
    _name = player.getName();
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(41);
    writeC(_side);
    writeD(_objId);
    writeS(_name);
    writeD(_classId);
    writeD(_curHp);
    writeD(_maxHp);
    writeD(_curCp);
    writeD(_maxCp);
  }
}