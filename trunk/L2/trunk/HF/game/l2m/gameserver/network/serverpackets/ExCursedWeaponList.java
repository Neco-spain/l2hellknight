package l2m.gameserver.network.serverpackets;

import l2m.gameserver.instancemanager.CursedWeaponsManager;

public class ExCursedWeaponList extends L2GameServerPacket
{
  private int[] cursedWeapon_ids;

  public ExCursedWeaponList()
  {
    cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
  }

  protected final void writeImpl()
  {
    writeEx(70);
    writeDD(cursedWeapon_ids, true);
  }
}