package l2p.gameserver.model.instances;

import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SetSummonRemainTime;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2p.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;


public class SummonInstance extends Summon {
    public final int CYCLE = 5000; // in millis
    private int _summonSkillId;
    private int _summonSkillLvl;
    private double _expPenalty = 0;

    private Future<?> _disappearTask;

    private int _lifetimeCountdown;
    private int _maxLifetime;
    private int _summonPoint;

    public SummonInstance(int objectId, NpcTemplate template, Player owner, int lifetime, int summonPoint, Skill skill) {
        super(objectId, template, owner);
        setName(template.name);
        _lifetimeCountdown = _maxLifetime = lifetime;
        _summonSkillId = skill.getDisplayId();
        _summonSkillLvl = skill.getLevel();
        _disappearTask = ThreadPoolManager.getInstance().schedule(new Lifetime(), CYCLE);
        _summonPoint = summonPoint;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HardReference<SummonInstance> getRef() {
        return (HardReference<SummonInstance>) super.getRef();
    }

    @Override
    public final int getLevel() {
        return getTemplate() != null ? getTemplate().level : 0;
    }

    @Override
    public int getSummonType() {
        return 1;
    }

    @Override
    public int getCurrentFed() {
        return _lifetimeCountdown;
    }

    @Override
    public int getMaxFed() {
        return _maxLifetime;
    }

    public void setExpPenalty(double expPenalty) {
        _expPenalty = expPenalty;
    }

    @Override
    public double getExpPenalty() {
        return _expPenalty;
    }

    class Lifetime extends RunnableImpl {
        @Override
        public void runImpl() throws Exception {
            Player owner = getPlayer();
            if (owner == null) {
                _disappearTask = null;
                unSummon();
                return;
            }

            int usedtime = isInCombat() ? CYCLE : CYCLE / 4;
            _lifetimeCountdown -= usedtime;

            if (_lifetimeCountdown <= 0) {
                owner.sendPacket(Msg.SERVITOR_DISAPPEASR_BECAUSE_THE_SUMMONING_TIME_IS_OVER);
                _disappearTask = null;
                unSummon();
                return;
            }

            owner.sendPacket(new SetSummonRemainTime(SummonInstance.this));

            _disappearTask = ThreadPoolManager.getInstance().schedule(this, CYCLE);
        }
    }

    @Override
    protected void onDeath(Creature killer) {
        super.onDeath(killer);

        saveEffects();

        if (_disappearTask != null) {
            _disappearTask.cancel(false);
            _disappearTask = null;
        }
    }

    protected synchronized void stopDisappear() {
        if (_disappearTask != null) {
            _disappearTask.cancel(false);
            _disappearTask = null;
        }
    }

    @Override
    public void unSummon() {
        stopDisappear();
        super.unSummon();
    }

    @Override
    public int getSummonPoint() {
        return _summonPoint;
    }

    @Override
    public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic) {
        Player owner = getPlayer();
        if (owner == null)
            return;
        if (crit)
            owner.sendPacket(SystemMsg.SUMMONED_MONSTERS_CRITICAL_HIT);
        if (miss)
            owner.sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
        else if (!target.isInvul())
            owner.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(target).addNumber(damage));
    }

    @Override
    public void displayReceiveDamageMessage(Creature attacker, int damage) {
        Player owner = getPlayer();
        owner.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RECEIVED_DAMAGE_OF_S3_FROM_C2).addName(this).addName(attacker).addNumber((long) damage));
    }

    @Override
    public int getSummonSkillId() {
        return _summonSkillId;
    }

    @Override
    public int getSummonSkillLvl() {
        return _summonSkillLvl;
    }

    @Override
    public boolean isServitor() {
        return true;
    }

    @Override
    public void onAction(Player player, boolean shift) {
        super.onAction(player, shift);
        if (shift) {
            if (!player.getPlayerAccess().CanViewChar)
                return;

            String dialog;
            dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2SummonInstance.onActionShift.htm", player);
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
            dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) getCurrentHp()));
            dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) getCurrentMp()));

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
            dialog = dialog.replaceFirst("%dist%", String.valueOf((int) getRealDistance(player)));

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

    @Override
    public long getWearedMask() {
        return WeaponType.SWORD.mask(); // TODO: читать пассивки и смотреть тип оружия и брони там
    }
}