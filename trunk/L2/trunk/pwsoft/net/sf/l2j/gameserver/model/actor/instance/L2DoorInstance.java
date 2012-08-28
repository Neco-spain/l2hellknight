package net.sf.l2j.gameserver.model.actor.instance;

import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2DoorAI;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.knownlist.DoorKnownList;
import net.sf.l2j.gameserver.model.actor.stat.DoorStat;
import net.sf.l2j.gameserver.model.actor.status.DoorStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

public class L2DoorInstance extends L2Character
{
  protected static final Logger log = Logger.getLogger(L2DoorInstance.class.getName());

  private int _castleIndex = -2;
  private int _mapRegion = -1;

  private int _rangeXMin = 0;
  private int _rangeYMin = 0;
  private int _rangeZMin = 0;
  private int _rangeXMax = 0;
  private int _rangeYMax = 0;
  private int _rangeZMax = 0;
  protected final int _doorId;
  protected final String _name;
  private boolean _open;
  private boolean _unlockable;
  private ClanHall _clanHall;
  protected int _autoActionDelay = -1;
  private ScheduledFuture<?> _autoActionTask;
  private Line2D _border;

  public L2CharacterAI getAI()
  {
    if (_ai == null) {
      _ai = new L2DoorAI(new AIAccessor());
    }
    return _ai;
  }

  public boolean hasAI()
  {
    return _ai != null;
  }

  public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
  {
    super(objectId, template);
    getKnownList();
    getStat();
    getStatus();
    _doorId = doorId;
    _name = name;
    _unlockable = unlockable;
  }

  public final DoorKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof DoorKnownList)))
      setKnownList(new DoorKnownList(this));
    return (DoorKnownList)super.getKnownList();
  }

  public final DoorStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof DoorStat)))
      setStat(new DoorStat(this));
    return (DoorStat)super.getStat();
  }

  public final DoorStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof DoorStatus)))
      setStatus(new DoorStatus(this));
    return (DoorStatus)super.getStatus();
  }

  public final boolean isUnlockable()
  {
    return _unlockable;
  }

  public final int getLevel()
  {
    return 1;
  }

  public int getDoorId()
  {
    return _doorId;
  }

  public boolean getOpen()
  {
    return _open;
  }

  public void setOpen(boolean open)
  {
    _open = open;
  }

  public void setAutoActionDelay(int actionDelay)
  {
    if (_autoActionDelay == actionDelay) {
      return;
    }
    if (actionDelay > -1) {
      AutoOpenClose ao = new AutoOpenClose();
      ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
    }
    else if (_autoActionTask != null) {
      _autoActionTask.cancel(false);
    }

    _autoActionDelay = actionDelay;
  }

  public int getDamage()
  {
    int dmg = 6 - (int)Math.ceil(getCurrentHp() / getMaxHp() * 6.0D);
    if (dmg > 6)
      return 6;
    if (dmg < 0)
      return 0;
    return dmg;
  }

  public final Castle getCastle()
  {
    if (_castleIndex < 0) _castleIndex = CastleManager.getInstance().getCastleIndex(this);
    if (_castleIndex < 0) return null;
    return (Castle)CastleManager.getInstance().getCastles().get(_castleIndex);
  }

  public void setClanHall(ClanHall clanhall) {
    _clanHall = clanhall;
  }

  public ClanHall getClanHall() {
    return _clanHall;
  }

  public boolean isEnemyOf(L2Character cha)
  {
    return true;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    if (attacker == null) {
      return false;
    }
    return isAttackable(attacker instanceof L2SiegeSummonInstance);
  }

  public boolean isAttackable(boolean golem)
  {
    if (getCastle() == null) {
      return false;
    }
    boolean siege = getCastle().getSiege().getIsInProgress();
    if ((isWall()) && (!golem)) {
      siege = false;
    }
    return siege;
  }

  public boolean isAttackable()
  {
    return isAttackable(false);
  }

  public void updateAbnormalEffect()
  {
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if (!object.isPlayer())
      return 0;
    return 2000;
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    if (!object.isPlayer()) {
      return 0;
    }
    return 4000;
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public L2Weapon getActiveWeaponItem()
  {
    return null;
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    return null;
  }

  public void onAction(L2PcInstance player)
  {
    if (player == null) {
      return;
    }

    if (this != player.getTarget())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new DoorStatusUpdate(this, true));

      player.sendPacket(new ValidateLocation(this));
    }
    else if (isAutoAttackable(player))
    {
      if (Math.abs(player.getZ() - getZ()) < 400)
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
    }
    else if (canOpen(player))
    {
      if (!isInsideRadius(player, 150, false, false)) {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
      }
      else if (!getOpen())
        openMe();
      else {
        closeMe();
      }

    }

    player.sendActionFailed();
  }

  public boolean canOpen(L2PcInstance player)
  {
    if (player.getClan() == null) {
      return false;
    }
    ClanHall ch = null;
    switch (_doorId)
    {
    case 25170003:
    case 25170004:
    case 25170005:
    case 25170006:
      ch = ClanHallManager.getInstance().getClanHallById(34);
      break;
    case 21170003:
    case 21170004:
    case 21170005:
    case 21170006:
      ch = ClanHallManager.getInstance().getClanHallById(64);
      break;
    case 24140001:
    case 24140002:
    case 24140005:
    case 24140006:
      ch = ClanHallManager.getInstance().getClanHallById(62);
      break;
    default:
      ch = _clanHall;
    }

    if (ch == null) {
      return false;
    }
    return (player.isGM()) || (player.getClanId() == ch.getOwnerId());
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

      if (isAutoAttackable(player)) {
        DoorStatusUpdate su = new DoorStatusUpdate(this, true);
        player.sendPacket(su);
      }

      NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
      TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
      html1.append("<tr><td>S.Y.L. Says:</td></tr>");
      html1.append("<tr><td>Current HP  " + getCurrentHp() + "</td></tr>");
      html1.append("<tr><td>Max HP      " + getMaxHp() + "</td></tr>");

      html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
      html1.append("<tr><td>Door ID:<br>" + getDoorId() + "</td></tr>");
      html1.append("<tr><td><br></td></tr>");

      html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
      html1.append("<tr><td><br></td></tr>");
      html1.append("</table>");

      html1.append("<table><tr>");
      html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
      html1.append("</tr></table></body></html>");

      html.setHtml(html1.toString());
      player.sendPacket(html);
    }

    player.sendActionFailed();
  }

  public void broadcastStatusUpdate()
  {
    FastList players = getKnownList().getKnownPlayersInRadius(2200);
    if ((players == null) || (players.isEmpty()))
      return;
    L2PcInstance pc = null;
    DoorStatusUpdate su = new DoorStatusUpdate(this, true);
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; )
    {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
        continue;
      pc.sendPacket(su);
    }
    pc = null;
  }

  public void onOpen()
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000L);
  }

  public void onClose()
  {
    closeMe();
  }

  public final void closeMe()
  {
    setOpen(false);
    broadcastStatusUpdate();
  }

  public final void openMe()
  {
    setOpen(true);
    broadcastStatusUpdate();
  }

  public String toString()
  {
    return "door " + _doorId;
  }

  public String getDoorName()
  {
    return _name;
  }

  public int getXMin()
  {
    return _rangeXMin;
  }

  public int getYMin()
  {
    return _rangeYMin;
  }

  public int getZMin()
  {
    return _rangeZMin;
  }

  public int getXMax()
  {
    return _rangeXMax;
  }

  public int getYMax()
  {
    return _rangeYMax;
  }

  public int getZMax()
  {
    return _rangeZMax;
  }

  public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
  {
    _rangeXMin = xMin;
    _rangeYMin = yMin;
    _rangeZMin = zMin;

    _rangeXMax = xMax;
    _rangeYMax = yMax;
    _rangeZMax = zMax;

    setBorder(new Line2D.Double(_rangeXMin, _rangeYMin, _rangeXMax, _rangeYMax));
  }

  public int getMapRegion()
  {
    return _mapRegion;
  }

  public void setMapRegion(int region)
  {
    _mapRegion = region;
  }

  public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
  {
    FastList result = new FastList();

    for (L2Object obj : getKnownList().getKnownObjects().values())
    {
      if (obj.isL2SiegeGuard()) result.add((L2SiegeGuardInstance)obj);
    }

    return result;
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    return false;
  }

  public boolean isWall()
  {
    switch (_doorId)
    {
    case 19210003:
    case 19210004:
    case 20160007:
    case 20160008:
    case 20160009:
    case 20220003:
    case 20220004:
    case 22130005:
    case 22190003:
    case 22190004:
    case 23220003:
    case 23220004:
    case 23250003:
    case 23250004:
    case 24160021:
    case 24160022:
    case 24180003:
    case 24180006:
    case 24180011:
      return true;
    }
    return false;
  }

  public void setBorder(Line2D line)
  {
    _border = line;
  }

  public Line2D getBorder() {
    return _border;
  }

  public boolean intersectsLine(int x, int y, int z, int tx, int ty, int tz) {
    if ((getCurrentHp() < 0.5D) || (getXMax() == 0)) {
      return false;
    }
    if (getOpen()) {
      return false;
    }
    if (((z < _rangeZMin) || (z > _rangeZMax)) && ((tz < _rangeZMin) || (tz > _rangeZMax))) {
      return false;
    }
    return _border.intersectsLine(x, y, tx, ty);
  }

  public boolean isL2Door()
  {
    return true;
  }

  public int getRegeneratePeriod()
  {
    return 300000;
  }

  class AutoOpenClose
    implements Runnable
  {
    AutoOpenClose()
    {
    }

    public void run()
    {
      try
      {
        if (!getOpen()) {
          String doorAction = "opened";
          openMe();
        }
        else {
          String doorAction = "closed";
          closeMe();
        }

      }
      catch (Exception e)
      {
        L2DoorInstance.log.warning("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
      }
    }
  }

  class CloseTask
    implements Runnable
  {
    CloseTask()
    {
    }

    public void run()
    {
      try
      {
        onClose();
      }
      catch (Throwable e)
      {
        L2DoorInstance.log.log(Level.SEVERE, "", e);
      }
    }
  }

  public class AIAccessor extends L2Character.AIAccessor
  {
    protected AIAccessor()
    {
      super();
    }
    public L2DoorInstance getActor() { return L2DoorInstance.this;
    }

    public void moveTo(int x, int y, int z, int offset)
    {
    }

    public void moveTo(int x, int y, int z)
    {
    }

    public void stopMove(L2CharPosition pos)
    {
    }

    public void doAttack(L2Character target)
    {
    }

    public void doCast(L2Skill skill)
    {
    }
  }
}