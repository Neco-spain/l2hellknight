package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.L2GameClient;
import scripts.communitybbs.CommunityBoard;

public class RequestBBSwrite extends L2GameClientPacket
{
  private String _url;
  private String _arg1;
  private String _arg2;
  private String _arg3;
  private String _arg4;
  private String _arg5;

  protected void readImpl()
  {
    _url = readS();
    _arg1 = readS();
    _arg2 = readS();
    _arg3 = readS();
    _arg4 = readS();
    _arg5 = readS();
  }

  protected void runImpl()
  {
    CommunityBoard.getInstance().handleWriteCommands((L2GameClient)getClient(), _url, _arg1, _arg2, _arg3, _arg4, _arg5);
  }
}