package l2m.loginserver;

import java.nio.ByteBuffer;
import l2m.commons.net.nio.impl.IPacketHandler;
import l2m.commons.net.nio.impl.ReceivablePacket;
import l2m.loginserver.clientpackets.AuthGameGuard;
import l2m.loginserver.clientpackets.RequestAuthLogin;
import l2m.loginserver.clientpackets.RequestServerList;
import l2m.loginserver.clientpackets.RequestServerLogin;

public final class L2LoginPacketHandler
  implements IPacketHandler<L2LoginClient>
{
  public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
  {
    int opcode = buf.get() & 0xFF;

    ReceivablePacket packet = null;
    L2LoginClient.LoginClientState state = client.getState();

    switch (1.$SwitchMap$l2m$loginserver$L2LoginClient$LoginClientState[state.ordinal()])
    {
    case 1:
      if (opcode != 7) break;
      packet = new AuthGameGuard(); break;
    case 2:
      if (opcode != 0) break;
      packet = new RequestAuthLogin(); break;
    case 3:
      if (opcode == 5) {
        packet = new RequestServerList(); } else {
        if (opcode != 2) break;
        packet = new RequestServerLogin();
      }
    }
    return packet;
  }
}