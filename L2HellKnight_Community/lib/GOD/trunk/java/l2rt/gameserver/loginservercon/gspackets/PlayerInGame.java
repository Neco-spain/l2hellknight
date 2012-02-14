package l2rt.gameserver.loginservercon.gspackets;

public class PlayerInGame extends GameServerBasePacket
{
	/**
	 * Посылает геймсерверу информацию об игроке в игре
	 * @param player - имя аккаунта или null как флаг инициализации списка
	 */
	public PlayerInGame(String player, int size)
	{
		writeC(0x02);
		writeS(player);
		writeH(size);
	}
}