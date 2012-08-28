package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExCursedWeaponLocation;
import net.sf.l2j.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import net.sf.l2j.util.Point3D;

public final class RequestCursedWeaponLocation extends L2GameClientPacket
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
    for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
    {
      if (!cw.isActive())
        continue;
      Point3D pos = cw.getWorldPosition();

      if (pos != null) {
        list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
      }

    }

    if (!list.isEmpty())
    {
      player.sendPacket(new ExCursedWeaponLocation(list));
    }
  }
}