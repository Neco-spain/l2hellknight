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
package com.l2js.gameserver.handler;

import gnu.trove.TIntObjectHashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.gameserver.handler.bypasshandlers.*;

/**
 * @author nBd
 */
public class BypassHandler
{
	private static Logger _log = Logger.getLogger(BypassHandler.class.getName());

	private TIntObjectHashMap<IBypassHandler> _datatable;

	public static BypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private BypassHandler()
	{
		_datatable = new TIntObjectHashMap<IBypassHandler>();
		registerBypassHandler(new Augment());
		registerBypassHandler(new BloodAlliance());
		registerBypassHandler(new Buy());
		registerBypassHandler(new BuyShadowItem());
		registerBypassHandler(new ChatLink());
		registerBypassHandler(new ClanWarehouse());
		registerBypassHandler(new CPRecovery());
		registerBypassHandler(new DrawHenna());
		registerBypassHandler(new Festival());
		registerBypassHandler(new FortSiege());
		registerBypassHandler(new Freight());
		registerBypassHandler(new ItemAuctionLink());
		registerBypassHandler(new Link());
		registerBypassHandler(new Loto());
		registerBypassHandler(new ManorManager());
		registerBypassHandler(new Multisell());
		registerBypassHandler(new Observation());
		registerBypassHandler(new OlympiadManagerLink());
		registerBypassHandler(new OlympiadObservation());
		registerBypassHandler(new PlayerHelp());
		registerBypassHandler(new PrivateWarehouse());
		registerBypassHandler(new QuestLink());
		registerBypassHandler(new QuestList());
		registerBypassHandler(new ReceivePremium());
		registerBypassHandler(new ReleaseAttribute());
		registerBypassHandler(new RemoveDeathPenalty());
		registerBypassHandler(new RemoveHennaList());
		registerBypassHandler(new RentPet());
		registerBypassHandler(new RideWyvern());
		registerBypassHandler(new Rift());
		registerBypassHandler(new SkillList());
		registerBypassHandler(new SupportBlessing());
		registerBypassHandler(new SupportMagic());
		registerBypassHandler(new TerritoryStatus());
		registerBypassHandler(new TerritoryWar());
		registerBypassHandler(new VoiceCommand());
		registerBypassHandler(new Wear());
		_log.info("Loaded " + size() + "  BypassHandlers");
	}

	public void registerBypassHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			if (Config.DEBUG)
				_log.log(Level.FINE, "Adding handler for command " + element);

			_datatable.put(element.toLowerCase().hashCode(), handler);
		}
	}

	public IBypassHandler getBypassHandler(String BypassCommand)
	{
		String command = BypassCommand;

		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}

		if (Config.DEBUG)
			_log.log(Level.FINE,
					"getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));

		return _datatable.get(command.toLowerCase().hashCode());
	}

	public int size()
	{
		return _datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BypassHandler _instance = new BypassHandler();
	}
}