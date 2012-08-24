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
package events.HideAndSeek;

import java.util.concurrent.ScheduledFuture;

import l2.hellknight.Config;
import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.ExShowScreenMessage;

import javolution.util.FastMap;

/**
 * @author BiggBoss
 */
public final class Engine
{
	
	protected StatsSet _holder = null;
	protected int _curClue = 0;
	protected ScheduledFuture<?> _clueTask = null;
	protected ScheduledFuture<?> _endGame = null;
	
	// Npcs
	private L2Spawn _regNpc = null;
	private L2Spawn _hideNpc = null;
	
	protected Engine()
	{
	}
	
	protected void setHolder(StatsSet set)
	{
		_holder = set;
	}
	
	protected void launchGame()
	{
		ThreadPoolManager.getInstance().executeTask(new Registration());
	}
	
	protected void endGame(L2PcInstance winner)
	{
		_endGame.cancel(true);
		ThreadPoolManager.getInstance().executeTask(new EndGame(true));
		Announcements.getInstance().announceToAll(winner.getName() + " is the winner! Congratulations!");
		
		final ExShowScreenMessage msg = new ExShowScreenMessage("Congratulations, you won!!!", 15000);
		winner.sendPacket(msg);
		
		final FastMap<Integer, Long> rewards = new FastMap<>();
		for (final String reward : _holder.getString("rewards").split(";"))
		{
			try
			{
				final String[] split = reward.split(",");
				final int itmId = Integer.valueOf(split[0]);
				final long amount = Long.valueOf(split[1]);
				rewards.put(itmId, amount);
			}
			catch (final Exception e)
			{
				System.out.println("Hide and Seek: Wrong reward format for event npc " + _holder.getInteger("npc"));
				winner.sendMessage("An error ocourred. Contact staff asking for your reward");
			}
		}
		
		for (final int itemId : rewards.keySet())
		{
			winner.addItem("Hide and Seek", itemId, rewards.get(itemId), winner, true);
		}
	}
	
	private void spawnRegistrationNpc()
	{
		try
		{
			_regNpc = new L2Spawn(NpcTable.getInstance().getTemplate(HideAndSeek.REG_NPC));
			_regNpc.setLocx(HideAndSeek.REG_NPC_COORDS[0]);
			_regNpc.setLocy(HideAndSeek.REG_NPC_COORDS[1]);
			_regNpc.setLocz(HideAndSeek.REG_NPC_COORDS[2]);
			_regNpc.setHeading(HideAndSeek.REG_NPC_COORDS[3]);
			_regNpc.setAmount(1);
			_regNpc.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_regNpc, false);
			_regNpc.init();
			
		}
		catch (final Exception e)
		{
			System.out.println("Hide and Seek: Error Spawning registration npc!");
			e.printStackTrace();
		}
	}
	
	private void unspawnRegistrationNpc()
	{
		_regNpc.stopRespawn();
		_regNpc.getLastSpawn().deleteMe();
		SpawnTable.getInstance().deleteSpawn(_regNpc, false);
		_regNpc = null;
	}
	
	protected void spawnHideNpc()
	{
		try
		{
			_hideNpc = new L2Spawn(NpcTable.getInstance().getTemplate(_holder.getInteger("npc")));
			_hideNpc.setLocx(_holder.getInteger("x"));
			_hideNpc.setLocy(_holder.getInteger("y"));
			_hideNpc.setLocz(_holder.getInteger("z"));
			_hideNpc.setHeading(1);
			_hideNpc.setAmount(1);
			_hideNpc.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_hideNpc, false);
			_hideNpc.init();
			_hideNpc.getLastSpawn().setTitle("Hide and Seek");
			
		}
		catch (final Exception e)
		{
			System.out.println("Hide and Seek: Error Spawning registration npc!");
			e.printStackTrace();
		}
	}
	
	protected void unspawnHideNpc()
	{
		_hideNpc.stopRespawn();
		_hideNpc.getLastSpawn().deleteMe();
		SpawnTable.getInstance().deleteSpawn(_hideNpc, false);
		_hideNpc = null;
	}
	
	protected class Registration implements Runnable
	{
		@Override
		public void run()
		{
			spawnRegistrationNpc();
			final int mins = 10;
			Announcements.getInstance().announceToAll("Hide and Seek is now active! You have " + mins + " minute(s) to register!");
			for (int i = mins; i > 0; i--)
			{
				Announcements.getInstance().announceToAll(i + " minute(s) left to register!");
				try
				{
					Thread.sleep(60000);
				}
				catch (final InterruptedException ie)
				{
					if (Config.DEBUG)
						ie.printStackTrace();
				}
			}
			Announcements.getInstance().announceToAll("Registration is Over!");
			if (HideAndSeek._players.size() == 0 || (HideAndSeek.MIN_PLAYERS != -1 && HideAndSeek._players.size() < HideAndSeek.MIN_PLAYERS))
				Announcements.getInstance().announceToAll("Event cancelled due lack of participants");
			else
			{
				ThreadPoolManager.getInstance().executeTask(new Game());
				HideAndSeek._canRegister = false;
				final long end = ExternalConfig.HAS_EVENT_MINS_DURATION * 60000;
				final long delay = end / 3;
				_clueTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ClueTask(), 3000, delay);
				_endGame = ThreadPoolManager.getInstance().scheduleGeneral(new EndGame(false), end);
			}
			unspawnRegistrationNpc();
		}
	}
	
	private class Game implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Hide And Seek starts now! Go find the npc!");
			spawnHideNpc();
		}
	}
	
	private class ClueTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_curClue > 2)
			{
				_clueTask.cancel(true);
				return;
			}
			final String clue = _holder.getString(_curClue + "_clue");
			HideAndSeek.clue(clue);
			_curClue++;
		}
	}
	
	private class EndGame implements Runnable
	{
		
		private boolean winner = false;
		
		protected EndGame(boolean winner)
		{
			this.winner = winner;
		}
		
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Hide and Seek event is over!");
			if (!winner)
			{
				Announcements.getInstance().announceToAll("Noone found the hide npc!");
			}
			unspawnHideNpc();
			HideAndSeek._players.clear();
		}
	}
}
