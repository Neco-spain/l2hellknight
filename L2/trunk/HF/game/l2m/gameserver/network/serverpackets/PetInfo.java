package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.model.items.PetInventory;
import l2m.gameserver.data.tables.PetDataTable;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public class PetInfo extends L2GameServerPacket
{
  private int _runSpd;
  private int _walkSpd;
  private int MAtkSpd;
  private int PAtkSpd;
  private int pvp_flag;
  private int karma;
  private int rideable;
  private int _type;
  private int obj_id;
  private int npc_id;
  private int runing;
  private int incombat;
  private int dead;
  private int _sp;
  private int level;
  private int _abnormalEffect;
  private int _abnormalEffect2;
  private int curFed;
  private int maxFed;
  private int curHp;
  private int maxHp;
  private int curMp;
  private int maxMp;
  private int curLoad;
  private int maxLoad;
  private int PAtk;
  private int PDef;
  private int MAtk;
  private int MDef;
  private int Accuracy;
  private int Evasion;
  private int Crit;
  private int sps;
  private int ss;
  private int type;
  private int _showSpawnAnimation;
  private Location _loc;
  private double col_redius;
  private double col_height;
  private long exp;
  private long exp_this_lvl;
  private long exp_next_lvl;
  private String _name;
  private String title;
  private TeamType _team;

  public PetInfo(Summon summon)
  {
    _type = summon.getSummonType();
    obj_id = summon.getObjectId();
    npc_id = summon.getTemplate().npcId;
    _loc = summon.getLoc();
    MAtkSpd = summon.getMAtkSpd();
    PAtkSpd = summon.getPAtkSpd();
    _runSpd = summon.getRunSpeed();
    _walkSpd = summon.getWalkSpeed();
    col_redius = summon.getColRadius();
    col_height = summon.getColHeight();
    runing = (summon.isRunning() ? 1 : 0);
    incombat = (summon.isInCombat() ? 1 : 0);
    dead = (summon.isAlikeDead() ? 1 : 0);
    _name = (summon.getName().equalsIgnoreCase(summon.getTemplate().name) ? "" : summon.getName());
    title = summon.getTitle();
    pvp_flag = summon.getPvpFlag();
    karma = summon.getKarma();
    curFed = summon.getCurrentFed();
    maxFed = summon.getMaxFed();
    curHp = (int)summon.getCurrentHp();
    maxHp = summon.getMaxHp();
    curMp = (int)summon.getCurrentMp();
    maxMp = summon.getMaxMp();
    _sp = summon.getSp();
    level = summon.getLevel();
    exp = summon.getExp();
    exp_this_lvl = summon.getExpForThisLevel();
    exp_next_lvl = summon.getExpForNextLevel();
    curLoad = (summon.isPet() ? summon.getInventory().getTotalWeight() : 0);
    maxLoad = summon.getMaxLoad();
    PAtk = summon.getPAtk(null);
    PDef = summon.getPDef(null);
    MAtk = summon.getMAtk(null, null);
    MDef = summon.getMDef(null, null);
    Accuracy = summon.getAccuracy();
    Evasion = summon.getEvasionRate(null);
    Crit = summon.getCriticalHit(null, null);
    _abnormalEffect = summon.getAbnormalEffect();
    _abnormalEffect2 = summon.getAbnormalEffect2();

    if (summon.getPlayer().getTransformation() != 0)
      rideable = 0;
    else
      rideable = (PetDataTable.isMountable(npc_id) ? 1 : 0);
    _team = summon.getTeam();
    ss = summon.getSoulshotConsumeCount();
    sps = summon.getSpiritshotConsumeCount();
    _showSpawnAnimation = summon.getSpawnAnimation();
    type = summon.getFormId();
  }

  public PetInfo update()
  {
    _showSpawnAnimation = 1;
    return this;
  }

  protected final void writeImpl()
  {
    writeC(178);
    writeD(_type);
    writeD(obj_id);
    writeD(npc_id + 1000000);
    writeD(0);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
    writeD(0);
    writeD(MAtkSpd);
    writeD(PAtkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeF(1.0D);
    writeF(1.0D);
    writeF(col_redius);
    writeF(col_height);
    writeD(0);
    writeD(0);
    writeD(0);
    writeC(1);
    writeC(runing);
    writeC(incombat);
    writeC(dead);
    writeC(_showSpawnAnimation);
    writeD(-1);
    writeS(_name);
    writeD(-1);
    writeS(title);
    writeD(1);
    writeD(pvp_flag);
    writeD(karma);
    writeD(curFed);
    writeD(maxFed);
    writeD(curHp);
    writeD(maxHp);
    writeD(curMp);
    writeD(maxMp);
    writeD(_sp);
    writeD(level);
    writeQ(exp);
    writeQ(exp_this_lvl);
    writeQ(exp_next_lvl);
    writeD(curLoad);
    writeD(maxLoad);
    writeD(PAtk);
    writeD(PDef);
    writeD(MAtk);
    writeD(MDef);
    writeD(Accuracy);
    writeD(Evasion);
    writeD(Crit);
    writeD(_runSpd);
    writeD(PAtkSpd);
    writeD(MAtkSpd);
    writeD(_abnormalEffect);
    writeH(rideable);
    writeC(0);
    writeH(0);
    writeC(_team.ordinal());
    writeD(ss);
    writeD(sps);
    writeD(type);
    writeD(_abnormalEffect2);
  }
}