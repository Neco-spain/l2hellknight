package l2m.gameserver.network.serverpackets;

import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class ConfirmDlg extends SysMsgContainer<ConfirmDlg>
{
  private int _time;
  private int _requestId;

  public ConfirmDlg(SystemMsg msg, int time)
  {
    super(msg);
    _time = time;
  }

  protected final void writeImpl()
  {
    writeC(243);
    writeElements();
    writeD(_time);
    writeD(_requestId);
  }

  public void setRequestId(int requestId)
  {
    _requestId = requestId;
  }
}