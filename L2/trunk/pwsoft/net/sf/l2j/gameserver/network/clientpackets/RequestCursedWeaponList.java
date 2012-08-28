package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2Character player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    List list = new FastList();
    for (Iterator i$ = CursedWeaponsManager.getInstance().getCursedWeaponsIds().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();

      list.add(Integer.valueOf(id));
    }
    player.sendPacket(new ExCursedWeaponList(list));
  }
}