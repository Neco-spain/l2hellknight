package l2m.gameserver.serverpackets.components;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;

public abstract interface IStaticPacket
{
  public abstract L2GameServerPacket packet(Player paramPlayer);
}