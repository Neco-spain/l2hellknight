package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(RecipeItemMakeInfo.class.getName());
  private int _id;
  private L2PcInstance _activeChar;
  private boolean _success;

  public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
  {
    _id = id;
    _activeChar = player;
    _success = success;
  }

  public RecipeItemMakeInfo(int id, L2PcInstance player)
  {
    _id = id;
    _activeChar = player;
    _success = true;
  }

  protected final void writeImpl()
  {
    L2RecipeList recipe = RecipeController.getInstance().getRecipeById(_id);

    if (recipe != null)
    {
      writeC(215);

      writeD(_id);
      writeD(recipe.isDwarvenRecipe() ? 0 : 1);
      writeD((int)_activeChar.getCurrentMp());
      writeD(_activeChar.getMaxMp());
      writeD(_success ? 1 : 0);
    }
  }

  public String getType()
  {
    return "S.RecipeItemMakeInfo";
  }
}