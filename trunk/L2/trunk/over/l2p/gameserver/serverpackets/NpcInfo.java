package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public class NpcInfo extends L2GameServerPacket
{
  private boolean can_writeImpl = false;
  private int _npcObjId;
  private int _npcId;
  private int running;
  private int incombat;
  private int dead;
  private int _showSpawnAnimation;
  private int _runSpd;
  private int _walkSpd;
  private int _mAtkSpd;
  private int _pAtkSpd;
  private int _rhand;
  private int _lhand;
  private int _enchantEffect;
  private int karma;
  private int pvp_flag;
  private int _abnormalEffect;
  private int _abnormalEffect2;
  private int clan_id;
  private int clan_crest_id;
  private int ally_id;
  private int ally_crest_id;
  private int _formId;
  private int _titleColor;
  private double colHeight;
  private double colRadius;
  private double currentColHeight;
  private double currentColRadius;
  private boolean _isAttackable;
  private boolean _isNameAbove;
  private boolean isFlying;
  private Location _loc;
  private String _name = "";
  private String _title = "";
  private boolean _showName;
  private int _state;
  private NpcString _nameNpcString = NpcString.NONE;
  private NpcString _titleNpcString = NpcString.NONE;
  private TeamType _team;

  public NpcInfo(NpcInstance cha, Creature attacker)
  {
    _npcId = (cha.getDisplayId() != 0 ? cha.getDisplayId() : cha.getTemplate().npcId);
    _isAttackable = ((attacker != null) && (cha.isAutoAttackable(attacker)));
    _rhand = cha.getRightHandItem();
    _lhand = cha.getLeftHandItem();
    _enchantEffect = 0;
    if ((Config.SERVER_SIDE_NPC_NAME) || (cha.getTemplate().displayId != 0) || (cha.getName() != cha.getTemplate().name))
      _name = cha.getName();
    if ((Config.SERVER_SIDE_NPC_TITLE) || (cha.getTemplate().displayId != 0) || (cha.getTitle() != cha.getTemplate().title)) {
      _title = cha.getTitle();
    }
    _showSpawnAnimation = cha.getSpawnAnimation();
    _showName = cha.isShowName();
    _state = cha.getNpcState();
    _nameNpcString = cha.getNameNpcString();
    _titleNpcString = cha.getTitleNpcString();

    common(cha);
  }

  public NpcInfo(Summon cha, Creature attacker)
  {
    if ((cha.getPlayer() != null) && (cha.getPlayer().isInvisible())) {
      return;
    }
    _npcId = cha.getTemplate().npcId;
    _isAttackable = cha.isAutoAttackable(attacker);
    _rhand = 0;
    _lhand = 0;
    _enchantEffect = 0;
    _showName = true;
    _name = cha.getName();
    _title = cha.getTitle();
    _showSpawnAnimation = cha.getSpawnAnimation();

    common(cha);
  }

  private void common(Creature cha)
  {
    colHeight = cha.getTemplate().collisionHeight;
    colRadius = cha.getTemplate().collisionRadius;
    currentColHeight = cha.getColHeight();
    currentColRadius = cha.getColRadius();
    _npcObjId = cha.getObjectId();
    _loc = cha.getLoc();
    _mAtkSpd = cha.getMAtkSpd();

    Clan clan = cha.getClan();
    Alliance alliance = clan == null ? null : clan.getAlliance();

    clan_id = (clan == null ? 0 : clan.getClanId());
    clan_crest_id = (clan == null ? 0 : clan.getCrestId());

    ally_id = (alliance == null ? 0 : alliance.getAllyId());
    ally_crest_id = (alliance == null ? 0 : alliance.getAllyCrestId());

    _runSpd = cha.getRunSpeed();
    _walkSpd = cha.getWalkSpeed();
    karma = cha.getKarma();
    pvp_flag = cha.getPvpFlag();
    _pAtkSpd = cha.getPAtkSpd();
    running = (cha.isRunning() ? 1 : 0);
    incombat = (cha.isInCombat() ? 1 : 0);
    dead = (cha.isAlikeDead() ? 1 : 0);
    _abnormalEffect = cha.getAbnormalEffect();
    _abnormalEffect2 = cha.getAbnormalEffect2();
    isFlying = cha.isFlying();
    _team = cha.getTeam();
    _formId = cha.getFormId();
    _isNameAbove = cha.isNameAbove();
    _titleColor = ((cha.isSummon()) || (cha.isPet()) ? 1 : 0);

    can_writeImpl = true;
  }

  public NpcInfo update()
  {
    _showSpawnAnimation = 1;
    return this;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }
    writeC(12);

    writeD(_npcObjId);
    writeD(_npcId + 1000000);
    writeD(_isAttackable ? 1 : 0);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z + Config.CLIENT_Z_SHIFT);
    writeD(_loc.h);
    writeD(0);
    writeD(_mAtkSpd);
    writeD(_pAtkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeF(1.100000023841858D);
    writeF(_pAtkSpd / 277.47834071900002D);
    writeF(colRadius);
    writeF(colHeight);
    writeD(_rhand);
    writeD(0);
    writeD(_lhand);
    writeC(_isNameAbove ? 1 : 0);
    writeC(running);
    writeC(incombat);
    writeC(dead);
    writeC(_showSpawnAnimation);
    writeD(_nameNpcString.getId());
    writeS(_name);
    writeD(_titleNpcString.getId());
    writeS(_title);
    writeD(_titleColor);
    writeD(pvp_flag);
    writeD(karma);
    writeD(_abnormalEffect);
    writeD(clan_id);
    writeD(clan_crest_id);
    writeD(ally_id);
    writeD(ally_crest_id);
    writeC(isFlying ? 2 : 0);
    writeC(_team.ordinal());
    writeF(currentColRadius);
    writeF(currentColHeight);
    writeD(_enchantEffect);
    writeD(0);
    writeD(0);
    writeD(_formId);
    writeC(_showName ? 1 : 0);
    writeC(_showName ? 1 : 0);
    writeD(_abnormalEffect2);
    writeD(_state);
  }
}