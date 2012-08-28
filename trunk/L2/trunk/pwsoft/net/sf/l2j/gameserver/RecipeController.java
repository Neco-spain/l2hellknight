package net.sf.l2j.gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2RecipeInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.RecipeBookItemList;
import net.sf.l2j.gameserver.network.serverpackets.RecipeItemMakeInfo;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopItemInfo;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class RecipeController
{
  protected static final Logger _log = AbstractLogger.getLogger(RecipeController.class.getName());
  private static RecipeController _instance;
  private Map<Integer, L2RecipeList> _lists;
  protected static final Map<L2PcInstance, RecipeItemMaker> _activeMakers = Collections.synchronizedMap(new WeakHashMap());

  public static RecipeController getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new RecipeController();
    _instance.load();
  }

  public void load()
  {
    _lists = new FastMap();
    String line = null;
    LineNumberReader lnr = null;
    try
    {
      File recipesData = new File(Config.DATAPACK_ROOT, "data/recipes.csv");
      lnr = new LineNumberReader(new BufferedReader(new FileReader(recipesData)));

      while ((line = lnr.readLine()) != null)
      {
        if ((line.trim().length() == 0) || (line.startsWith("#"))) {
          continue;
        }
        parseList(line);
      }

      _log.config("RecipeController: Loaded " + _lists.size() + " Recipes.");
    }
    catch (Exception e)
    {
      if (lnr != null)
      {
        _log.log(Level.WARNING, "error while creating recipe controller in linenr: " + lnr.getLineNumber(), e);
      }
      else
      {
        _log.warning("No recipes were found in data folder");
      }
    }
    finally
    {
      try
      {
        lnr.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public int getRecipesCount()
  {
    return _lists.size();
  }

  public L2RecipeList getRecipeList(int listId)
  {
    return (L2RecipeList)_lists.get(Integer.valueOf(listId));
  }

  public L2RecipeList getRecipeByItemId(int itemId)
  {
    for (int i = 0; i < _lists.size(); i++)
    {
      L2RecipeList find = (L2RecipeList)_lists.get(Integer.valueOf(i));
      if (find.getRecipeId() == itemId)
        return find;
    }
    return null;
  }

  public L2RecipeList getRecipeById(int recId)
  {
    for (int i = 0; i < _lists.size(); i++)
    {
      L2RecipeList find = (L2RecipeList)_lists.get(Integer.valueOf(i));
      if (find.getId() == recId)
        return find;
    }
    return null;
  }

  public synchronized void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
  {
    RecipeItemMaker maker = null;
    if (Config.ALT_GAME_CREATION) maker = (RecipeItemMaker)_activeMakers.get(player);

    if (maker == null)
    {
      RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
      response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());

      player.sendPacket(response);
      return;
    }

    player.sendPacket(Static.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
  }

  public synchronized void requestMakeItemAbort(L2PcInstance player)
  {
    _activeMakers.remove(player);
  }

  public synchronized void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
  {
    L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

    if (recipeList == null) return;

    List dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
    List commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());

    if ((!dwarfRecipes.contains(recipeList)) && (!commonRecipes.contains(recipeList)))
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
      return;
    }
    RecipeItemMaker maker;
    if ((Config.ALT_GAME_CREATION) && ((maker = (RecipeItemMaker)_activeMakers.get(manufacturer)) != null))
    {
      player.sendMessage("Manufacturer is busy, please try later.");
      return;
    }

    RecipeItemMaker maker = new RecipeItemMaker(manufacturer, recipeList, player);
    if (maker._isValid)
    {
      if (Config.ALT_GAME_CREATION)
      {
        _activeMakers.put(manufacturer, maker);
        ThreadPoolManager.getInstance().scheduleGeneral(maker, 100L);
      } else {
        maker.run();
      }
    }
    manufacturer.sendChanges();
  }

  public synchronized void requestMakeItem(L2PcInstance player, int recipeListId)
  {
    if (player.isInDuel())
    {
      player.sendPacket(Static.CANT_CRAFT_DURING_COMBAT);
      return;
    }

    L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

    if (recipeList == null) return;

    List dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
    List commonRecipes = Arrays.asList(player.getCommonRecipeBook());

    if ((!dwarfRecipes.contains(recipeList)) && (!commonRecipes.contains(recipeList)))
    {
      Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
      return;
    }
    RecipeItemMaker maker;
    if ((Config.ALT_GAME_CREATION) && ((maker = (RecipeItemMaker)_activeMakers.get(player)) != null))
    {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_S2).addString("You are busy creating ").addItemName(recipeList.getItemId()));
      return;
    }

    RecipeItemMaker maker = new RecipeItemMaker(player, recipeList, player);
    if (maker._isValid)
    {
      if (Config.ALT_GAME_CREATION)
      {
        _activeMakers.put(player, maker);
        ThreadPoolManager.getInstance().scheduleGeneral(maker, 100L);
      } else {
        maker.run();
      }
    }
  }

  private void parseList(String line)
  {
    try
    {
      StringTokenizer st = new StringTokenizer(line, ";");
      List recipePartList = new FastList();

      String recipeTypeString = st.nextToken();
      boolean isDwarvenRecipe;
      if (recipeTypeString.equalsIgnoreCase("dwarven")) { isDwarvenRecipe = true;
      }
      else
      {
        boolean isDwarvenRecipe;
        if (recipeTypeString.equalsIgnoreCase("common")) { isDwarvenRecipe = false;
        } else
        {
          _log.warning("Error parsing recipes.csv, unknown recipe type " + recipeTypeString);
          return;
        }
      }
      boolean isDwarvenRecipe;
      String recipeName = st.nextToken();
      int id = Integer.parseInt(st.nextToken());
      int recipeId = Integer.parseInt(st.nextToken());
      int level = Integer.parseInt(st.nextToken());

      StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
      while (st2.hasMoreTokens())
      {
        StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
        int rpItemId = Integer.parseInt(st3.nextToken());
        int quantity = Integer.parseInt(st3.nextToken());
        L2RecipeInstance rp = new L2RecipeInstance(rpItemId, quantity);
        recipePartList.add(rp);
      }

      int itemId = Integer.parseInt(st.nextToken());
      int count = Integer.parseInt(st.nextToken());

      st.nextToken();

      int mpCost = Integer.parseInt(st.nextToken());
      int successRate = Integer.parseInt(st.nextToken());

      L2RecipeList recipeList = new L2RecipeList(id, level, recipeId, recipeName, successRate, mpCost, itemId, count, isDwarvenRecipe);

      for (L2RecipeInstance recipePart : recipePartList)
      {
        recipeList.addRecipe(recipePart);
      }
      _lists.put(Integer.valueOf(_lists.size()), recipeList);
    }
    catch (Exception e)
    {
      _log.severe("Exception in RecipeController.parseList() - " + e);
    }
  }

  private L2RecipeList getValidRecipeList(L2PcInstance player, int id)
  {
    L2RecipeList recipeList = getRecipeList(id - 1);

    if ((recipeList == null) || (recipeList.getRecipes().length == 0))
    {
      player.sendMessage("No recipe for: " + id);
      player.isInCraftMode(false);
      return null;
    }
    return recipeList;
  }

  private static class RecipeItemMaker
    implements Runnable
  {
    protected boolean _isValid;
    protected List<TempItem> _items = null;
    protected final L2RecipeList _recipeList;
    protected final L2PcInstance _player;
    protected final L2PcInstance _target;
    protected final L2Skill _skill;
    protected final int _skillId;
    protected final int _skillLevel;
    protected double _creationPasses;
    protected double _manaRequired;
    protected int _price;
    protected int _totalItems;
    protected int _materialsRefPrice;
    protected int _delay;

    public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
    {
      _player = pPlayer;
      _target = pTarget;
      _recipeList = pRecipeList;

      _isValid = false;
      _skillId = (_recipeList.isDwarvenRecipe() ? 172 : 1320);

      _skillLevel = _player.getSkillLevel(_skillId);
      _skill = _player.getKnownSkill(_skillId);

      _player.isInCraftMode(true);

      if (_player.isAlikeDead())
      {
        _player.sendMessage("Dead people don't craft.");
        _player.sendActionFailed();
        abort();
        return;
      }

      if (_target.isAlikeDead())
      {
        _target.sendMessage("Dead customers can't use manufacture.");
        _target.sendActionFailed();
        abort();
        return;
      }

      if (_target.isTransactionInProgress())
      {
        _target.sendMessage("You are busy.");
        _target.sendActionFailed();
        abort();
        return;
      }

      if (_player.isTransactionInProgress())
      {
        if (_player != _target)
        {
          _target.sendMessage(new StringBuilder().append("Manufacturer ").append(_player.getName()).append(" is busy.").toString());
        }
        _player.sendActionFailed();
        abort();
        return;
      }

      if ((_recipeList == null) || (_recipeList.getRecipes().length == 0))
      {
        _player.sendMessage("No such recipe");
        _player.sendActionFailed();
        abort();
        return;
      }

      _manaRequired = _recipeList.getMpCost();

      if (_recipeList.getLevel() > _skillLevel)
      {
        _player.sendMessage(new StringBuilder().append("Need skill level ").append(_recipeList.getLevel()).toString());
        _player.sendActionFailed();
        abort();
        return;
      }

      if (_player != _target)
      {
        for (L2ManufactureItem temp : _player.getCreateList().getList()) {
          if (temp.getRecipeId() == _recipeList.getId())
          {
            _price = temp.getCost();
            if (_target.getAdena() >= _price)
              break;
            _target.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
            abort();
            return;
          }

        }

      }

      if ((this._items = listItems(false)) == null)
      {
        abort();
        return;
      }

      for (TempItem i : _items)
      {
        _materialsRefPrice += i.getReferencePrice() * i.getQuantity();
        _totalItems += i.getQuantity();
      }

      if (_player.getCurrentMp() < _manaRequired)
      {
        _target.sendPacket(Static.NOT_ENOUGH_MP);
        abort();
        return;
      }

      _creationPasses = (_totalItems / _skillLevel + (_totalItems % _skillLevel != 0 ? 1 : 0));

      if ((Config.ALT_GAME_CREATION) && (_creationPasses != 0.0D)) {
        _manaRequired /= _creationPasses;
      }
      updateMakeInfo(true);
      updateCurMp();
      updateCurLoad();

      _player.isInCraftMode(false);
      _isValid = true;
    }

    public void run()
    {
      if (!Config.IS_CRAFTING_ENABLED)
      {
        _target.sendMessage("Item creation is currently disabled.");
        abort();
        return;
      }

      if ((_player == null) || (_target == null))
      {
        RecipeController._log.warning(new StringBuilder().append("player or target == null (disconnected?), aborting").append(_target).append(_player).toString());
        abort();
        return;
      }

      if ((_player.isOnline() == 0) || (_target.isOnline() == 0))
      {
        RecipeController._log.warning(new StringBuilder().append("player or target is not online, aborting ").append(_target).append(_player).toString());
        abort();
        return;
      }

      if ((Config.ALT_GAME_CREATION) && (RecipeController._activeMakers.get(_player) == null))
      {
        if (_target != _player)
        {
          _target.sendMessage("Manufacture aborted");
          _player.sendMessage("Manufacture aborted");
        }
        else
        {
          _player.sendMessage("Item creation aborted");
        }

        abort();
        return;
      }

      if ((Config.ALT_GAME_CREATION) && (!_items.isEmpty()))
      {
        if (!validateMp()) return;
        _player.reduceCurrentMp(_manaRequired);
        updateCurMp();

        grabSomeItems();

        if (!_items.isEmpty())
        {
          _delay = ((int)(Config.ALT_GAME_CREATION_SPEED * _player.getMReuseRate(_skill) * 10.0D / Config.RATE_CONSUMABLE_COST) * 100);

          _player.broadcastPacket(new MagicSkillUser(_player, _skillId, _skillLevel, _delay, 0));
          _player.sendPacket(new SetupGauge(0, _delay));
          ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
        }
        else
        {
          _player.sendPacket(new SetupGauge(0, _delay));
          try
          {
            Thread.sleep(_delay);
          } catch (InterruptedException e) {
          } finally {
            finishCrafting();
          }
        }
      } else {
        finishCrafting();
      }
    }

    private void finishCrafting() {
      if (!Config.ALT_GAME_CREATION) _player.reduceCurrentMp(_manaRequired);

      if ((_target != _player) && (_price > 0))
      {
        L2ItemInstance adenatransfer = _target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);

        if (adenatransfer == null)
        {
          _target.sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
          abort();
          return;
        }
      }

      if ((this._items = listItems(true)) != null)
      {
        if (Rnd.get(100) < _recipeList.getSuccessRate())
        {
          rewardPlayer();
          updateMakeInfo(true);
        }
        else
        {
          _player.sendMessage("Item(s) failed to create");
          if (_target != _player) {
            _target.sendMessage("Item(s) failed to create");
          }
          updateMakeInfo(false);
        }
      }
      updateCurMp();
      updateCurLoad();
      RecipeController._activeMakers.remove(_player);
      _player.isInCraftMode(false);
      _target.sendItems(false);
    }

    private void updateMakeInfo(boolean success) {
      if (_target == _player) _target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
      else
        _target.sendPacket(new RecipeShopItemInfo(_player.getObjectId(), _recipeList.getId()));
    }

    private void updateCurLoad()
    {
      StatusUpdate su = new StatusUpdate(_target.getObjectId());
      su.addAttribute(14, _target.getCurrentLoad());
      _target.sendPacket(su);
    }

    private void updateCurMp()
    {
      StatusUpdate su = new StatusUpdate(_target.getObjectId());
      su.addAttribute(11, (int)_target.getCurrentMp());
      _target.sendPacket(su);
    }

    private void grabSomeItems()
    {
      int numItems = _skillLevel;

      while ((numItems > 0) && (!_items.isEmpty()))
      {
        TempItem item = (TempItem)_items.get(0);

        int count = item.getQuantity();
        if (count >= numItems) count = numItems;

        item.setQuantity(item.getQuantity() - count);
        if (item.getQuantity() <= 0) _items.remove(0); else {
          _items.set(0, item);
        }
        numItems -= count;

        if (_target == _player)
          _player.sendPacket(SystemMessage.id(SystemMessageId.S1_S2_EQUIPPED).addNumber(count).addItemName(item.getItemId()));
        else
          _target.sendMessage(new StringBuilder().append("Manufacturer ").append(_player.getName()).append(" used ").append(count).append(" ").append(item.getItemName()).toString());
      }
    }

    private boolean validateMp()
    {
      if (_player.getCurrentMp() < _manaRequired)
      {
        if (Config.ALT_GAME_CREATION)
        {
          _player.sendPacket(new SetupGauge(0, _delay));
          ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
        }
        else
        {
          _target.sendPacket(Static.NOT_ENOUGH_MP);
          abort();
        }
        return false;
      }
      return true;
    }

    private List<TempItem> listItems(boolean remove)
    {
      L2RecipeInstance[] recipes = _recipeList.getRecipes();
      Inventory inv = _target.getInventory();
      List materials = new FastList();

      for (L2RecipeInstance recipe : recipes)
      {
        int quantity = _recipeList.isConsumable() ? (int)(recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : recipe.getQuantity();

        if (quantity <= 0)
          continue;
        L2ItemInstance item = inv.getItemByItemId(recipe.getItemId());

        if ((item == null) || (item.getCount() < quantity))
        {
          _target.sendMessage(new StringBuilder().append("You dont have the right elements for making this item").append((_recipeList.isConsumable()) && (Config.RATE_CONSUMABLE_COST != 1.0F) ? new StringBuilder().append(".\nDue to server rates you need ").append(Config.RATE_CONSUMABLE_COST).append("x more material than listed in recipe").toString() : "").toString());

          abort();
          return null;
        }

        TempItem temp = new TempItem(item, quantity);
        materials.add(temp);
      }

      if (remove)
      {
        for (TempItem tmp : materials)
        {
          inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);
        }
      }
      return materials;
    }

    private void abort()
    {
      updateMakeInfo(false);
      _player.isInCraftMode(false);
      RecipeController._activeMakers.remove(_player);
    }

    private void rewardPlayer()
    {
      int itemId = _recipeList.getItemId();
      int itemCount = _recipeList.getCount();

      L2ItemInstance createdItem = _target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);

      SystemMessage sm = null;
      if (itemCount > 1)
        sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount);
      else
        sm = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(itemId);
      _target.sendPacket(sm);

      if (_target != _player)
      {
        _player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ADENA).addNumber(_price));
      }

      if (Config.ALT_GAME_CREATION)
      {
        int recipeLevel = _recipeList.getLevel();
        int exp = createdItem.getReferencePrice() * itemCount;

        if (exp < 0) exp = 0;

        exp /= recipeLevel;
        for (int i = _skillLevel; i > recipeLevel; i--) {
          exp /= 4;
        }
        int sp = exp / 10;

        _player.addExpAndSp((int)_player.calcStat(Stats.EXPSP_RATE, exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null), (int)_player.calcStat(Stats.EXPSP_RATE, sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
      }

      updateMakeInfo(true);
    }

    private class TempItem
    {
      private int _itemId;
      private int _quantity;
      private int _ownerId;
      private int _referencePrice;
      private String _itemName;

      public TempItem(L2ItemInstance item, int quantity)
      {
        _itemId = item.getItemId();
        _quantity = quantity;
        _ownerId = item.getOwnerId();
        _itemName = item.getItem().getName();
        _referencePrice = item.getReferencePrice();
      }

      public int getQuantity()
      {
        return _quantity;
      }

      public void setQuantity(int quantity)
      {
        _quantity = quantity;
      }

      public int getReferencePrice()
      {
        return _referencePrice;
      }

      public int getItemId()
      {
        return _itemId;
      }

      public int getOwnerId()
      {
        return _ownerId;
      }

      public String getItemName()
      {
        return _itemName;
      }
    }
  }
}