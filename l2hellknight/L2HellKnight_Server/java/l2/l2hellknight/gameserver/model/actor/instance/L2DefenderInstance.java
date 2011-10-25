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
package l2.hellknight.gameserver.model.actor.instance;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.ai.L2CharacterAI;
import l2.hellknight.gameserver.ai.L2FortSiegeGuardAI;
import l2.hellknight.gameserver.ai.L2SiegeGuardAI;
import l2.hellknight.gameserver.ai.L2SpecialSiegeGuardAI;
import l2.hellknight.gameserver.instancemanager.CastleManager;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.TerritoryWarManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.knownlist.DefenderKnownList;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.clanhall.SiegableHall;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.MyTargetSelected;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.ValidateLocation;
import l2.hellknight.gameserver.templates.L2NpcTemplate;

public class L2DefenderInstance extends L2Attackable
{
	private Castle _castle = null; // the castle which the instance should defend
	private Fort _fort = null; // the fortress which the instance should defend
	private SiegableHall _hall = null; // the siegable hall which the instance should defend
	
	public L2DefenderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DefenderInstance);
	}
	
	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList)super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DefenderKnownList(this));
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized(this)
			{
				if (_ai == null)
				{
					if (getConquerableHall() == null && getCastle(10000) == null)
						_ai = new L2FortSiegeGuardAI(new AIAccessor());
					else if(getCastle(10000) != null)
						_ai = new L2SiegeGuardAI(new AIAccessor());
					else
						_ai = new L2SpecialSiegeGuardAI(new AIAccessor());
				}	
				return _ai;
			}
		}
		return ai;
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR><BR>
	 *
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 *
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by all except defenders
		if (!(attacker instanceof L2Playable))
			return false;
		
		L2PcInstance player = attacker.getActingPlayer();
		
		// Check if siege is in progress
		if ((_fort != null && _fort.getZone().isActive())
				|| (_castle != null && _castle.getZone().isActive())
				|| (_hall != null && _hall.getSiegeZone().isActive()))
		{
			int activeSiegeId = (_fort != null ? _fort.getFortId() : (_castle != null ? _castle.getCastleId() : (_hall != null? _hall.getId() : 0)));
			
			// Check if player is an enemy of this defender npc
			if (player != null && ((player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId))
					|| (player.getSiegeState() == 1 && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))
					|| player.getSiegeState() == 0))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 *
	 */
	@Override
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
			return;
		if (getSpawn() == null) // just in case
			return;
		if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
		{
			if (Config.DEBUG) _log.info(getObjectId()+": moving home");
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		_hall = getConquerableHall();
		if (_fort == null && _castle == null && _hall == null)
			_log.warning("L2DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: "+getNpcId()+ " x="+getX()+ " y="+getY()+ " z="+getZ());
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need
	 * extra check to see if a player should interact or ATTACK them when clicked.
	 * 
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG) _log.info("new target selected:"+getObjectId());
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp() );
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if(!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		//Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2DefenderInstance))
		{
			if (damage == 0 && aggro <= 1 && attacker instanceof L2Playable)
			{
				L2PcInstance player = attacker.getActingPlayer();
				// Check if siege is in progress
				if ((_fort != null && _fort.getZone().isActive())
						|| (_castle != null && _castle.getZone().isActive())
						|| _hall != null && _hall.getSiegeZone().isActive())
				{
					int activeSiegeId = (_fort != null ? _fort.getFortId() : (_castle != null ? _castle.getCastleId() : (_hall != null? _hall.getId() : 0)));
					if (player != null && ((player.getSiegeState() == 2 && player.isRegisteredOnThisSiegeField(activeSiegeId))
							|| (player.getSiegeState() == 1 && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}