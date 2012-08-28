package net.sf.l2j.gameserver.network.serverpackets;

import java.io.PrintStream;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;
import org.mmocore.network.SendablePacket;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
  protected void write()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if ((activeChar != null) && (activeChar.isSpy())) {
      Log.add(TimeLogger.getLogTime() + " Player: " + activeChar.getName() + "// " + ((L2GameClient)getClient()).toString() + "Server: " + getType(), "packet_spy");
    }
    try
    {
      writeImpl();
    } catch (Throwable t) {
      System.out.println("Client: " + ((L2GameClient)getClient()).toString() + " - Failed writing: " + getType());
      t.printStackTrace();
    }

    gc();
  }

  public void runImpl()
  {
  }

  protected abstract void writeImpl();

  public String getType()
  {
    return "S." + getClass().getSimpleName();
  }

  protected int getHeaderSize()
  {
    return 2;
  }

  protected void writeHeader(int dataSize)
  {
    writeH(dataSize + getHeaderSize());
  }

  public void gc()
  {
  }

  public void gcb()
  {
  }

  public boolean isCharInfo()
  {
    return false;
  }
}