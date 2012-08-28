package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.KeyPacket;
import l2p.gameserver.serverpackets.SendStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolVersion extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);
  private int _version;

  protected void readImpl()
  {
    _version = readD();
  }

  protected void runImpl()
  {
    if (_version == -2)
    {
      ((GameClient)_client).closeNow(false);
      return;
    }
    if (_version == -3)
    {
      _log.info("Status request from IP : " + ((GameClient)getClient()).getIpAddr());
      ((GameClient)getClient()).close(new SendStatus());
      return;
    }
    if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
    {
      _log.warn("Unknown protocol revision : " + _version + ", client : " + _client);
      ((GameClient)getClient()).close(new KeyPacket(null));
      return;
    }

    ((GameClient)getClient()).setRevision(_version);
    sendPacket(new KeyPacket(((GameClient)_client).enableCrypt()));
  }
}