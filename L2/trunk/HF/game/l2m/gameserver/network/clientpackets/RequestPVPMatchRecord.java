package l2m.gameserver.network.clientpackets;

import java.io.PrintStream;
import java.nio.ByteBuffer;

public class RequestPVPMatchRecord extends L2GameClientPacket
{
  protected void readImpl()
  {
    System.out.println("Unimplemented packet: " + getType() + " | size: " + _buf.remaining());
  }

  protected void runImpl()
  {
  }
}