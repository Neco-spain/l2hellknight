package org.mmocore.test.testserver;

import java.nio.ByteBuffer;
import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.SendablePacket;

public final class ServerClient extends MMOClient<MMOConnection<ServerClient>>
{
  public ServerClient(MMOConnection<ServerClient> con)
  {
    super(con);
  }

  public boolean decrypt(ByteBuffer buf, int size)
  {
    return true;
  }

  public boolean encrypt(ByteBuffer buf, int size)
  {
    buf.position(buf.position() + size);
    return true;
  }

  public void sendPacket(SendablePacket<ServerClient> sp)
  {
    getConnection().sendPacket(sp);
  }

  public void onDisconection()
  {
  }
}