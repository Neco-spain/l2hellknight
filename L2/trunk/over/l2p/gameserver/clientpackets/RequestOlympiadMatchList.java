package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExReceiveOlympiad.MatchList;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class RequestOlympiadMatchList extends L2GameClientPacket
{
  protected void readImpl()
    throws Exception
  {
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((!Olympiad.inCompPeriod()) || (Olympiad.isOlympiadEnd()))
    {
      player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return;
    }

    player.sendPacket(new ExReceiveOlympiad.MatchList());
  }
}