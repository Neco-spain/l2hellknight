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
package l2.brick.gameserver.model.entity;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.model.actor.L2Summon;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.util.Rnd;

public class TvTRoundEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance = null;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];
	/** Admin removed this player from event */
	private boolean _adminRemove = false;
	
	/**
	 * Initialize the teleporter and start the delayed task<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @param coordinates as int[]<br>
	 * @param fastShedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public TvTRoundEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		
		long delay = Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY * 1000;
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br><br>
	 *
	 * @see java.lang.Runnable#run()<br>
	 */
	public void run()
	{
		if (_playerInstance == null)
			return;
		
		L2Summon summon = _playerInstance.getPet();
		
		if (summon != null)
			summon.unSummon(_playerInstance);
		
		if (Config.TVT_ROUND_EVENT_EFFECTS_REMOVAL == 0
				|| (Config.TVT_ROUND_EVENT_EFFECTS_REMOVAL == 1 && (_playerInstance.getTeam() == 0 || (_playerInstance.isInDuel() && _playerInstance.getDuelState() != Duel.DUELSTATE_INTERRUPTED))))
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		if (_playerInstance.isInDuel())
			_playerInstance.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		
		int TvTRoundInstance = TvTRoundEvent.getTvTRoundEventInstance();
		if (TvTRoundInstance != 0)
		{
			if (TvTRoundEvent.isStarted() && !_adminRemove)
			{
				_playerInstance.setInstanceId(TvTRoundInstance);
			}
			else
			{
				_playerInstance.setInstanceId(0);
			}
		}
		else
		{
			_playerInstance.setInstanceId(0);
		}
		
		_playerInstance.doRevive();
		
		_playerInstance.teleToLocation( _coordinates[ 0 ] + Rnd.get(101)-50, _coordinates[ 1 ] + Rnd.get(101)-50, _coordinates[ 2 ], false );
		
		if (TvTRoundEvent.isStarted() && !_adminRemove)
			_playerInstance.setTeam(TvTRoundEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		else
			_playerInstance.setTeam(0);
		
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}