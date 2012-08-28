package net.sf.l2j.gameserver.network.serverpackets;

public abstract class AbstractServerBasePacket extends L2GameServerPacket
{
  public abstract void runImpl();

  protected abstract void writeImpl();

  public abstract String getType();
}