package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;
import org.mmocore.network.ReceivablePacket;

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
  private static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

  protected boolean read()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player != null) && (player.isSpy())) {
      Log.add(TimeLogger.getLogTime() + " Player: " + player.getName() + "// " + ((L2GameClient)getClient()).toString() + "Clent: " + getType(), "packet_spy");
    }
    try
    {
      readImpl();
      return true;
    } catch (Throwable t) {
      _log.severe(TimeLogger.getLogTime() + "Client: " + ((L2GameClient)getClient()).toString() + " - Failed reading: " + getType());
      _log.severe("error: " + t);
      t.printStackTrace();

      handleIncompletePacket();
    }

    return false;
  }

  protected abstract void readImpl();

  public void run()
  {
    try {
      runImpl();
    } catch (Throwable t) {
      _log.severe("Client:  - Failed running: " + getType() + " - L2J Server Version: " + Config.SERVER_VERSION + " - DP Revision: " + Config.DATAPACK_VERSION);
      _log.severe("error: " + t);
      t.printStackTrace();
      if (!(this instanceof RequestWithDrawalParty)) {
        handleIncompletePacket();
      }
    }
    ((L2GameClient)getClient()).can_runImpl = true;
  }

  protected abstract void runImpl();

  protected final void sendPacket(L2GameServerPacket gsp) {
    ((L2GameClient)getClient()).sendPacket(gsp);
  }

  public void handleIncompletePacket() {
    try {
      L2GameClient client = (L2GameClient)getClient();

      _log.severe(TimeLogger.getLogTime() + "Packet not completed " + getType() + ". IP:" + client.toString());

      if (client.getUPTryes() > 4) {
        L2PcInstance player = client.getActiveChar();
        if (player == null) {
          _log.warning(TimeLogger.getLogTime() + "Too many uncomplete packets, connection closed. IP: " + client.getIpAddr() + ", account:" + client.getAccountName());
          client.forcedClose();
          return;
        }
        _log.warning(TimeLogger.getLogTime() + "Too many uncomplete packets, connection closed. IP: " + client.getIpAddr() + ", account:" + client.getAccountName() + ", character:" + player.getName());
        player.closeNetConnection();
      } else {
        client.addUPTryes();
      }
    }
    catch (Exception e)
    {
    }
  }

  public String getType()
  {
    return "C." + getClass().getSimpleName();
  }
}