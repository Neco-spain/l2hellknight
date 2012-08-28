package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.RecipeItemMakeInfo;

public final class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
  private int _id;
  private L2PcInstance _activeChar;

  protected void readImpl()
  {
    _id = readD();
    _activeChar = ((L2GameClient)getClient()).getActiveChar();
  }

  protected void runImpl()
  {
    sendPacket(new RecipeItemMakeInfo(_id, _activeChar));
  }

  public String getType()
  {
    return "C.RecipeItemMakeInfo";
  }
}