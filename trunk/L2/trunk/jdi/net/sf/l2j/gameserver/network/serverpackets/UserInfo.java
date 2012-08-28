package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

public class UserInfo extends L2GameServerPacket
{
  private static final String _S__04_USERINFO = "[S] 04 UserInfo";
  private L2PcInstance _activeChar;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private int _relation;
  private float _moveMultiplier;

  public UserInfo(L2PcInstance character)
  {
    _activeChar = character;

    _moveMultiplier = _activeChar.getMovementSpeedMultiplier();
    _runSpd = (int)(_activeChar.getRunSpeed() / _moveMultiplier);
    _walkSpd = (int)(_activeChar.getWalkSpeed() / _moveMultiplier);
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _relation = (_activeChar.isClanLeader() ? 64 : 0);
    if (_activeChar.getSiegeState() == 1) _relation |= 384;
    if (_activeChar.getSiegeState() == 2) _relation |= 128;
  }

  protected final void writeImpl()
  {
    writeC(4);

    writeD(_activeChar.getX());
    writeD(_activeChar.getY());
    writeD(_activeChar.getZ());
    writeD(_activeChar.getHeading());
    writeD(_activeChar.getObjectId());
    writeS(_activeChar.getName());
    writeD(_activeChar.getRace().ordinal());
    writeD(_activeChar.getAppearance().getSex() ? 1 : 0);

    if (_activeChar.getClassIndex() == 0) writeD(_activeChar.getClassId().getId()); else {
      writeD(_activeChar.getBaseClass());
    }
    writeD(_activeChar.getLevel());
    writeQ(_activeChar.getExp());
    writeD(_activeChar.getSTR());
    writeD(_activeChar.getDEX());
    writeD(_activeChar.getCON());
    writeD(_activeChar.getINT());
    writeD(_activeChar.getWIT());
    writeD(_activeChar.getMEN());
    writeD(_activeChar.getMaxHp());
    writeD((int)_activeChar.getCurrentHp());
    writeD(_activeChar.getMaxMp());
    writeD((int)_activeChar.getCurrentMp());
    writeD(_activeChar.getSp());
    writeD(_activeChar.getCurrentLoad());
    writeD(_activeChar.getMaxLoad());

    writeD(40);

    writeD(_activeChar.getInventory().getPaperdollObjectId(17));
    writeD(_activeChar.getInventory().getPaperdollObjectId(2));
    writeD(_activeChar.getInventory().getPaperdollObjectId(1));
    writeD(_activeChar.getInventory().getPaperdollObjectId(3));
    writeD(_activeChar.getInventory().getPaperdollObjectId(5));
    writeD(_activeChar.getInventory().getPaperdollObjectId(4));
    writeD(_activeChar.getInventory().getPaperdollObjectId(6));
    writeD(_activeChar.getInventory().getPaperdollObjectId(7));
    writeD(_activeChar.getInventory().getPaperdollObjectId(8));
    writeD(_activeChar.getInventory().getPaperdollObjectId(9));
    writeD(_activeChar.getInventory().getPaperdollObjectId(10));
    writeD(_activeChar.getInventory().getPaperdollObjectId(11));
    writeD(_activeChar.getInventory().getPaperdollObjectId(12));
    writeD(_activeChar.getInventory().getPaperdollObjectId(13));
    writeD(_activeChar.getInventory().getPaperdollObjectId(14));
    writeD(_activeChar.getInventory().getPaperdollObjectId(16));
    writeD(_activeChar.getInventory().getPaperdollObjectId(15));

    writeD(_activeChar.getInventory().getPaperdollItemId(17));
    writeD(_activeChar.getInventory().getPaperdollItemId(2));
    writeD(_activeChar.getInventory().getPaperdollItemId(1));
    writeD(_activeChar.getInventory().getPaperdollItemId(3));
    writeD(_activeChar.getInventory().getPaperdollItemId(5));
    writeD(_activeChar.getInventory().getPaperdollItemId(4));
    writeD(_activeChar.getInventory().getPaperdollItemId(6));
    writeD(_activeChar.getInventory().getPaperdollItemId(7));
    writeD(_activeChar.getInventory().getPaperdollItemId(8));
    writeD(_activeChar.getInventory().getPaperdollItemId(9));
    writeD(_activeChar.getInventory().getPaperdollItemId(10));
    writeD(_activeChar.getInventory().getPaperdollItemId(11));
    writeD(_activeChar.getInventory().getPaperdollItemId(12));
    writeD(_activeChar.getInventory().getPaperdollItemId(13));
    writeD(_activeChar.getInventory().getPaperdollItemId(14));
    writeD(_activeChar.getInventory().getPaperdollItemId(16));
    writeD(_activeChar.getInventory().getPaperdollItemId(15));

    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeD(_activeChar.getInventory().getPaperdollAugmentationId(7));
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);
    writeD(_activeChar.getInventory().getPaperdollAugmentationId(14));
    writeH(0);
    writeH(0);
    writeH(0);
    writeH(0);

    writeD(_activeChar.getPAtk(null));
    writeD(_activeChar.getPAtkSpd());
    writeD(_activeChar.getPDef(null));
    writeD(_activeChar.getEvasionRate(null));
    writeD(_activeChar.getAccuracy());
    writeD(_activeChar.getCriticalHit(null, null));
    writeD(_activeChar.getMAtk(null, null));

    writeD(_activeChar.getMAtkSpd());
    writeD(_activeChar.getPAtkSpd());

    writeD(_activeChar.getMDef(null, null));

    writeD(_activeChar.getPvpFlag());
    writeD(_activeChar.getKarma());

    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_swimRunSpd);
    writeD(_swimWalkSpd);
    writeD(_flRunSpd);
    writeD(_flWalkSpd);
    writeD(_flyRunSpd);
    writeD(_flyWalkSpd);
    writeF(_moveMultiplier);
    writeF(_activeChar.getAttackSpeedMultiplier());

    L2Summon pet = _activeChar.getPet();
    if ((_activeChar.getMountType() != 0) && (pet != null))
    {
      writeF(pet.getTemplate().collisionRadius);
      writeF(pet.getTemplate().collisionHeight);
    }
    else
    {
      writeF(_activeChar.getBaseTemplate().collisionRadius);
      writeF(_activeChar.getBaseTemplate().collisionHeight);
    }

    writeD(_activeChar.getAppearance().getHairStyle());
    writeD(_activeChar.getAppearance().getHairColor());
    writeD(_activeChar.getAppearance().getFace());
    writeD(_activeChar.getAccessLevel() >= Config.GM_ALTG_MIN_LEVEL ? 1 : 0);

    String title = _activeChar.getTitle();
    if ((_activeChar.getAppearance().getInvisible()) && (_activeChar.isGM())) title = "Invisible";
    if (_activeChar.getPoly().isMorphed())
    {
      L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
      if (polyObj != null)
        title = title + " - " + polyObj.name;
    }
    writeS(title);

    writeD(_activeChar.getClanId());
    writeD(_activeChar.getClanCrestId());
    writeD(_activeChar.getAllyId());
    writeD(_activeChar.getAllyCrestId());

    writeD(_relation);
    writeC(_activeChar.getMountType());
    writeC(_activeChar.getPrivateStoreType());
    writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
    writeD(_activeChar.getPkKills());
    writeD(_activeChar.getPvpKills());

    writeH(_activeChar.getCubics().size());
    for (Iterator i$ = _activeChar.getCubics().keySet().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
      writeH(id);
    }
    writeC(0);

    writeD(_activeChar.getAbnormalEffect());
    writeC(0);

    writeD(_activeChar.getClanPrivileges());

    writeH(_activeChar.getRecomLeft());
    writeH(_activeChar.getRecomHave());
    writeD(0);
    writeH(_activeChar.GetInventoryLimit());

    writeD(_activeChar.getClassId().getId());
    writeD(0);
    writeD(_activeChar.getMaxCp());
    writeD((int)_activeChar.getCurrentCp());
    writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());

    if (_activeChar.getTeam() == 1)
      writeC(1);
    else if (_activeChar.getTeam() == 2)
      writeC(2);
    else {
      writeC(0);
    }
    writeD(_activeChar.getClanCrestLargeId());
    writeC(_activeChar.isNoble() ? 1 : 0);
    writeC((_activeChar.isHero()) || ((_activeChar.isGM()) && (Config.GM_HERO_AURA)) ? 1 : 0);

    writeC(_activeChar.isFishing() ? 1 : 0);
    writeD(_activeChar.GetFishx());
    writeD(_activeChar.GetFishy());
    writeD(_activeChar.GetFishz());
    writeD(_activeChar.getAppearance().getNameColor());

    writeC(_activeChar.isRunning() ? 1 : 0);

    writeD(_activeChar.getPledgeClass());
    writeD(0);

    writeD(_activeChar.getAppearance().getTitleColor());

    if (_activeChar.isCursedWeaponEquiped())
      writeD(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquipedId()));
    else
      writeD(0);
  }

  public String getType()
  {
    return "[S] 04 UserInfo";
  }
}