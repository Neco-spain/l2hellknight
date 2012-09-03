package l2rt.gameserver.loginservercon.gspackets;

import l2rt.util.GArray;

import java.util.Collection;

public class PlayersInGame extends GameServerBasePacket
{
	private PlayersInGame(int online, String... accs)
	{
		writeC(0x0e);
		writeH(online);
		writeH(accs.length);
		for(String acc : accs)
			writeS(acc);
	}

	private PlayersInGame(int online, Collection<String> accs)
	{
		writeC(0x0e);
		writeH(online);
		writeH(accs.size());
		for(String acc : accs)
			writeS(acc);
	}

	private static final int MaxAccountsDataSize = 65535 - 2 - 1 - 2 - 2; // 65535 - 2 байта длины - 1 байт ID - 2 байта онлайн - 2 байта длина масива 
	private static final int AvgAccountNameLength = 16;
	private static final int AvgAccountNameBytes = (AvgAccountNameLength + 1) * 2;
	private static final int AvgAccountsPerPacket = MaxAccountsDataSize / AvgAccountNameBytes;

	public static Collection<GameServerBasePacket> makePlayersInGame(int online, Collection<String> accs)
	{
		GArray<GameServerBasePacket> retList = new GArray<GameServerBasePacket>(accs.size() / AvgAccountsPerPacket);
		GArray<String> nextList = new GArray<String>(AvgAccountsPerPacket);
		int accBytes, nextBytes = 0;

		for(String acc : accs)
		{
			accBytes = (acc.length() + 1) * 2;
			if(accBytes + nextBytes >= MaxAccountsDataSize)
			{
				retList.add(new PlayersInGame(online, nextList));
				nextList.clearSize();
				nextBytes = 0;
			}
			nextList.add(acc);
			nextBytes += accBytes;
		}
		if(nextList.size() > 0)
			retList.add(new PlayersInGame(online, nextList));

		return retList;
	}
}