package net.sf.l2j.gameserver.network.clientpackets;

public final class DummyPacket extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  public void runImpl()
  {
  }

  public String getType()
  {
    return "DummyPacket";
  }
}