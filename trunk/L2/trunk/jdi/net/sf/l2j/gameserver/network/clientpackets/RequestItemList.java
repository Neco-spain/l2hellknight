package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public final class RequestItemList extends L2GameClientPacket
{
  private static final String _C__0F_REQUESTITEMLIST = "[C] 0F RequestItemList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    ((L2GameClient)getClient()).getActiveChar().cancelActiveTrade();
    if ((getClient() != null) && (((L2GameClient)getClient()).getActiveChar() != null) && (!((L2GameClient)getClient()).getActiveChar().isInvetoryDisabled()))
    {
      ItemList il = new ItemList(((L2GameClient)getClient()).getActiveChar(), true);
      sendPacket(il);
    }
  }

  public String getType()
  {
    return "[C] 0F RequestItemList";
  }
}