package scripts.script;

import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.LevelUpData;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2World;

public abstract interface EngineInterface
{
  public static final CharNameTable charNametable = CharNameTable.getInstance();

  public static final IdFactory idFactory = IdFactory.getInstance();
  public static final ItemTable itemTable = ItemTable.getInstance();

  public static final SkillTable skillTable = SkillTable.getInstance();

  public static final RecipeController recipeController = RecipeController.getInstance();

  public static final SkillTreeTable skillTreeTable = SkillTreeTable.getInstance();
  public static final CharTemplateTable charTemplates = CharTemplateTable.getInstance();
  public static final ClanTable clanTable = ClanTable.getInstance();

  public static final NpcTable npcTable = NpcTable.getInstance();

  public static final TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
  public static final LevelUpData levelUpData = LevelUpData.getInstance();
  public static final L2World world = L2World.getInstance();
  public static final SpawnTable spawnTable = SpawnTable.getInstance();
  public static final GameTimeController gameTimeController = GameTimeController.getInstance();
  public static final Announcements announcements = Announcements.getInstance();
  public static final MapRegionTable mapRegions = MapRegionTable.getInstance();

  public abstract void addQuestDrop(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, String paramString, String[] paramArrayOfString);

  public abstract void addEventDrop(int[] paramArrayOfInt1, int[] paramArrayOfInt2, double paramDouble, DateRange paramDateRange);

  public abstract void onPlayerLogin(String[] paramArrayOfString, DateRange paramDateRange);
}