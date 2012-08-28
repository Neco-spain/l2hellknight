package org.mmocore.test.testserver;

import java.io.PrintStream;
import org.mmocore.network.ReceivablePacket;

public class TestRecvPacket extends ReceivablePacket<ServerClient>
{
  private int _value;

  protected boolean read()
  {
    _value = readD();
    return true;
  }

  public void run()
  {
    System.out.println("ServerRecebeu " + _value);
    TestSendPacket tsp = new TestSendPacket(_value);
    ((ServerClient)getClient()).sendPacket(tsp);
    ((ServerClient)getClient()).sendPacket(tsp);
    ((ServerClient)getClient()).sendPacket(tsp);
    ((ServerClient)getClient()).sendPacket(tsp);
    ((ServerClient)getClient()).sendPacket(tsp);
    ((ServerClient)getClient()).sendPacket(tsp);
  }
}