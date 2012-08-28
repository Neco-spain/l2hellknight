package l2p.gameserver.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExGetBossRecord;
import l2p.gameserver.serverpackets.ExGetBossRecord.BossRecordInfo;

public class RequestGetBossRecord extends L2GameClientPacket
{
  private int _bossID;

  protected void readImpl()
  {
    _bossID = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    int totalPoints = 0;
    int ranking = 0;

    if (activeChar == null) {
      return;
    }
    List list = new ArrayList();
    Map points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
    if ((points != null) && (!points.isEmpty())) {
      for (Map.Entry e : points.entrySet())
        switch (((Integer)e.getKey()).intValue())
        {
        case -1:
          ranking = ((Integer)e.getValue()).intValue();
          break;
        case 0:
          totalPoints = ((Integer)e.getValue()).intValue();
          break;
        default:
          list.add(new ExGetBossRecord.BossRecordInfo(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue(), 0));
        }
    }
    activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
  }
}