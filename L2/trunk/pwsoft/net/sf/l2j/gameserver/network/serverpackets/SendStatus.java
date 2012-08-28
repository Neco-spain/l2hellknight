package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Random;
import net.sf.l2j.gameserver.util.Online;

public final class SendStatus extends L2GameServerPacket
{
  protected int online_players;
  protected int max_online;
  protected int online_priv_store;

  public void runImpl()
  {
    online_players = Online.getInstance().getCurrentOnline();
    online_priv_store = Online.getInstance().getOfflineTradersOnline();
    max_online = Online.getInstance().getMaxOnline();
  }

  protected final void writeImpl()
  {
    writeC(0);
    writeD(1);
    writeD(max_online);
    writeD(online_players);
    writeD(online_players);
    writeD(online_priv_store);

    Random ppc = new Random();

    writeH(48);
    writeH(44);
    for (int x = 0; x < 11; x++)
    {
      writeH(41 + ppc.nextInt(17));
    }
    writeD(43 + ppc.nextInt(17));
    int z = 36219 + ppc.nextInt(1987);
    writeD(z);
    writeD(z);
    writeD(37211 + ppc.nextInt(2397));
    writeD(0);
    writeD(2);
  }

  public String getType()
  {
    return "S.rWho";
  }
}