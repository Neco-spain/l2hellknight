package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class NicknameChanged extends L2GameServerPacket
{
  private String _title;
  private int _objectId;

  public NicknameChanged(L2PcInstance cha)
  {
    _objectId = cha.getObjectId();
    _title = cha.getTitle();
  }

  protected void writeImpl()
  {
    writeC(204);
    writeD(_objectId);
    writeS(_title);
  }
}