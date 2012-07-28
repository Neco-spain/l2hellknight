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
package l2.brick.gameserver.datatables;

import gnu.trove.map.hash.TIntIntHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.idfactory.IdFactory;
import l2.brick.gameserver.model.L2SummonItem;
import l2.brick.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2.brick.gameserver.model.actor.instance.L2SummonInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.serverpackets.PetItemList;
import l2.brick.gameserver.skills.l2skills.L2SkillSummon;
import l2.brick.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Nyaran
 */
public class CharSummonTable
{
	private static Logger _log = Logger.getLogger(CharSummonTable.class.getName());
	
	private static final String INIT_SUMMONS = "SELECT ownerId, summonSkillId FROM character_summons";
	private static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
	
	private static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?)";
	private static final String LOAD_SUMMON = "SELECT curHp, curMp, time FROM character_summons WHERE ownerId = ? AND summonSkillId = ?";
	private static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ?";
	
	private static final TIntIntHashMap _servitors = new TIntIntHashMap();
	private static final TIntIntHashMap _pets = new TIntIntHashMap();
	
	public static CharSummonTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void init()
	{
		int ownerId;
		int refId;
		
		Connection con = null;
		
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INIT_SUMMONS);
				ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					ownerId = rset.getInt("ownerId");
					refId = rset.getInt("summonSkillId");
					
					_servitors.put(ownerId, refId);
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error while loading saved summons", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		
		if (Config.RESTORE_PET_ON_RECONNECT)
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INIT_PET);
				ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					ownerId = rset.getInt("ownerId");
					refId = rset.getInt("item_obj_id");
					
					_pets.put(ownerId, refId);
				}
				
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error while loading saved summons", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	public TIntIntHashMap getServitors()
	{
		return _servitors;
	}
	
	public TIntIntHashMap getPets()
	{
		return _pets;
	}
	
	public void saveSummon(L2SummonInstance summon)
	{
		if (summon == null || summon.getTimeRemaining() <= 0)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SAVE_SUMMON);
			
			statement.setInt(1, summon.getOwner().getObjectId());
			statement.setInt(2, summon.getReferenceSkill());
			statement.setInt(3, (int) Math.round(summon.getCurrentHp()));
			statement.setInt(4, (int) Math.round(summon.getCurrentMp()));
			statement.setInt(5, summon.getTimeRemaining());
			
			statement.execute();
			statement.close();
			
			_servitors.put(summon.getOwner().getObjectId(), summon.getReferenceSkill());
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed to store summon [SummonId: " + summon.getNpcId() + "] from Char [CharId: " + summon.getOwner().getObjectId() + "] data", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restoreServitor(L2PcInstance activeChar)
	{
		Connection con = null;
		try
		{
			int skillId = _servitors.get(activeChar.getObjectId());
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(LOAD_SUMMON);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, skillId);
			ResultSet rset = statement.executeQuery();
			
			L2NpcTemplate summonTemplate;
			L2SummonInstance summon;
			L2SkillSummon skill;
			
			while (rset.next())
			{
				int curHp = rset.getInt("curHp");
				int curMp = rset.getInt("curMp");
				int time = rset.getInt("time");
				
				skill = (L2SkillSummon) SkillTable.getInstance().getInfo(skillId, activeChar.getSkillLevel(skillId));
				if (skill == null)
				{
					removeServitor(activeChar);
					return;
				}
				
				summonTemplate = NpcTable.getInstance().getTemplate(skill.getNpcId());
				if (summonTemplate == null)
				{
					_log.warning("[CharSummonTable] Summon attemp for nonexisting Skill ID:" + skillId);
					return;
				}
				
				final int id = IdFactory.getInstance().getNextId();
				if (summonTemplate.isType("L2SiegeSummon"))
				{
					summon = new L2SiegeSummonInstance(id, summonTemplate, activeChar, skill);
				}
				else if (summonTemplate.isType("L2MerchantSummon"))
				{
					// TODO: Confirm L2Merchant summon = new L2MerchantSummonInstance(id, summonTemplate, activeChar, skill);
					summon = new L2SummonInstance(id, summonTemplate, activeChar, skill);
				}
				else
				{
					summon = new L2SummonInstance(id, summonTemplate, activeChar, skill);
				}
				
				summon.setName(summonTemplate.getName());
				summon.setTitle(activeChar.getName());
				summon.setExpPenalty(skill.getExpPenalty());
				
				if (summon.getLevel() >= ExperienceTable.getInstance().getMaxPetLevel())
				{
					summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(ExperienceTable.getInstance().getMaxPetLevel()-1));
					_log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above "+ExperienceTable.getInstance().getMaxPetLevel()+". Please rectify.");
				}
				else
				{
					summon.getStat().setExp(ExperienceTable.getInstance().getExpForLevel(summon.getLevel() % ExperienceTable.getInstance().getMaxPetLevel()));
				}
				summon.setCurrentHp(curHp);
				summon.setCurrentMp(curMp);
				summon.setHeading(activeChar.getHeading());
				summon.setRunning();
				if (!(summon instanceof L2MerchantSummonInstance))
					activeChar.setPet(summon);
				
				summon.setTimeRemaining(time);
				
				//L2World.getInstance().storeObject(summon);
				summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "[CharSummonTable]: Summon cannot be restored: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void removeServitor(L2PcInstance activeChar)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REMOVE_SUMMON);
			statement.setInt(1, activeChar.getObjectId());
			
			statement.execute();
			statement.close();
			_servitors.remove(activeChar.getObjectId());
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "[CharSummonTable]: Summon cannot be removed: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restorePet(L2PcInstance activeChar)
	{
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_pets.get(activeChar.getObjectId()));
		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(sitem.getNpcId());
		
		if (npcTemplate == null)
			return;
		
		final L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
		if (petSummon == null)
			return;
		
		petSummon.setShowSummonAnimation(true);
		petSummon.setTitle(activeChar.getName());
		
		if (!petSummon.isRespawned())
		{
			petSummon.setCurrentHp(petSummon.getMaxHp());
			petSummon.setCurrentMp(petSummon.getMaxMp());
			petSummon.getStat().setExp(petSummon.getExpForThisLevel());
			petSummon.setCurrentFed(petSummon.getMaxFed());
		}
		
		petSummon.setRunning();
		
		if (!petSummon.isRespawned())
			petSummon.store();
		
		activeChar.setPet(petSummon);
		
		petSummon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
		petSummon.startFeed();
		item.setEnchantLevel(petSummon.getLevel());
		
		if (petSummon.getCurrentFed() <= 0)
			petSummon.unSummon(activeChar);
		else
			petSummon.startFeed();
		
		petSummon.setFollowStatus(true);
		
		petSummon.getOwner().sendPacket(new PetItemList(petSummon));
		petSummon.broadcastStatusUpdate();
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharSummonTable _instance = new CharSummonTable();
	}
}
