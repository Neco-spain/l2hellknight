package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExGetBossRecord;

public class RequestGetBossRecord extends L2GameClientPacket
{
  protected static final Logger _log = Logger.getLogger(RequestGetBossRecord.class.getName());
  private int _bossId;

  protected void readImpl()
  {
    _bossId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    int points = RaidBossPointsManager.getPointsByOwnerId(player.getObjectId());
    int ranking = RaidBossPointsManager.calculateRanking(player);

    Map list = RaidBossPointsManager.getList(player);

    player.sendPacket(new ExGetBossRecord(ranking, points, list));
  }
}