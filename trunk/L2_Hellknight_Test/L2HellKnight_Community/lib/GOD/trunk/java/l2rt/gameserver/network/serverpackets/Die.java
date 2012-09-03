package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.instances.L2MonsterInstance;

/**
 * Пример:
 * 00
 * 8b 22 90 48 objectId
 * 01 00 00 00
 * 00 00 00 00
 * 00 00 00 00
 * 00 00 00 00
 * 00 00 00 00
 * 00 00 00 00
 * 00 00 00 00
 * format  dddddddd   rev 828
 */
public class Die extends L2GameServerPacket
{
	private int _chaId;
	private boolean _fake;
	private boolean _sweepable;
	private int _access;
	private L2Clan _clan;
	private L2Character _cha;
	private int to_hideaway, to_castle, to_siege_HQ, to_fortress;

	/**
	 * @param _characters
	 */
	public Die(L2Character cha)
	{
		_cha = cha;
		if(cha.isPlayer())
		{
			L2Player player = (L2Player) cha;
			_access = player.getPlayerAccess().ResurectFixed ? 0x01 : 0x00;
			_clan = player.getClan();
		}
		_chaId = cha.getObjectId();
		_fake = !cha.isDead();
		if(cha.isMonster())
			_sweepable = ((L2MonsterInstance) cha).isSweepActive();

		if(_clan != null)
		{
			SiegeClan siegeClan = null;
			Siege siege = SiegeManager.getSiege(_cha, true);
			if(siege != null)
				siegeClan = siege.getAttackerClan(_clan);

			if(TerritorySiege.checkIfInZone(_cha))
				siegeClan = TerritorySiege.getSiegeClan(_clan);

			to_hideaway = _clan.getHasHideout() > 0 ? 0x01 : 0x00;
			to_castle = _clan.getHasCastle() > 0 ? 0x01 : 0x00;
			to_siege_HQ = siegeClan != null && siegeClan.getHeadquarter() != null ? 0x01 : 0x00;
			to_fortress = _clan.getHasFortress() > 0 ? 0x01 : 0x00;
		}
		else
		{
			to_hideaway = 0;
			to_castle = 0;
			to_siege_HQ = 0;
			to_fortress = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(_fake)
			return;

		writeC(0x00);
		writeD(_chaId);
		writeD(0x01); // to nearest village
		writeD(to_hideaway); // to hide away
		writeD(to_castle); // to castle
		writeD(to_siege_HQ); // to siege HQ
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(_access); // FIXED
		writeD(to_fortress); // fortress
        // TODO:
		writeC(0); //show die animation
	    writeD(0); //agathion ress button
	    writeD(0); //additional free space
	}
}