package l2m.gameserver.network.serverpackets;

import l2m.gameserver.skills.TimeStamp;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
  private int _itemId;
  private int _grpId;
  private int _remainedTime;
  private int _totalTime;

  public ExUseSharedGroupItem(int grpId, TimeStamp timeStamp)
  {
    _grpId = grpId;
    _itemId = timeStamp.getId();
    _remainedTime = (int)(timeStamp.getReuseCurrent() / 1000L);
    _totalTime = (int)(timeStamp.getReuseBasic() / 1000L);
  }

  protected final void writeImpl()
  {
    writeEx(74);

    writeD(_itemId);
    writeD(_grpId);
    writeD(_remainedTime);
    writeD(_totalTime);
  }
}