package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiegeDatabase;
import l2rt.gameserver.network.serverpackets.ExShowDominionRegistry;

// ddd
public class RequestExJoinDominionWar extends L2GameClientPacket
{
	private int _terrId;
	private int _registrationType; // 0 - merc; 1 - clan
	private int _requestType; // 1 - регистрация; 0 - отмена регистрации

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || TerritorySiege.isInProgress())
			return;

		// Регистрация кончается за 2 часа до старта ТВ
		long timeRemaining = TerritorySiege.getSiegeDate().getTimeInMillis() - System.currentTimeMillis();
		if(timeRemaining <= 7200000 || TerritorySiege.isInProgress())
		{
			activeChar.sendPacket(Msg.IT_IS_NOT_A_TERRITORY_WAR_REGISTRATION_PERIOD_SO_A_REQUEST_CANNOT_BE_MADE_AT_THIS_TIME);
			return;
		}

		if(activeChar.getLevel() < 40 || activeChar.getClassId().getLevel() < 3)
		{
			activeChar.sendPacket(Msg.ONLY_CHARACTERS_WHO_ARE_LEVEL_40_OR_ABOVE_WHO_HAVE_COMPLETED_THEIR_SECOND_CLASS_TRANSFER_CAN);
			return;
		}

		// Персональная регистрация
		if(_registrationType == 0)
		{
			if(_requestType == 1)
			{
				int registerdTerrId = TerritorySiege.getTerritoryForPlayer(activeChar.getObjectId());
				if(registerdTerrId != -1 && registerdTerrId != _terrId)
				{
					activeChar.sendPacket(Msg.YOU_VE_ALREADY_REQUESTED_A_TERRITORY_WAR_IN_ANOTHER_TERRITORY_ELSEWHERE);
					return;
				}

				TerritorySiege.getPlayers().putIfAbsent(activeChar.getObjectId(), _terrId);
			}
			else
				TerritorySiege.getPlayers().remove(activeChar.getObjectId());

			TerritorySiegeDatabase.saveSiegeMember(activeChar.getObjectId(), _terrId, 0);
		}
		else
		{
			L2Clan clan = activeChar.getClan();
			if(clan == null)
				return;

			// Клановая регистрация
			if((activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
			{
				activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			if(clan.getHasCastle() > 0)
			{
				activeChar.sendMessage("Клан владеющий замком автоматически подписан на войны земель.");
				return;
			}

			if(_requestType == 1)
			{
				int registerdTerrId = TerritorySiege.getTerritoryForClan(clan.getClanId());
				if(registerdTerrId != 0 && registerdTerrId != _terrId)
				{
					activeChar.sendMessage("Ваш клан уже зарегистрированы на на другую территорию.");
					return;
				}

				// Зарегистрироваться
				TerritorySiege.getClans().putIfAbsent(new SiegeClan(clan.getClanId(), null), _terrId);
				TerritorySiegeDatabase.saveSiegeMember(clan.getClanId(), _terrId, 1);
			}
			else
			{
				// Отказаться
				SiegeClan siegeClan = TerritorySiege.getSiegeClan(clan);
				if(siegeClan != null)
				{
					TerritorySiege.getClans().remove(siegeClan);
					TerritorySiegeDatabase.saveSiegeMember(siegeClan.getClanId(), _terrId, 1);
				}
			}
		}
		activeChar.sendPacket(new ExShowDominionRegistry(activeChar, _terrId));
	}

	@Override
	public void readImpl()
	{
		_terrId = readD();
		_registrationType = readD();
		_requestType = readD();
	}
}