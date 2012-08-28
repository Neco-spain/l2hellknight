package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExBR_ExtraUserInfo extends L2GameServerPacket
{
  private int _objectId;
  private int _effect3;
  private int _lectureMark;

  public ExBR_ExtraUserInfo(Player cha)
  {
    _objectId = cha.getObjectId();
    _effect3 = cha.getAbnormalEffect3();
    _lectureMark = cha.getLectureMark();
  }

  protected void writeImpl()
  {
    writeEx(218);
    writeD(_objectId);
    writeD(_effect3);
    writeC(_lectureMark);
  }
}