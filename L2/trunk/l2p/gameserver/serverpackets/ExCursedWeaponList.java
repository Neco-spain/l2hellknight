package l2p.gameserver.serverpackets;

import l2p.gameserver.instancemanager.CursedWeaponsManager;

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