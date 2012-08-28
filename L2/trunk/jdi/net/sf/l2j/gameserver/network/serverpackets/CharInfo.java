package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

public class CharInfo extends L2GameServerPacket
{
  private static final Logger _log = Logger.getLogger(CharInfo.class.getName());
  private static final String _S__03_CHARINFO = "[S] 03 CharInfo";
  private L2PcInstance _activeChar;
  private Inventory _inv;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _mAtkSpd;
  private int _pAtkSpd;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private float _moveMultiplier;
  private float _attackSpeedMultiplier;
  private int _maxCp;

  public CharInfo(L2PcInstance cha)
  {
    _activeChar = cha;
    _inv = cha.getInventory();
    _x = _activeChar.getX();
    _y = _activeChar.getY();
    _z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
    _mAtkSpd = _activeChar.getMAtkSpd();
    _pAtkSpd = _activeChar.getPAtkSpd();
    _moveMultiplier = _activeChar.getMovementSpeedMultiplier();
    _attackSpeedMultiplier = _activeChar.getAttackSpeedMultiplier();
    _runSpd = (int)(_activeChar.getRunSpeed() / _moveMultiplier);
    _walkSpd = (int)(_activeChar.getWalkSpeed() / _moveMultiplier); _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
    _maxCp = _activeChar.getMaxCp();
  }

  protected final void writeImpl()
  {
    boolean gmSeeInvis = false;

    if (_activeChar.getAppearance().getInvisible())
    {
      L2PcInstance tmp = ((L2GameClient)getClient()).getActiveChar();
      if ((tmp != null) && (tmp.isGM()))
        gmSeeInvis = true;
      else {
        return;
      }
    }
    if (_activeChar.getPoly().isMorphed())
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());

      if (template != null)
      {
        writeC(22);
        writeD(_activeChar.getObjectId());
        writeD(_activeChar.getPoly().getPolyId() + 1000000);
        writeD(_activeChar.getKarma() > 0 ? 1 : 0);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(_heading);
        writeD(0);
        writeD(_mAtkSpd);
        writeD(_pAtkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimRunSpd);
        writeD(_swimWalkSpd);
        writeD(_flRunSpd);
        writeD(_flWalkSpd);
        writeD(_flyRunSpd);
        writeD(_flyWalkSpd);
        writeF(_moveMultiplier);
        writeF(_attackSpeedMultiplier);
        writeF(template.collisionRadius);
        writeF(template.collisionHeight);
        writeD(_inv.getPaperdollItemId(7));
        writeD(0);
        writeD(_inv.getPaperdollItemId(8));
        writeC(1);
        writeC(_activeChar.isRunning() ? 1 : 0);
        writeC(_activeChar.isInCombat() ? 1 : 0);
        writeC(_activeChar.isAlikeDead() ? 1 : 0);

        if (gmSeeInvis)
        {
          writeC(0);
        }
        else
        {
          writeC(_activeChar.getAppearance().getInvisible() ? 1 : 0);
        }

        writeS(_activeChar.getName());

        if (gmSeeInvis)
        {
          writeS("Invisible");
        }
        else
        {
          writeS(_activeChar.getTitle());
        }

        writeD(0);
        writeD(0);
        writeD(0);

        if (gmSeeInvis)
        {
          writeD(_activeChar.getAbnormalEffect() | 0x100000);
        }
        else
        {
          writeD(_activeChar.getAbnormalEffect());
        }

        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeC(0);
      }
      else {
        _log.warning("Character " + _activeChar.getName() + " (" + _activeChar.getObjectId() + ") morphed in a Npc (" + _activeChar.getPoly().getPolyId() + ") w/o template.");
      }
    }
    else
    {
      writeC(3);
      writeD(_x);
      writeD(_y);
      writeD(_z);
      writeD(_heading);
      writeD(_activeChar.getObjectId());
      writeS(_activeChar.getName());
      writeD(_activeChar.getRace().ordinal());
      writeD(_activeChar.getAppearance().getSex() ? 1 : 0);

      if (_activeChar.getClassIndex() == 0)
        writeD(_activeChar.getClassId().getId());
      else {
        writeD(_activeChar.getBaseClass());
      }
      writeD(_inv.getPaperdollItemId(17));
      writeD(_inv.getPaperdollItemId(6));
      writeD(_inv.getPaperdollItemId(7));
      writeD(_inv.getPaperdollItemId(8));
      writeD(_inv.getPaperdollItemId(9));
      writeD(_inv.getPaperdollItemId(10));
      writeD(_inv.getPaperdollItemId(11));
      writeD(_inv.getPaperdollItemId(12));
      writeD(_inv.getPaperdollItemId(13));
      writeD(_inv.getPaperdollItemId(14));
      writeD(_inv.getPaperdollItemId(16));
      writeD(_inv.getPaperdollItemId(15));

      writeH(0);
      writeH(0);
      writeH(0);
      writeH(0);
      writeD(_inv.getPaperdollAugmentationId(7));
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
      writeD(_inv.getPaperdollAugmentationId(14));
      writeH(0);
      writeH(0);
      writeH(0);
      writeH(0);

      writeD(_activeChar.getPvpFlag());
      writeD(_activeChar.getKarma());

      writeD(_mAtkSpd);
      writeD(_pAtkSpd);

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
      writeF(_activeChar.getMovementSpeedMultiplier());
      writeF(_activeChar.getAttackSpeedMultiplier());
      writeF(_activeChar.getBaseTemplate().collisionRadius);
      writeF(_activeChar.getBaseTemplate().collisionHeight);

      writeD(_activeChar.getAppearance().getHairStyle());
      writeD(_activeChar.getAppearance().getHairColor());
      writeD(_activeChar.getAppearance().getFace());

      if (gmSeeInvis)
      {
        writeS("Invisible");
      }
      else
      {
        writeS(_activeChar.getTitle());
      }

      writeD(_activeChar.getClanId());
      writeD(_activeChar.getClanCrestId());
      writeD(_activeChar.getAllyId());
      writeD(_activeChar.getAllyCrestId());

      writeD(0);

      writeC(_activeChar.isSitting() ? 0 : 1);
      writeC(_activeChar.isRunning() ? 1 : 0);
      writeC(_activeChar.isInCombat() ? 1 : 0);
      writeC(_activeChar.isAlikeDead() ? 1 : 0);

      if (gmSeeInvis)
      {
        writeC(0);
      }
      else
      {
        writeC(_activeChar.getAppearance().getInvisible() ? 1 : 0);
      }

      writeC(_activeChar.getMountType());
      writeC(_activeChar.getPrivateStoreType());

      writeH(_activeChar.getCubics().size());
      for (Iterator i$ = _activeChar.getCubics().keySet().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
        writeH(id);
      }
      writeC(0);

      if (gmSeeInvis)
      {
        writeD(_activeChar.getAbnormalEffect() | 0x100000);
      }
      else
      {
        writeD(_activeChar.getAbnormalEffect());
      }

      writeC(_activeChar.getRecomLeft());
      writeH(_activeChar.getRecomHave());
      writeD(_activeChar.getClassId().getId());

      writeD(_maxCp);
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

      if ((Config.OFFLINE_SET_NAME_COLOR) && (_activeChar.isOffline()))
        writeD(Config.OFFLINE_NAME_COLOR);
      else {
        writeD(_activeChar.getAppearance().getNameColor());
      }

      writeD(0);

      writeD(_activeChar.getPledgeClass());
      writeD(0);

      writeD(_activeChar.getAppearance().getTitleColor());

      if (_activeChar.isCursedWeaponEquiped())
        writeD(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquipedId()));
      else
        writeD(0);
    }
  }

  public String getType()
  {
    return "[S] 03 CharInfo";
  }
}