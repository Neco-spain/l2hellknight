package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
  private static final String _C__B1_RequestRecipeShopMessageSet = "[C] b1 RequestRecipeShopMessageSet";
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (player.getCreateList() != null)
    {
      player.getCreateList().setStoreName(_name);
    }
  }

  public String getType()
  {
    return "[C] b1 RequestRecipeShopMessageSet";
  }
}