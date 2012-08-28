package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.handler.bbs.CommunityBoardManager;
import l2m.gameserver.handler.bbs.ICommunityBoardHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;

public class RequestShowBoard extends L2GameClientPacket
{
  private int _unknown;

  public void readImpl()
  {
    _unknown = readD();
  }

  public void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (Config.COMMUNITYBOARD_ENABLED)
    {
      ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT);
      if (handler != null)
        handler.onBypassCommand(activeChar, Config.BBS_DEFAULT);
    }
    else {
      activeChar.sendPacket(new SystemMessage(938));
    }
  }
}