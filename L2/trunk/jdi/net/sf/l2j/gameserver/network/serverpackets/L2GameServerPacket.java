package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.network.L2GameClient;
import org.mmocore.network.SendablePacket;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
  protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());

  protected void write()
  {
    try
    {
      writeImpl();
    }
    catch (Throwable t)
    {
      _log.severe("Client: " + ((L2GameClient)getClient()).toString() + " - Failed writing: " + getType());
      t.printStackTrace();
    }
  }

  public void runImpl()
  {
  }

  protected abstract void writeImpl();

  public abstract String getType();

  protected int getHeaderSize()
  {
    return 2;
  }

  protected void writeHeader(int dataSize)
  {
    writeH(dataSize + getHeaderSize());
  }
}