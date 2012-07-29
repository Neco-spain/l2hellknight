package l2p.gameserver.serverpackets.components;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.L2GameServerPacket;

/**
 * @author VISTALL
 * @date 13:28/01.12.2010
 */
public interface IStaticPacket {
    L2GameServerPacket packet(Player player);
}
