package l2p.gameserver.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.CursedWeapon;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExCursedWeaponLocation;
import l2p.gameserver.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import l2p.gameserver.utils.Location;

public class RequestCursedWeaponLocation extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Creature activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    List list = new ArrayList();
    for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
    {
      Location pos = cw.getWorldPosition();
      if (pos != null) {
        list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
      }
    }
    activeChar.sendPacket(new ExCursedWeaponLocation(list));
  }
}