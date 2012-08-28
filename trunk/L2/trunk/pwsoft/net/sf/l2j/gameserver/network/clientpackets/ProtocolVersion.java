package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.gameserver.network.serverpackets.SendStatus;

public final class ProtocolVersion extends L2GameClientPacket
{
  private int _version;

  protected void readImpl()
  {
    _version = readH();
  }

  protected void runImpl()
  {
    if (_version == -2)
    {
      ((L2GameClient)getClient()).close(new SendStatus());
      return;
    }

    if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
    {
      ((L2GameClient)getClient()).close(new SendStatus());
      return;
    }

    ((L2GameClient)getClient()).sendPacket(new KeyPacket(((L2GameClient)getClient()).enableCrypt()));
  }
}