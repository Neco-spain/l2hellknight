package l2rt.gameserver.network.serverpackets;

import l2rt.config.ConfigSystem;

public class KeyPacket extends L2GameServerPacket
{
	private byte[] _key;
	public KeyPacket(byte[] key)
	{
		_key = key;
	}
	@Override
	protected void writeImpl() 
	{
        writeC(0x2E);
        if (_key == null || _key.length == 0) 
		{
            writeC(0x00);
            return;
        }
		else 
			writeC(0x01);
        for (int i = 0; i < 8; i++) 
		{
			writeC(_key[i]); // key
		}
        writeD(0x01);
        writeD(ConfigSystem.getInt("RequestServerID")); // server id
        writeC(0x00);
        writeD(0x00); // Seed (obfuscation key)
    }
}