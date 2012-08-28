package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Random;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;

public final class SendStatus extends L2GameServerPacket
{
  private static final String _S__00_STATUS = "[S] 00 rWho";
  private int online_players = 0;
  private int max_online = 0;
  private int online_priv_store = 0;
  private float priv_store_factor = 0.0F;
  private static Logger _log = Logger.getLogger(SendStatus.class.getName());

  public void runImpl()
  {
  }

  protected final void writeImpl()
  {
    Random ppc = new Random();
    L2World.getInstance(); online_players = (L2World.getAllPlayersCount() + Config.RWHO_ONLINE_INCREMENT);

    if (online_players > Config.RWHO_MAX_ONLINE)
    {
      Config.RWHO_MAX_ONLINE = online_players;
    }

    max_online = Config.RWHO_MAX_ONLINE;
    priv_store_factor = Config.RWHO_PRIV_STORE_FACTOR;

    L2World.getInstance(); L2World.getInstance(); online_players = (L2World.getAllPlayersCount() + L2World.getAllPlayersCount() * Config.RWHO_ONLINE_INCREMENT / 100 + Config.RWHO_FORCE_INC);
    online_priv_store = (int)(online_players * (priv_store_factor / 100.0F));

    writeC(0);
    writeD(1);
    writeD(max_online);
    writeD(online_players);
    writeD(online_players);
    writeD(online_priv_store);

    if (Config.RWHO_SEND_TRASH)
    {
      writeH(48);
      writeH(44);

      writeH(54);
      writeH(44);

      if (Config.RWHO_ARRAY[12] == Config.RWHO_KEEP_STAT)
      {
        int z = ppc.nextInt(6);
        if (z == 0) {
          z += 2;
        }
        for (int x = 0; x < 8; x++) {
          if (x == 4)
          {
            Config.RWHO_ARRAY[x] = 44;
          }
          else Config.RWHO_ARRAY[x] = (51 + ppc.nextInt(z));
        }
        Config.RWHO_ARRAY[11] = (37265 + ppc.nextInt(z * 2 + 3));
        Config.RWHO_ARRAY[8] = (51 + ppc.nextInt(z));
        z = 36224 + ppc.nextInt(z * 2);
        Config.RWHO_ARRAY[9] = z;
        Config.RWHO_ARRAY[10] = z;
        Config.RWHO_ARRAY[12] = 1;
      }

      for (int z = 0; z < 8; z++)
      {
        if (z == 3)
        {
          Config.RWHO_ARRAY[z] -= 1;
        }
        writeH(Config.RWHO_ARRAY[z]);
      }
      writeD(Config.RWHO_ARRAY[8]);
      writeD(Config.RWHO_ARRAY[9]);
      writeD(Config.RWHO_ARRAY[10]);
      writeD(Config.RWHO_ARRAY[11]);
      Config.RWHO_ARRAY[12] += 1;

      writeD(0);
      writeD(2);
    }
  }

  public String getType()
  {
    return "[S] 00 rWho";
  }
}