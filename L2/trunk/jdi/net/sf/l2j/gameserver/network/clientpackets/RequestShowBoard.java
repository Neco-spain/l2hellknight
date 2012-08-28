package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestShowBoard extends L2GameClientPacket
{
  private static final String _C__57_REQUESTSHOWBOARD = "[C] 57 RequestShowBoard";
  private int _unknown;

  protected void readImpl()
  {
    _unknown = readD();
  }

  protected void runImpl()
  {
    CommunityBoard.getInstance().handleCommands((L2GameClient)getClient(), Config.BBS_DEFAULT);
  }

  public String getType()
  {
    return "[C] 57 RequestShowBoard";
  }
}