/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.model.restriction;

import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.gameserver.Announcements;
import com.l2js.gameserver.datatables.CharCustomTable;
import com.l2js.gameserver.datatables.ModsBufferSchemeTable;
import com.l2js.gameserver.instancemanager.CastleManager;
import com.l2js.gameserver.model.L2Clan;
import com.l2js.gameserver.model.L2ItemInstance;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.actor.appearance.PcAppearance;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.entity.Castle;
import com.l2js.gameserver.model.entity.TvTEvent;
import com.l2js.gameserver.model.entity.event.DMEvent;
import com.l2js.gameserver.model.entity.event.Hitman;
import com.l2js.gameserver.model.entity.event.LMEvent;
import com.l2js.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2js.gameserver.util.Util;

/**
 * @author L0ngh0rn
 */
public class PCRestriction extends AbstractRestriction
{
	private static Logger	_log	= Logger.getLogger(PCRestriction.class.getName());

	private static final class SingletonHolder
	{
		private static final PCRestriction	INSTANCE	= new PCRestriction();
	}

	public static PCRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		checkIllegalEnchant(activeChar);
		checkingEvent(activeChar);
		getCustomWelcome(activeChar);
		loadCustomL2jS(activeChar);
		verifyDonator(activeChar);
		setSystemPvPColor(activeChar);

		ModsBufferSchemeTable.getInstance().loadMyScheme(activeChar);

		sendMsg(activeChar);

		activeChar.broadcastStatusUpdate();
		activeChar.broadcastUserInfo();
		activeChar.broadcastTitleInfo();
	}

	@Override
	public void playerDisconnected(L2PcInstance activeChar)
	{
		ModsBufferSchemeTable.getInstance().destroyMyScheme(activeChar);
		CharCustomTable.getInstance().destroyMyCustom(activeChar);
	}

	private static void checkingEvent(L2PcInstance activeChar)
	{
		if (Config.TVT_EVENT_ENABLED)
			TvTEvent.onLogin(activeChar);
		if (Config.DM_EVENT_ENABLED)
			DMEvent.onLogin(activeChar);
		if (Config.LM_EVENT_ENABLED)
			LMEvent.onLogin(activeChar);
		if (Config.HITMAN_ENABLE_EVENT)
			Hitman.getInstance().onEnterWorld(activeChar);
	}

	private static void getCustomWelcome(L2PcInstance activeChar)
	{
		if (Config.SERVER_WELCOME_MESSAGE_ENABLE)
			activeChar.sendMessage(Config.SERVER_WELCOME_MESSAGE);
		if (Config.ONLINE_PLAYERS_AT_STARTUP)
			activeChar.sendMessage("Online Players: " + L2World.getInstance().getAllPlayersCount(activeChar.isGM()));
		if (Config.SCREEN_WELCOME_MESSAGE_ENABLE)
			activeChar.sendPacket(new ExShowScreenMessage(Config.SCREEN_WELCOME_MESSAGE,
					Config.SCREEN_WELCOME_MESSAGE_TIME));
	}

	private static void loadCustomL2jS(L2PcInstance activeChar)
	{
		CharCustomTable.getInstance().loadMyCustom(activeChar);
	}

	private static void sendMsg(L2PcInstance activeChar)
	{
		String nameChar = activeChar.getName();
		L2Clan clan = activeChar.getClan();

		if (Config.HERO_ANNOUNCE_LOGIN && activeChar.isHero())
			Announcements.getInstance().announceToAll(Config.HERO_MSG_LOGIN.replaceAll("\\$player", nameChar));

		if (Config.CASTLE_LORDS_ANNOUNCE && clan != null && clan.getHasCastle() > 0)
		{
			Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());
			if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
				Announcements.getInstance().announceToAll(
						Config.CASTLE_LORDS_MSG.replaceAll("\\$player", nameChar).replaceAll("\\$castle",
								castle.getName()));
		}
	}

	private void setSystemPvPColor(L2PcInstance activeChar)
	{
		PcAppearance appearance = activeChar.getAppearance();
		if (!activeChar.isGM() && Config.ALLOW_PVP_COLOR_SYSTEM)
		{
			int pvpAmount = activeChar.getPvpKills() + 1;
			Integer indexColor = null;

			if (Config.ALLOW_PVP_COLOR_NAME)
			{
				TreeMap<Integer, Integer> colorName = Config.SYSTEM_PVP_COLOR.getColorName();
				colorName.put(pvpAmount, 0);

				indexColor = colorName.lowerKey(pvpAmount);

				if (indexColor != null)
					appearance.setNameColor(colorName.get(indexColor));
			}

			if (Config.ALLOW_PVP_COLOR_TITLE)
			{
				TreeMap<Integer, Integer> colorTitle = Config.SYSTEM_PVP_COLOR.getColorTitle();
				colorTitle.put(pvpAmount, 0);

				if (indexColor == null)
					indexColor = colorTitle.lowerKey(pvpAmount);

				if (indexColor != null)
					appearance.setTitleColor(colorTitle.get(indexColor));
			}
		}
	}

	private void checkIllegalEnchant(L2PcInstance activeChar)
	{
		if (!activeChar.isGM() && Config.ALLOW_VALID_ENCHANT)
		{
			for (L2ItemInstance i : activeChar.getInventory().getItems())
			{
				if (i.isArmor() && i.getEnchantLevel() > Config.ENCHANT_MAX_ARMOR)
					actionIllegalEnchant(activeChar, i, "Armor");
				else if (i.isWeapon() && i.getEnchantLevel() > Config.ENCHANT_MAX_WEAPON)
					actionIllegalEnchant(activeChar, i, "Weapon");
			}
		}
	}

	public static void actionIllegalEnchant(L2PcInstance activeChar, L2ItemInstance i, String cat)
	{
		if (Config.DESTROY_ENCHANT_ITEM)
		{
			activeChar.getInventory().destroyItem("Over Enchant", i, activeChar, null);
			_log.log(Level.WARNING, "Player: " + activeChar.getName() + " use illegal item (" + cat + "). Item: " + i
					+ " was deleted!");
		}
		else
			_log.log(Level.WARNING, "Player: " + activeChar.getName() + " use illegal item (" + cat + "). Item: " + i
					+ "!");
		activeChar.sendMessage("[Server]: You have Items over enchanted!");
		if (Config.PUNISH_PLAYER)
			Util.handleIllegalPlayerAction(activeChar, "Player: " + activeChar.getName() + " use illegal item (" + cat
					+ "). Item: " + i + "!", Config.DEFAULT_PUNISH);
	}

	public static void verifyDonator(L2PcInstance activeChar)
	{
		if (activeChar.isDonator())
		{
			if (Config.DONATOR_SEE_NAME_COLOR)
				activeChar.getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);

			if (Config.DONATOR_SEE_TITLE_COLOR)
				activeChar.getAppearance().setTitleColor(Config.DONATOR_TITLE_COLOR);

			activeChar.sendMessage(Config.DONATOR_WELCOME_MESSAGE);
		}
	}
}
