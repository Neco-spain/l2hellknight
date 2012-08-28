package l2p.gameserver.serverpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;

public final class SendStatus extends L2GameServerPacket
{
  private static final long MIN_UPDATE_PERIOD = 30000L;
  private static int online_players = 0;
  private static int max_online_players = 0;
  private static int online_priv_store = 0;
  private static long last_update = 0L;

  public SendStatus()
  {
    if (System.currentTimeMillis() - last_update < 30000L)
      return;
    last_update = System.currentTimeMillis();
    int i = 0;
    int j = 0;
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
    {
      i++;
      if ((player.isInStoreMode()) && ((!Config.SENDSTATUS_TRADE_JUST_OFFLINE) || (player.isInOfflineMode())))
        j++;
    }
    online_players = i;
    online_priv_store = (int)Math.floor(j * Config.SENDSTATUS_TRADE_MOD);
    max_online_players = Math.max(max_online_players, online_players);
  }

  protected final void writeImpl()
  {
    writeC(0);
    writeD(1);
    writeD(max_online_players);
    writeD(online_players);
    writeD(online_players);
    writeD(online_priv_store);

    writeD(2883632);
    for (int x = 0; x < 10; x++)
      writeH(41 + Rnd.get(17));
    writeD(43 + Rnd.get(17));
    int z = 36219 + Rnd.get(1987);
    writeD(z);
    writeD(z);
    writeD(37211 + Rnd.get(2397));
    writeD(0);
    writeD(2);
  }
}