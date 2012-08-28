package l2m.gameserver.model.instances;

import java.util.concurrent.Future;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.htm.HtmCache;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.SetSummonRemainTime;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public class SummonInstance extends Summon
{
  public static final long serialVersionUID = 1L;
  public final int CYCLE = 5000;
  private int _summonSkillId;
  private double _expPenalty = 0.0D;
  private int _itemConsumeIdInTime;
  private int _itemConsumeCountInTime;
  private int _itemConsumeDelay;
  private Future<?> _disappearTask;
  private int _consumeCountdown;
  private int _lifetimeCountdown;
  private int _maxLifetime;

  public SummonInstance(int objectId, NpcTemplate template, Player owner, int lifetime, int consumeid, int consumecount, int consumedelay, Skill skill)
  {
    super(objectId, template, owner);
    setName(template.name);
    _lifetimeCountdown = (this._maxLifetime = lifetime);
    _itemConsumeIdInTime = consumeid;
    _itemConsumeCountInTime = consumecount;
    _consumeCountdown = (this._itemConsumeDelay = consumedelay);
    _summonSkillId = skill.getDisplayId();
    _disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(), 5000L);
  }

  public HardReference<SummonInstance> getRef()
  {
    return super.getRef();
  }

  public final int getLevel()
  {
    return getTemplate() != null ? getTemplate().level : 0;
  }

  public int getSummonType()
  {
    return 1;
  }

  public int getCurrentFed()
  {
    return _lifetimeCountdown;
  }

  public int getMaxFed()
  {
    return _maxLifetime;
  }

  public void setExpPenalty(double expPenalty)
  {
    _expPenalty = expPenalty;
  }

  public double getExpPenalty()
  {
    return _expPenalty;
  }

  protected void onDeath(Creature killer)
  {
    super.onDeath(killer);

    saveEffects();

    if (_disappearTask != null)
    {
      _disappearTask.cancel(false);
      _disappearTask = null;
    }
  }

  public int getItemConsumeIdInTime()
  {
    return _itemConsumeIdInTime;
  }

  public int getItemConsumeCountInTime()
  {
    return _itemConsumeCountInTime;
  }

  public int getItemConsumeDelay()
  {
    return _itemConsumeDelay;
  }

  protected synchronized void stopDisappear()
  {
    if (_disappearTask != null)
    {
      _disappearTask.cancel(false);
      _disappearTask = null;
    }
  }

  public void unSummon()
  {
    stopDisappear();
    super.unSummon();
  }

  public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
  {
    Player owner = getPlayer();
    if (owner == null)
      return;
    if (crit)
      owner.sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
    if (miss)
      owner.sendPacket(new SystemMessage(2265).addName(this));
    else if (!target.isInvul())
      owner.sendPacket(new SystemMessage(2261).addName(this).addName(target).addNumber(damage));
  }

  public void displayReceiveDamageMessage(Creature attacker, int damage)
  {
    Player owner = getPlayer();
    owner.sendPacket(new SystemMessage(2262).addName(this).addName(attacker).addNumber(damage));
  }

  public int getEffectIdentifier()
  {
    return _summonSkillId;
  }

  public boolean isSummon()
  {
    return true;
  }

  public void onAction(Player player, boolean shift)
  {
    super.onAction(player, shift);
    if (shift)
    {
      if (!player.getPlayerAccess().CanViewChar) {
        return;
      }

      String dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2SummonInstance.onActionShift.htm", player);
      dialog = dialog.replaceFirst("%name%", String.valueOf(getName()));
      dialog = dialog.replaceFirst("%level%", String.valueOf(getLevel()));
      dialog = dialog.replaceFirst("%class%", String.valueOf(getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
      dialog = dialog.replaceFirst("%xyz%", getLoc().x + " " + getLoc().y + " " + getLoc().z);
      dialog = dialog.replaceFirst("%heading%", String.valueOf(getLoc().h));

      dialog = dialog.replaceFirst("%owner%", String.valueOf(getPlayer().getName()));
      dialog = dialog.replaceFirst("%ownerId%", String.valueOf(getPlayer().getObjectId()));

      dialog = dialog.replaceFirst("%npcId%", String.valueOf(getNpcId()));
      dialog = dialog.replaceFirst("%expPenalty%", String.valueOf(getExpPenalty()));

      dialog = dialog.replaceFirst("%maxHp%", String.valueOf(getMaxHp()));
      dialog = dialog.replaceFirst("%maxMp%", String.valueOf(getMaxMp()));
      dialog = dialog.replaceFirst("%currHp%", String.valueOf((int)getCurrentHp()));
      dialog = dialog.replaceFirst("%currMp%", String.valueOf((int)getCurrentMp()));

      dialog = dialog.replaceFirst("%pDef%", String.valueOf(getPDef(null)));
      dialog = dialog.replaceFirst("%mDef%", String.valueOf(getMDef(null, null)));
      dialog = dialog.replaceFirst("%pAtk%", String.valueOf(getPAtk(null)));
      dialog = dialog.replaceFirst("%mAtk%", String.valueOf(getMAtk(null, null)));
      dialog = dialog.replaceFirst("%accuracy%", String.valueOf(getAccuracy()));
      dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(getEvasionRate(null)));
      dialog = dialog.replaceFirst("%crt%", String.valueOf(getCriticalHit(null, null)));
      dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(getRunSpeed()));
      dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(getWalkSpeed()));
      dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(getPAtkSpd()));
      dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(getMAtkSpd()));
      dialog = dialog.replaceFirst("%dist%", String.valueOf((int)getRealDistance(player)));

      dialog = dialog.replaceFirst("%STR%", String.valueOf(getSTR()));
      dialog = dialog.replaceFirst("%DEX%", String.valueOf(getDEX()));
      dialog = dialog.replaceFirst("%CON%", String.valueOf(getCON()));
      dialog = dialog.replaceFirst("%INT%", String.valueOf(getINT()));
      dialog = dialog.replaceFirst("%WIT%", String.valueOf(getWIT()));
      dialog = dialog.replaceFirst("%MEN%", String.valueOf(getMEN()));

      NpcHtmlMessage msg = new NpcHtmlMessage(5);
      msg.setHtml(dialog);
      player.sendPacket(msg);
    }
  }

  public long getWearedMask()
  {
    return WeaponTemplate.WeaponType.SWORD.mask();
  }

  class Lifetime extends RunnableImpl
  {
    Lifetime()
    {
    }

    public void runImpl()
      throws Exception
    {
      Player owner = getPlayer();
      if (owner == null)
      {
        SummonInstance.access$002(SummonInstance.this, null);
        unSummon();
        return;
      }

      int usedtime = isInCombat() ? 5000 : 1250;
      SummonInstance.access$120(SummonInstance.this, usedtime);

      if (_lifetimeCountdown <= 0)
      {
        owner.sendPacket(Msg.SERVITOR_DISAPPEASR_BECAUSE_THE_SUMMONING_TIME_IS_OVER);
        SummonInstance.access$002(SummonInstance.this, null);
        unSummon();
        return;
      }

      SummonInstance.access$220(SummonInstance.this, usedtime);
      if ((_itemConsumeIdInTime > 0) && (_itemConsumeCountInTime > 0) && (_consumeCountdown <= 0)) {
        if (owner.getInventory().destroyItemByItemId(getItemConsumeIdInTime(), getItemConsumeCountInTime()))
        {
          SummonInstance.access$202(SummonInstance.this, _itemConsumeDelay);
          owner.sendPacket(new SystemMessage(1029).addItemName(getItemConsumeIdInTime()));
        }
        else
        {
          owner.sendPacket(Msg.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_WILL_DISAPPEAR);
          unSummon();
        }
      }
      owner.sendPacket(new SetSummonRemainTime(SummonInstance.this));

      SummonInstance.access$002(SummonInstance.this, ThreadPoolManager.getInstance().schedule(this, 5000L));
    }
  }
}