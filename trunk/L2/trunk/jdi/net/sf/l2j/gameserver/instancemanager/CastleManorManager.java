package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.ClanWarehouse;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public class CastleManorManager
{
  protected static Logger _log = Logger.getLogger(CastleManorManager.class.getName());
  private static CastleManorManager _instance;
  public static final int PERIOD_CURRENT = 0;
  public static final int PERIOD_NEXT = 1;
  private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
  private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
  private static final int NEXT_PERIOD_APPROVE = Config.ALT_MANOR_APPROVE_TIME;
  private static final int NEXT_PERIOD_APPROVE_MIN = Config.ALT_MANOR_APPROVE_MIN;
  private static final int MANOR_REFRESH = Config.ALT_MANOR_REFRESH_TIME;
  private static final int MANOR_REFRESH_MIN = Config.ALT_MANOR_REFRESH_MIN;
  protected static final long MAINTENANCE_PERIOD = Config.ALT_MANOR_MAINTENANCE_PERIOD;
  private Calendar _manorRefresh;
  private Calendar _periodApprove;
  private boolean _underMaintenance;
  private boolean _disabled;
  protected ScheduledFuture _scheduledManorRefresh;
  protected ScheduledFuture _scheduledMaintenanceEnd;
  protected ScheduledFuture _scheduledNextPeriodapprove;

  public static final CastleManorManager getInstance()
  {
    if (_instance == null) {
      _log.info("Initializing CastleManorManager");
      _instance = new CastleManorManager();
    }
    return _instance;
  }

  private CastleManorManager()
  {
    load();
    init();
    _underMaintenance = false;
    _disabled = (!Config.ALLOW_MANOR);
    boolean isApproved = (_periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis());

    for (Castle c : CastleManager.getInstance().getCastles())
      c.setNextPeriodApproved(isApproved);
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      for (Castle castle : CastleManager.getInstance().getCastles()) {
        FastList production = new FastList();
        FastList productionNext = new FastList();
        FastList procure = new FastList();
        FastList procureNext = new FastList();

        PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
        statement.setInt(1, castle.getCastleId());
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
          int seedId = rs.getInt("seed_id");
          int canProduce = rs.getInt("can_produce");
          int startProduce = rs.getInt("start_produce");
          int price = rs.getInt("seed_price");
          int period = rs.getInt("period");
          if (period == 0)
            production.add(new SeedProduction(seedId, canProduce, price, startProduce));
          else
            productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
        }
        statement.close();
        rs.close();

        castle.setSeedProduction(production, 0);
        castle.setSeedProduction(productionNext, 1);

        statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
        statement.setInt(1, castle.getCastleId());
        rs = statement.executeQuery();
        while (rs.next()) {
          int cropId = rs.getInt("crop_id");
          int canBuy = rs.getInt("can_buy");
          int startBuy = rs.getInt("start_buy");
          int rewardType = rs.getInt("reward_type");
          int price = rs.getInt("price");
          int period = rs.getInt("period");
          if (period == 0)
            procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
          else
            procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
        }
        statement.close();
        rs.close();

        castle.setCropProcure(procure, 0);
        castle.setCropProcure(procureNext, 1);

        if ((!procure.isEmpty()) || (!procureNext.isEmpty()) || (!production.isEmpty()) || (!productionNext.isEmpty()))
        {
          _log.info(castle.getName() + ": Data loaded");
        }
      }
    } catch (Exception e) {
      _log.info("Error restoring manor data: " + e.getMessage()); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void init() {
    _manorRefresh = Calendar.getInstance();
    _manorRefresh.set(11, MANOR_REFRESH);
    _manorRefresh.set(12, MANOR_REFRESH_MIN);

    _periodApprove = Calendar.getInstance();
    _periodApprove.set(11, NEXT_PERIOD_APPROVE);
    _periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);

    updateManorRefresh();
    updatePeriodApprove();
  }

  public void updateManorRefresh() {
    _log.info("Manor System: Manor refresh updated");
    _scheduledManorRefresh = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
      public void run() {
        if (!isDisabled()) {
          setUnderMaintenance(true);
          CastleManorManager._log.info("Manor System: Under maintenance mode started");

          _scheduledMaintenanceEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
            public void run() {
              CastleManorManager._log.info("Manor System: Next period started");
              setNextPeriod();
              try {
                save();
              } catch (Exception e) {
                CastleManorManager._log.info("Manor System: Failed to save manor data: " + e);
              }
              setUnderMaintenance(false);
            }
          }
          , CastleManorManager.MAINTENANCE_PERIOD);
        }

        updateManorRefresh();
      }
    }
    , getMillisToManorRefresh());
  }

  public void updatePeriodApprove()
  {
    _log.info("Manor System: Manor period approve updated");
    _scheduledNextPeriodapprove = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
      public void run() {
        if (!isDisabled()) {
          approveNextPeriod();
          CastleManorManager._log.info("Manor System: Next period approved");
        }
        updatePeriodApprove();
      }
    }
    , getMillisToNextPeriodApprove());
  }

  public long getMillisToManorRefresh()
  {
    if (_manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
      return _manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }
    return setNewManorRefresh();
  }

  public long setNewManorRefresh() {
    _manorRefresh = Calendar.getInstance();
    _manorRefresh.set(11, MANOR_REFRESH);
    _manorRefresh.set(12, MANOR_REFRESH_MIN);
    _manorRefresh.add(11, 24);

    _log.info("Manor System: New Schedule for manor refresh @ " + _manorRefresh.getTime());

    return _manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
  }

  public long getMillisToNextPeriodApprove() {
    if (_periodApprove.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
      return _periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }
    return setNewPeriodApprove();
  }

  public long setNewPeriodApprove() {
    _periodApprove = Calendar.getInstance();
    _periodApprove.set(11, NEXT_PERIOD_APPROVE);
    _periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);
    _periodApprove.add(11, 24);

    _log.info("Manor System: New Schedule for period approve @ " + _periodApprove.getTime());

    return _periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
  }

  public void setNextPeriod() {
    for (Castle c : CastleManager.getInstance().getCastles()) {
      if (c.getOwnerId() <= 0)
        continue;
      L2Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
      if (clan == null) {
        continue;
      }
      ItemContainer cwh = clan.getWarehouse();
      if (!(cwh instanceof ClanWarehouse)) {
        _log.info("Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(c.getOwnerId()));
        return;
      }

      for (CropProcure crop : c.getCropProcure(0)) {
        if (crop.getStartAmount() == 0) {
          continue;
        }
        if (crop.getStartAmount() - crop.getAmount() > 0) {
          int count = crop.getStartAmount() - crop.getAmount();
          count = count * 90 / 100;
          if ((count < 1) && 
            (Rnd.nextInt(99) < 90)) {
            count = 1;
          }
          if (count > 0) {
            cwh.addItem("Manor", L2Manor.getInstance().getMatureCrop(crop.getId()), count, null, null);
          }
        }

        if (crop.getAmount() > 0) {
          c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
        }
      }

      c.setSeedProduction(c.getSeedProduction(1), 0);
      c.setCropProcure(c.getCropProcure(1), 0);

      if (c.getTreasury() < c.getManorCost(0)) {
        c.setSeedProduction(getNewSeedsList(c.getCastleId()), 1);
        c.setCropProcure(getNewCropsList(c.getCastleId()), 1);
      } else {
        FastList production = new FastList();
        for (SeedProduction s : c.getSeedProduction(0)) {
          s.setCanProduce(s.getStartProduce());
          production.add(s);
        }
        c.setSeedProduction(production, 1);

        FastList procure = new FastList();
        for (CropProcure cr : c.getCropProcure(0)) {
          cr.setAmount(cr.getStartAmount());
          procure.add(cr);
        }
        c.setCropProcure(procure, 1);
      }
      if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
        c.saveCropData();
        c.saveSeedData();
      }

      L2PcInstance clanLeader = null;
      if (clan != null)
        clanLeader = L2World.getInstance().getPlayer(clan.getLeader().getName());
      if (clanLeader != null) {
        clanLeader.sendPacket(new SystemMessage(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED));
      }
      c.setNextPeriodApproved(false);
    }
  }

  public void approveNextPeriod() {
    for (Castle c : CastleManager.getInstance().getCastles()) {
      boolean notFunc = false;

      if (c.getOwnerId() <= 0) {
        c.setCropProcure(new FastList(), 1);
        c.setSeedProduction(new FastList(), 1);
      } else if (c.getTreasury() < c.getManorCost(1)) {
        notFunc = true;
        c.setSeedProduction(getNewSeedsList(c.getCastleId()), 1);
        c.setCropProcure(getNewCropsList(c.getCastleId()), 1);
      } else {
        ItemContainer cwh = ClanTable.getInstance().getClan(c.getOwnerId()).getWarehouse();
        if (!(cwh instanceof ClanWarehouse)) {
          _log.info("Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(c.getOwnerId()));
          return;
        }
        int slots = 0;
        for (CropProcure crop : c.getCropProcure(1)) {
          if (crop.getStartAmount() > 0) {
            slots++;
          }
        }
        if (!cwh.validateCapacity(slots)) {
          notFunc = true;
          c.setSeedProduction(getNewSeedsList(c.getCastleId()), 1);
          c.setCropProcure(getNewCropsList(c.getCastleId()), 1);
        }
      }
      c.setNextPeriodApproved(true);
      c.addToTreasuryNoTax(-1 * c.getManorCost(1));

      if (notFunc) {
        L2Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
        L2PcInstance clanLeader = null;
        if (clan != null)
          clanLeader = L2World.getInstance().getPlayer(clan.getLeader().getName());
        if (clanLeader != null)
          clanLeader.sendPacket(new SystemMessage(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION));
      }
    }
  }

  private FastList<SeedProduction> getNewSeedsList(int castleId)
  {
    FastList seeds = new FastList();
    FastList seedsIds = L2Manor.getInstance().getSeedsForCastle(castleId);
    for (Iterator i$ = seedsIds.iterator(); i$.hasNext(); ) { int sd = ((Integer)i$.next()).intValue();
      seeds.add(new SeedProduction(sd));
    }
    return seeds;
  }

  private FastList<CropProcure> getNewCropsList(int castleId) {
    FastList crops = new FastList();
    FastList cropsIds = L2Manor.getInstance().getCropsForCastle(castleId);
    for (Iterator i$ = cropsIds.iterator(); i$.hasNext(); ) { int cr = ((Integer)i$.next()).intValue();
      crops.add(new CropProcure(cr));
    }
    return crops;
  }

  public boolean isUnderMaintenance() {
    return _underMaintenance;
  }

  public void setUnderMaintenance(boolean mode) {
    _underMaintenance = mode;
  }

  public boolean isDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean mode) {
    _disabled = mode;
  }

  public SeedProduction getNewSeedProduction(int id, int amount, int price, int sales) {
    return new SeedProduction(id, amount, price, sales);
  }

  public CropProcure getNewCropProcure(int id, int amount, int type, int price, int buy) {
    return new CropProcure(id, amount, type, buy, price);
  }

  public void save() {
    for (Castle c : CastleManager.getInstance().getCastles()) {
      c.saveSeedData();
      c.saveCropData();
    }
  }

  public class SeedProduction
  {
    int _seedId;
    int _residual;
    int _price;
    int _sales;

    public SeedProduction(int id)
    {
      _seedId = id;
      _sales = 0;
      _price = 0;
      _sales = 0;
    }

    public SeedProduction(int id, int amount, int price, int sales) {
      _seedId = id;
      _residual = amount;
      _price = price;
      _sales = sales;
    }
    public int getId() {
      return _seedId; } 
    public int getCanProduce() { return _residual; } 
    public int getPrice() { return _price; } 
    public int getStartProduce() { return _sales; } 
    public void setCanProduce(int amount) {
      _residual = amount;
    }
  }

  public class CropProcure
  {
    int _cropId;
    int _buyResidual;
    int _rewardType;
    int _buy;
    int _price;

    public CropProcure(int id)
    {
      _cropId = id;
      _buyResidual = 0;
      _rewardType = 0;
      _buy = 0;
      _price = 0;
    }

    public CropProcure(int id, int amount, int type, int buy, int price) {
      _cropId = id;
      _buyResidual = amount;
      _rewardType = type;
      _buy = buy;
      _price = price;
    }
    public int getReward() {
      return _rewardType; } 
    public int getId() { return _cropId; } 
    public int getAmount() { return _buyResidual; } 
    public int getStartAmount() { return _buy; } 
    public int getPrice() { return _price; } 
    public void setAmount(int amount) {
      _buyResidual = amount;
    }
  }
}