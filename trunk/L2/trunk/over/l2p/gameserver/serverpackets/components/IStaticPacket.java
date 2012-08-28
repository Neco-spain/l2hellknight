package l2p.gameserver.serverpackets.components;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.L2GameServerPacket;

public abstract interface IStaticPacket
{
  public abstract L2GameServerPacket packet(Player paramPlayer);
}