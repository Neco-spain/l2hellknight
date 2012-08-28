package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

public class GMViewCharacterInfo extends L2GameServerPacket
{
  private static final String _S__8F_GMVIEWCHARINFO = "[S] 8F GMViewCharacterInfo";
  private L2PcInstance _activeChar;

  public GMViewCharacterInfo(L2PcInstance character)
  {
    _activeChar = character;
  }

  protected final void writeImpl()
  {
    float moveMultiplier = _activeChar.getMovementSpeedMultiplier();
    int _runSpd = (int)(_activeChar.getRunSpeed() / moveMultiplier);
    int _walkSpd = (int)(_activeChar.getWalkSpeed() / moveMultiplier);

    writeC(143);

    writeD(_activeChar.getX());
    writeD(_activeChar.getY());
    writeD(_activeChar.getZ());
    writeD(_activeChar.getHeading());
    writeD(_activeChar.getObjectId());
    writeS(_activeChar.getName());
    writeD(_activeChar.getRace().ordinal());
    writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
    writeD(_activeChar.getClassId().getId());
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
    writeH(0);
    writeH(0);
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
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeF(moveMultiplier);
    writeF(_activeChar.getAttackSpeedMultiplier());
    writeF(_activeChar.getTemplate().collisionRadius);
    writeF(_activeChar.getTemplate().collisionHeight);
    writeD(_activeChar.getAppearance().getHairStyle());
    writeD(_activeChar.getAppearance().getHairColor());
    writeD(_activeChar.getAppearance().getFace());
    writeD(_activeChar.isGM() ? 1 : 0);

    writeS(_activeChar.getTitle());
    writeD(_activeChar.getClanId());
    writeD(_activeChar.getClanCrestId());
    writeD(_activeChar.getAllyId());
    writeC(_activeChar.getMountType());
    writeC(_activeChar.getPrivateStoreType());
    writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
    writeD(_activeChar.getPkKills());
    writeD(_activeChar.getPvpKills());

    writeH(_activeChar.getRecomLeft());
    writeH(_activeChar.getRecomHave());
    writeD(_activeChar.getClassId().getId());
    writeD(0);
    writeD(_activeChar.getMaxCp());
    writeD((int)_activeChar.getCurrentCp());

    writeC(_activeChar.isRunning() ? 1 : 0);

    writeC(321);

    writeD(_activeChar.getPledgeClass());

    writeC(_activeChar.isNoble() ? 1 : 0);
    writeC(_activeChar.isHero() ? 1 : 0);

    writeD(_activeChar.getAppearance().getNameColor());
    writeD(_activeChar.getAppearance().getTitleColor());
  }

  public String getType()
  {
    return "[S] 8F GMViewCharacterInfo";
  }
}