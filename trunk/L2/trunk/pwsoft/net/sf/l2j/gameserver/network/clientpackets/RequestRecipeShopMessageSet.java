package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    try
    {
      _name = readS();
    }
    catch (BufferUnderflowException e)
    {
      _name = "";
    }
  }

  protected void runImpl()
  {
    if (_name.length() < 1) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getCreateList() != null)
      player.getCreateList().setStoreName(_name);
  }
}