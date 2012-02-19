package l2.brick.gameserver.network.serverpackets;

import l2.brick.gameserver.instancemanager.GraciaSeedManager;

public class ExShowSeedMapInfo extends L2GameServerPacket
{
	private static final String _S__FE_A1_EXSHOWSEEDMAPINFO = "[S] FE:A1 ExShowSeedMapInfo";
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0xa1); // SubId
		
		writeD(2); // seed count
		// Seed of Destruction
		writeD(-246857); // x coord
		writeD(251960); // y coord
		writeD(4331); // z coord
		writeD(2770 + GraciaSeedManager.getInstance().getSoDState()); // sys msg id
		// Seed of Infinity
		writeD(-213770); // x coord
		writeD(210760); // y coord
		writeD(4400); // z coord
		// Manager not implemented yet
		writeD(2766); // sys msg id
	}
	
	@Override
	public String getType()
	{
		return _S__FE_A1_EXSHOWSEEDMAPINFO;
	}
}
