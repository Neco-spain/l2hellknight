package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import scripts.communitybbs.CommunityBoard;

public final class RequestShowBoard extends L2GameClientPacket
{
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
    return "C.ShowBoard";
  }
}