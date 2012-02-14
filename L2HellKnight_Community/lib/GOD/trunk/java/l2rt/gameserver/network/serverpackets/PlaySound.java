package l2rt.gameserver.network.serverpackets;

import l2rt.util.Location;

public class PlaySound extends L2GameServerPacket
{
	private int _unknown1;
	private String _soundFile;
	private int _unknown3;
	private int _unknown4;
	private Location _loc = new Location();

	public PlaySound(String soundFile)
	{
		_unknown1 = 0;
		_soundFile = soundFile;
		_unknown3 = 0;
		_unknown4 = 0;
	}

	public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, Location loc)
	{
		_unknown1 = unknown1;
		_soundFile = soundFile;
		_unknown3 = unknown3;
		_unknown4 = unknown4;
		_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9e);
		writeD(_unknown1); //unknown 0 for quest and ship, c4 toturial = 2
		writeS(_soundFile);
		writeD(_unknown3); //unknown 0 for quest; 1 for ship;
		writeD(_unknown4); //0 for quest; objectId of ship
		writeD(_loc.x); //x
		writeD(_loc.y); //y
		writeD(_loc.z); //z
		writeD(_loc.h); //не уверен на все 100% :)
	}
}