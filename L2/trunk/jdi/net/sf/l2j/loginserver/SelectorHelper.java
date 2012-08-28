package net.sf.l2j.loginserver;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.sf.l2j.loginserver.serverpackets.Init;
import org.mmocore.network.HeaderInfo;
import org.mmocore.network.IAcceptFilter;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;
import org.mmocore.network.TCPHeaderHandler;

public class SelectorHelper extends TCPHeaderHandler<L2LoginClient>
  implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
  private ThreadPoolExecutor _generalPacketsThreadPool;

  public SelectorHelper()
  {
    super(null);
    _generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue());
  }

  public void execute(ReceivablePacket<L2LoginClient> packet)
  {
    _generalPacketsThreadPool.execute(packet);
  }

  public L2LoginClient create(MMOConnection<L2LoginClient> con)
  {
    L2LoginClient client = new L2LoginClient(con);
    client.sendPacket(new Init(client));
    return client;
  }

  public boolean accept(SocketChannel sc)
  {
    return !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
  }

  public HeaderInfo handleHeader(SelectionKey key, ByteBuffer buf)
  {
    if (buf.remaining() >= 2)
    {
      int dataPending = (buf.getShort() & 0xFFFF) - 2;
      L2LoginClient client = (L2LoginClient)((MMOConnection)key.attachment()).getClient();
      return getHeaderInfoReturn().set(0, dataPending, false, client);
    }

    L2LoginClient client = (L2LoginClient)((MMOConnection)key.attachment()).getClient();
    return getHeaderInfoReturn().set(2 - buf.remaining(), 0, false, client);
  }
}