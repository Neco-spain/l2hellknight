package net.sf.l2j.gameserver.network.clientpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    FastList manorsName = new FastList();
    manorsName.add("gludio");
    manorsName.add("dion");
    manorsName.add("giran");
    manorsName.add("oren");
    manorsName.add("aden");
    manorsName.add("innadril");
    manorsName.add("goddard");
    manorsName.add("rune");
    manorsName.add("schuttgart");
    player.sendPacket(new ExSendManorList(manorsName));
    manorsName.clear();
    manorsName = null;
  }
}