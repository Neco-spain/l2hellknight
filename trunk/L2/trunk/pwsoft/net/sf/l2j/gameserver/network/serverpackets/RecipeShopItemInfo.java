package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RecipeShopItemInfo extends L2GameServerPacket
{
  private int _recipeId;
  private int _shopId;
  private int curMp;
  private int maxMp;
  private int _success = -1;
  private boolean can_writeImpl = false;

  public RecipeShopItemInfo(int shopId, int recipeId)
  {
    _shopId = shopId;
    _recipeId = recipeId;
  }

  public final void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    L2PcInstance manufacturer = L2World.getInstance().getPlayer(_shopId);
    if (manufacturer == null) {
      return;
    }
    curMp = (int)manufacturer.getCurrentMp();
    maxMp = manufacturer.getMaxMp();
    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(218);
    writeD(_shopId);
    writeD(_recipeId);
    writeD(curMp);
    writeD(maxMp);
    writeD(_success);
  }
}