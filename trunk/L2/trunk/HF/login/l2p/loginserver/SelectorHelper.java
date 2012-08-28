package l2m.loginserver;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import l2m.commons.net.nio.impl.IAcceptFilter;
import l2m.commons.net.nio.impl.IClientFactory;
import l2m.commons.net.nio.impl.IMMOExecutor;
import l2m.commons.net.nio.impl.MMOConnection;
import l2m.commons.threading.RunnableImpl;
import l2m.loginserver.serverpackets.Init;

public class SelectorHelper
  implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
  public void execute(Runnable r)
  {
    ThreadPoolManager.getInstance().execute(r);
  }

  public L2LoginClient create(MMOConnection<L2LoginClient> con)
  {
    L2LoginClient client = new L2LoginClient(con);
    client.sendPacket(new Init(client));
    ThreadPoolManager.getInstance().schedule(new RunnableImpl(client)
    {
      public void runImpl()
      {
        val$client.closeNow(false);
      }
    }
    , 60000L);

    return client;
  }

  public boolean accept(SocketChannel sc)
  {
    return !IpBanManager.getInstance().isIpBanned(sc.socket().getInetAddress().getHostAddress());
  }
}