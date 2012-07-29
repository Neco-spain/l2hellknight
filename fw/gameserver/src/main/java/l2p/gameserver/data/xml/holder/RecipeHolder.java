package l2p.gameserver.data.xml.holder;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Recipe;
import l2p.gameserver.model.RecipeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class RecipeHolder {
    private static final Logger _log = LoggerFactory.getLogger(RecipeHolder.class);
    private static RecipeHolder _instance;

    private Map<Integer, Recipe> _listByRecipeId;
    private Map<Integer, Recipe> _listByRecipeItem;

    public static RecipeHolder getInstance() {
        if (_instance == null)
            _instance = new RecipeHolder();
        return _instance;
    }

    public RecipeHolder() {
        _listByRecipeId = new HashMap<Integer, Recipe>();
        _listByRecipeItem = new HashMap<Integer, Recipe>();
        Connection con = null;
        PreparedStatement statement = null;
        PreparedStatement st2 = null;
        ResultSet list = null, rset2 = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM recipes");
            st2 = con.prepareStatement("SELECT * FROM `recitems` WHERE `rid`=?");
            list = statement.executeQuery();

            while (list.next()) {
                Vector<RecipeComponent> recipePartList = new Vector<RecipeComponent>();

                boolean isDvarvenCraft = list.getBoolean("dwarven");
                String recipeName = list.getString("name");
                int id = list.getInt("id");
                int recipeId = list.getInt("recid");
                int level = list.getInt("lvl");
                int itemId = list.getInt("item");
                int foundation = list.getInt("foundation");
                int count = list.getInt("q");
                int mpCost = list.getInt("mp");
                int successRate = list.getInt("success");
                long exp = list.getLong("exp");
                long sp = list.getLong("sp");

                //material
                st2.setInt(1, id);
                rset2 = st2.executeQuery();
                while (rset2.next()) {
                    int rpItemId = rset2.getInt("item");
                    int quantity = rset2.getInt("q");
                    RecipeComponent rp = new RecipeComponent(rpItemId, quantity);
                    recipePartList.add(rp);
                }

                Recipe recipeList = new Recipe(id, level, recipeId, recipeName, successRate, mpCost, itemId, foundation, count, exp, sp, isDvarvenCraft);
                for (RecipeComponent recipePart : recipePartList)
                    recipeList.addRecipe(recipePart);
                _listByRecipeId.put(id, recipeList);
                _listByRecipeItem.put(recipeId, recipeList);
            }

            _log.info("RecipeController: Loaded " + _listByRecipeId.size() + " Recipes.");
        } catch (Exception e) {
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(st2, rset2);
            DbUtils.closeQuietly(con, statement, list);
        }
    }

    public Collection<Recipe> getRecipes() {
        return _listByRecipeId.values();
    }

    public Recipe getRecipeByRecipeId(int listId) {
        return _listByRecipeId.get(listId);
    }

    public Recipe getRecipeByRecipeItem(int itemId) {
        return _listByRecipeItem.get(itemId);
    }
}