package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestExBR_LectureMark extends L2GameClientPacket
{
  public static final int INITIAL_MARK = 1;
  public static final int EVANGELIST_MARK = 2;
  public static final int OFF_MARK = 3;
  private int _mark;

  protected void readImpl()
    throws Exception
  {
    _mark = readC();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!Config.EX_LECTURE_MARK)) {
      return;
    }
    switch (_mark)
    {
    case 1:
    case 2:
    case 3:
      player.setLectureMark(_mark);
      player.broadcastUserInfo(true);
    }
  }
}