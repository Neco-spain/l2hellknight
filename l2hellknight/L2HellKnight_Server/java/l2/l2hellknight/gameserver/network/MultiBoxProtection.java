package l2.hellknight.gameserver.network;

import java.util.Arrays;
import java.util.Map;

import l2.hellknight.Config;

import javolution.util.FastMap;

public class MultiBoxProtection
{
	private Map<IpPack, Integer> map;
	
	public static MultiBoxProtection getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public MultiBoxProtection()
	{
		map = new FastMap<MultiBoxProtection.IpPack, Integer>();
	}
	
	public synchronized boolean registerNewConnection(L2GameClient client)
	{
		IpPack pack = new IpPack(client.getAdress(), client.getTrace());
		Integer count = map.get(pack);
		if (count == null)
		{
			map.put(pack, 1);
		}
		else if (count < Config.MAX_PLAYERS_FROM_ONE_PC)
		{
			map.put(pack, count+1);
		}
		else
		{
			map.put(pack, count+1); // yes do it anyway
			return false;
		}
		
		return true;
	}
	
	public synchronized void removeConnection(L2GameClient client)
	{
		IpPack pack = new IpPack(client.getAdress(), client.getTrace());
		Integer count = map.get(pack);
		if (count != null && count > 1)
		{
			map.put(pack, count-1);
		}
		else
		{
			map.remove(pack);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MultiBoxProtection _instance = new MultiBoxProtection();
	}
	
	public final static class IpPack
	{
		String ip;
		int[][] tracert;
		
		public IpPack(String ip, int[][] tracert)
		{
			this.ip = ip;
			this.tracert = tracert;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ip == null) ? 0 : ip.hashCode());
			for (int[] array: tracert)
				result = prime * result + Arrays.hashCode(array);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IpPack other = (IpPack) obj;
			if (ip == null)
			{
				if (other.ip != null)
					return false;
			}
			else if (!ip.equals(other.ip))
				return false;
			for (int i = 0 ; i < tracert.length; i++)
				for (int o = 0; o < tracert[0].length; o++)
					if (tracert[i][o] != other.tracert[i][o])
						return false;
			return true;
		}

	}
}