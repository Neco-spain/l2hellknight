package l2.hellknight.gameserver.model.actor.instance;

import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.network.serverpackets.ExColosseumFenceInfoPacket;

	public final class L2FenceInstance extends L2Object
	{
		private int _type;
		private int _width;
		private int _height;
		
	public L2FenceInstance(int objectId, int type, int width, int height)
   {
      super(objectId);
      _type = type;
      _width = width;
      _height = height;
   }
	
   @Override
    public void sendInfo(L2PcInstance activeChar)
   {
      activeChar.sendPacket(new ExColosseumFenceInfoPacket(this));
   }
   
   public int getType()
   {
      return _type;
   }
   
   public int getWidth()
   {
      return _width;
   }
   
   public int getHeight()
   {
      return _height;
   }
   
   @Override
   public boolean isAutoAttackable(L2Character attacker)
   {
      return false;
   }
}