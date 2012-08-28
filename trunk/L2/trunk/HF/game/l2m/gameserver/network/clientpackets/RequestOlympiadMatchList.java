package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.olympiad.Olympiad;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExReceiveOlympiad.MatchList;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

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