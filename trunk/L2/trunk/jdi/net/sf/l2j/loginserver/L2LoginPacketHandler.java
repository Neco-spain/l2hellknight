package net.sf.l2j.loginserver;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import net.sf.l2j.loginserver.clientpackets.AuthGameGuard;
import net.sf.l2j.loginserver.clientpackets.RequestAuthLogin;
import net.sf.l2j.loginserver.clientpackets.RequestServerList;
import net.sf.l2j.loginserver.clientpackets.RequestServerLogin;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.ReceivablePacket;

public final class L2LoginPacketHandler
  implements IPacketHandler<L2LoginClient>
{
  public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
  {
    int opcode = buf.get() & 0xFF;

    ReceivablePacket packet = null;
    L2LoginClient.LoginClientState state = client.getState();

    switch (1.$SwitchMap$net$sf$l2j$loginserver$L2LoginClient$LoginClientState[state.ordinal()])
    {
    case 1:
      if (opcode == 7)
      {
        packet = new AuthGameGuard();
      }
      else
      {
        debugOpcode(opcode, state);
      }
      break;
    case 2:
      if (opcode == 0)
      {
        packet = new RequestAuthLogin();
      }
      else
      {
        debugOpcode(opcode, state);
      }
      break;
    case 3:
      if (opcode == 5)
      {
        packet = new RequestServerList();
      }
      else if (opcode == 2)
      {
        packet = new RequestServerLogin();
      }
      else
      {
        debugOpcode(opcode, state);
      }
    }

    return packet;
  }

  private void debugOpcode(int opcode, L2LoginClient.LoginClientState state)
  {
    System.out.println("Unknown Opcode: " + opcode + " for state: " + state.name());
  }
}