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
package l2.hellknight.gameserver.script;

import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.RecipeController;
import l2.hellknight.gameserver.datatables.CharNameTable;
import l2.hellknight.gameserver.datatables.CharTemplateTable;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.LevelUpData;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreesData;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.datatables.TeleportLocationTable;
import l2.hellknight.gameserver.idfactory.IdFactory;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.model.L2World;

/**
 * @author Luis Arias
 */
public interface EngineInterface
{
	//*  keep the references of Singletons to prevent garbage collection
	public CharNameTable charNametable = CharNameTable.getInstance();
	
	public IdFactory idFactory = IdFactory.getInstance();
	public ItemTable itemTable = ItemTable.getInstance();
	
	public SkillTable skillTable = SkillTable.getInstance();
	
	public RecipeController recipeController = RecipeController.getInstance();
	
	public SkillTreesData skillTreeTable = SkillTreesData.getInstance();
	public CharTemplateTable charTemplates = CharTemplateTable.getInstance();
	public ClanTable clanTable = ClanTable.getInstance();
	
	public NpcTable npcTable = NpcTable.getInstance();
	
	public TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
	public LevelUpData levelUpData = LevelUpData.getInstance();
	public L2World world = L2World.getInstance();
	public SpawnTable spawnTable = SpawnTable.getInstance();
	public GameTimeController gameTimeController = GameTimeController.getInstance();
	public Announcements announcements = Announcements.getInstance();
	public MapRegionManager mapRegions = MapRegionManager.getInstance();
	
	
	
	//public ArrayList getAllPlayers();
	//public Player getPlayer(String characterName);
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range);
	public void onPlayerLogin(String[] message, DateRange range);
	
}
