package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.handler.bbs.CommunityBoardManager;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SystemMessage;

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