package l2rt.loginserver;

import l2rt.util.GArray;
import l2rt.util.HWID.HWIDComparator;
import l2rt.util.HWID.HardwareID;
import l2rt.util.NetList;

public class Lock
{
	public static final HWIDLockComparator LockComparator = new HWIDLockComparator(2);
	private GArray<HardwareID> hwids;
	private NetList ips;

	public Lock()
	{}

	public void addIP(String ip)
	{
		if(ips == null)
			ips = new NetList();
		ips.AddNet(ip);
	}

	public void addHWID(String hwid)
	{
		if(hwids == null)
			hwids = new GArray<HardwareID>(2);
		hwids.add(new HardwareID(hwid));
	}

	public boolean checkIP(String ip)
	{
		if(ips == null)
			return false;
		return ips.isIpInNets(ip);
	}

	public boolean checkHWID(HardwareID hwid)
	{
		if(hwids == null)
			return false;
		return LockComparator.contains(hwid, hwids);
	}

	public static class HWIDLockComparator extends HWIDComparator
	{
		private int MIN_MATCHES = 2;

		public HWIDLockComparator(int min)
		{
			MIN_MATCHES = min;
		}

		@Override
		public int compare(HardwareID o1, HardwareID o2)
		{
			if(o1 == null || o2 == null)
				return o1 == o2 ? EQUALS : NOT_EQUALS;
			int matches = 0;
			if(COMPARE_CPU && o1.CPU == o2.CPU)
				matches++;
			if(COMPARE_BIOS && o1.BIOS == o2.BIOS)
				matches++;
			if(COMPARE_HDD && o1.HDD == o2.HDD)
				matches++;
			if(COMPARE_MAC && o1.MAC == o2.MAC)
				matches++;
			if(COMPARE_WLInternal && o1.WLInternal == o2.WLInternal)
				matches++;
			return matches >= MIN_MATCHES ? EQUALS : NOT_EQUALS;
		}
	}
}