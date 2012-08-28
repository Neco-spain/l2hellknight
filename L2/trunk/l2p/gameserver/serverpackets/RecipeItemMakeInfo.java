package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Recipe;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
  private int _id;
  private boolean _isDwarvenRecipe;
  private int _status;
  private int _curMP;
  private int _maxMP;

  public RecipeItemMakeInfo(Player player, Recipe recipeList, int status)
  {
    _id = recipeList.getId();
    _isDwarvenRecipe = recipeList.isDwarvenRecipe();
    _status = status;
    _curMP = (int)player.getCurrentMp();
    _maxMP = player.getMaxMp();
  }

  protected final void writeImpl()
  {
    writeC(221);
    writeD(_id);
    writeD(_isDwarvenRecipe ? 0 : 1);
    writeD(_curMP);
    writeD(_maxMP);
    writeD(_status);
  }
}