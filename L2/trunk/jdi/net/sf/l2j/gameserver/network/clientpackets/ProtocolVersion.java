package net.sf.l2j.gameserver.network.clientpackets;

import java.net.InetAddress;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.gameserver.network.serverpackets.SendStatus;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public final class ProtocolVersion extends L2GameClientPacket
{
  private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
  static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
  private int _version;

  protected void readImpl()
  {
    _version = readH();
  }

  protected void runImpl()
  {
    if (_version == 65534)
    {
      if (Config.DEBUG) _log.info("Ping received");
      ((L2GameClient)getClient()).closeNow();
    }
    else if (_version == 65533)
    {
      if ((Config.RWHO_LOG) && (((L2GameClient)getClient()).getConnection().getSocket().getInetAddress().getHostAddress().equals(Config.RWHO_CLIENT)))
      {
        _log.warning(((L2GameClient)getClient()).toString() + " remote status request");
        ((L2GameClient)getClient()).close(new SendStatus());
      }
    }
    else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
    {
      _log.info("Client: " + ((L2GameClient)getClient()).toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
      _log.warning("Wrong Protocol Version " + _version);
      ((L2GameClient)getClient()).closeNow();
    }
    else
    {
      if (Config.DEBUG)
      {
        _log.fine("Client Protocol Revision is ok: " + _version);
      }

      KeyPacket pk = new KeyPacket(((L2GameClient)getClient()).enableCrypt());
      ((L2GameClient)getClient()).sendPacket(pk);
    }
  }

  public String getType()
  {
    return "[C] 00 ProtocolVersion";
  }
}