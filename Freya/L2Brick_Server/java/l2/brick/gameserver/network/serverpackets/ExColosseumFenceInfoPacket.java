package l2.brick.gameserver.network.serverpackets;

import l2.brick.gameserver.model.actor.instance.L2FenceInstance;

	public class ExColosseumFenceInfoPacket extends L2GameServerPacket 
	{
		private static final String _S__FE_03_EXCOLOSSEUMFENCEINFOPACKET = "[S] FE:03 ExColosseumFenceInfoPacket";
		private int _type;
		private L2FenceInstance _activeChar;
		private int _width;
		private int _height;
   
	public ExColosseumFenceInfoPacket(L2FenceInstance activeChar)
   {
      _activeChar = activeChar;
      _type = activeChar.getType();
      _width = activeChar.getWidth();
      _height = activeChar.getHeight();
   }
   
   @Override
   protected void writeImpl()
   {
      writeC(0xfe);
      writeH(0x03);
      writeD(_activeChar.getObjectId()); // ?
      writeD(_type);
      writeD(_activeChar.getX());
      writeD(_activeChar.getY());
      writeD(_activeChar.getZ());
      writeD(_width);
      writeD(_height);
   }

   @Override
   public String getType()
   {
      return _S__FE_03_EXCOLOSSEUMFENCEINFOPACKET;
   }
}