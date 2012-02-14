package l2rt.gameserver.network.serverpackets;

import l2rt.Config;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.instances.L2CubicInstance;
import l2rt.gameserver.model.instances.L2DecoyInstance;
import l2rt.gameserver.model.instances.L2NoTargetNpcInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.util.Location;

public class NpcInfo extends L2GameServerPacket
{
	//   ddddddddddddddddddffffdddcccccSSddd dddddccffddddccd
	private boolean can_writeImpl = false;
	private L2Character _cha;
	private L2Summon _summon;
	private int _npcObjId, _npcId, running, incombat, dead, team, type, _showSpawnAnimation = 0;
	private int _runSpd, _walkSpd, _mAtkSpd, _pAtkSpd, _rhand, _lhand, _enchantEffect;
	private int karma, pvp_flag, _abnormalEffect, _abnormalEffect2, clan_crest_id, ally_crest_id;
	private int _HP,_maxHP,_MP,_maxMP,_CP,_maxCP;
	private int _transform = 0;
	private double colHeight, colRadius, currentColHeight, currentColRadius;
	//private int _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private boolean _isAttackable;
	private Location _loc, decoy_fishLoc;
	private String _name = "";
	private String _title = "";
	private Inventory decoy_inv;
	private int decoy_race, decoy_sex, decoy_base_class, decoy_clan_id, decoy_ally_id;
	private int decoy_noble, decoy_hair_style, decoy_hair_color, decoy_face, decoy_sitting;
	private int decoy_rec_have, decoy_rec_left, decoy_class_id, decoy_large_clan_crest_id;
	private int decoy_enchant, decoy_PledgeClass, decoy_pledge_type;
	private int decoy_NameColor, decoy_TitleColor, decoy_Transformation, decoy_Agathion;
	private int decoy_hero, decoy_mount_id, decoy_swimSpd, decoy_cw_level, decoy_clan_rep_score;
	private byte decoy_mount_type, decoy_private_store, decoy_fishing;
	private double decoy_move_speed, decoy_attack_speed;
	private L2CubicInstance[] decoy_cubics;
	private L2Character _attacker;
	private boolean isFlying = false;
	private boolean isHideName = false;

	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		if(cha == null)
		{
			return;
		}
		_cha = cha;
		_attacker = attacker;
		_npcId = cha.getDisplayId() != 0 ? cha.getDisplayId() : cha.getTemplate().npcId;
		isHideName = cha.getTemplate().isHideName;
		_isAttackable = attacker == null ? false : cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_enchantEffect = cha.getWeaponEnchant();
		if(Config.SERVER_SIDE_NPC_NAME || cha.getTemplate().displayId != 0)
		{
			_name = cha.getName();
		}
		if(Config.SERVER_SIDE_NPC_TITLE || cha.getTemplate().displayId != 0)
		{
			_title = _title + cha.getTitle();
		}
		
		_HP = (int)cha.getCurrentHp();
		_MP = (int)cha.getCurrentMp();
		_CP = (int)cha.getCurrentCp();
		_maxHP = cha.getMaxHp();
		_maxMP = cha.getMaxMp();		
		_maxCP = cha.getMaxCp();
		
		_showSpawnAnimation = cha.isShowSpawnAnimation();
		common();
		can_writeImpl = true;
	}

	public NpcInfo(L2Summon cha, L2Character attacker, int showSpawnAnimation)
	{
		if(cha == null)
		{
			return;
		}
		if(cha.getPlayer() != null && cha.getPlayer().isInvisible())
		{
			return;
		}
		_showSpawnAnimation = showSpawnAnimation;
		_cha = cha;
		_attacker = attacker;
		_summon = cha;
		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker); //(cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_enchantEffect = 0;
		if(Config.SERVER_SIDE_NPC_NAME || cha.isPet())
		{
			_name = _cha.getName();
		}
		_title = cha.getTitle();
		switch(_summon.getTemplate().getNpcId())
		{
			case PetDataTable.GREAT_WOLF_ID:
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				if(_summon.getLevel() >= 70)
				{
					type = 3;
				}
				else if(_summon.getLevel() >= 65)
				{
					type = 2;
				}
				else if(_summon.getLevel() >= 60)
				{
					type = 1;
				}
				break;
		}
		common();
		can_writeImpl = true;
	}

	private void common()
	{
		currentColHeight = colHeight = _cha.getColHeight();
		currentColRadius = colRadius = _cha.getColRadius();
		if(_cha.getEffectList().getEffectByType(EffectType.Grow) != null)
		{
			currentColHeight = (int) (currentColHeight / 1.2);
			currentColRadius = (int) (currentColRadius / 1.2);
		}
		_npcObjId = _cha.getObjectId();
		_loc = _cha.getLoc();
		_mAtkSpd = _cha.getMAtkSpd();
		clan_crest_id = _cha.getClanCrestId();
		ally_crest_id = _cha.getAllyCrestId();
		_transform = _cha.getTransformation();
		if(_cha instanceof L2DecoyInstance)
		{
			fillDecoy();
		}
		else
		{
			_runSpd = _cha.getRunSpeed();
			_walkSpd = _cha.getWalkSpeed();
			karma = _cha.getKarma();
			pvp_flag = _cha.getPvpFlag();
			_pAtkSpd = _cha.getPAtkSpd();
			running = _cha.isRunning() ? 1 : 0;
			incombat = _cha.isInCombat() ? 1 : 0;
			dead = _cha.isAlikeDead() ? 1 : 0;
			_abnormalEffect = _cha.getAbnormalEffect();
			_abnormalEffect2 = _cha.getAbnormalEffect2();
			isFlying = _cha.isFlying();
			if(_cha instanceof L2Summon)
			{
				if(_cha.getTeam() < 3)
				{
					team = _cha.getTeam();
				}
				else if(_attacker == null || _attacker.getTeam() == 0)
				{
					team = 0;
				}
				else if(_attacker.getTeam() == _cha.getTeam())
				{
					team = 1;
				}
				else
				{
					team = 2;
				}
			}
			else
			{
				team = _cha.getTeam();
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
		{
			return;
		}
		if(_cha instanceof L2DecoyInstance)
		{
			writeImpl_Decoy();
			return;
		}
		writeC(0x0c);
		//2048C334   PUSH Engine.205AA268                      ASCII "ddddddddddddddddddffffdddcccccdSdSddd"
		//2048C3B6   PUSH Engine.205AA290                      ASCII "dddddccffddddccdddddddcddd"
		writeD(_npcObjId);
		writeD(_npcId + 1000000); // npctype id c4
		writeD(_isAttackable ? 1 : 0);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd /*_swimRunSpd*//*0x32*/); // swimspeed
		writeD(_walkSpd/*_swimWalkSpd*//*0x32*/); // swimspeed
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(1.100000023841858); // взято из клиента
		writeF(_pAtkSpd / 277.478340719);
		writeF(colRadius);
		writeF(colHeight);
		writeD(_rhand); // right hand weapon
		writeD(0); //TODO chest
		writeD(_lhand); // left hand weapon
		writeC(1); // 2.2: name above char 1=true ... ??; 2.3: 1 - normal, 2 - dead
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(-1);
		writeS(_name);
		writeD(-1);
		writeS(_title);
		writeD(0); // как-то связано с тайтлом, если не 0 скрывать? Title color 0=client default?
		writeD(pvp_flag);
		writeD(karma); // hmm karma ??
		writeD(_abnormalEffect); // C2
		writeD(0); // clan id (клиентом не используется, но требуется для показа значка)
		writeD(clan_crest_id); // clan crest id
		writeD(0); // ally id (клиентом не используется, но требуется для показа значка)
		writeD(ally_crest_id); // ally crest id
		writeC(isFlying ? 2 : 0); // C2
		writeC(team); // team aura 1-blue, 2-red
		writeF(currentColRadius); // тут что-то связанное с colRadius
		writeF(currentColHeight); // тут что-то связанное с colHeight
		writeD(Math.min(_enchantEffect, 127)); // C4
		writeD(0x00); // writeD(_npc.isFlying() ? 1 : 0); // C6
		writeD(0x00);
		writeD(type); // great wolf type
		writeC(0x01); // влияет на возможность примененя к цели /nexttarget и /assist
		if (_cha instanceof L2NoTargetNpcInstance)
			writeC(0); // 0 - hide name
		else
			writeC(1);
		writeD(_abnormalEffect2);
		writeD(0);
		writeD(_transform); // <-- Transform id
		writeD(_HP);
		writeD(_maxHP);
		writeD(_MP);
		writeD(_maxMP);
		writeC(0);	// <-- хз, ниче не меняется
		writeD(0);	// <-- хз, ниче не меняется
		writeD(0);	// <-- хз, ниче не меняется
		writeD(0);	// <-- хз, ниче не меняется
	}

	private void fillDecoy()
	{
		L2Player cha_owner = _cha.getPlayer();
		_runSpd = cha_owner.getRunSpeed();
		_walkSpd = cha_owner.getWalkSpeed();
		karma = cha_owner.getKarma();
		pvp_flag = cha_owner.getPvpFlag();
		_pAtkSpd = cha_owner.getPAtkSpd();
		running = cha_owner.isRunning() ? 1 : 0;
		incombat = cha_owner.isInCombat() ? 1 : 0;
		dead = cha_owner.isAlikeDead() ? 1 : 0;
		_abnormalEffect = cha_owner.getAbnormalEffect();
		team = cha_owner.getTeam();
		if(cha_owner.isCursedWeaponEquipped())
		{
			_name = cha_owner.getTransformationName();
			_title = "";
			clan_crest_id = 0;
			ally_crest_id = 0;
			decoy_clan_id = 0;
			decoy_ally_id = 0;
			decoy_large_clan_crest_id = 0;
			decoy_cw_level = CursedWeaponsManager.getInstance().getLevel(cha_owner.getCursedWeaponEquippedId());
		}
		else
		{
			_name = cha_owner.getName();
			_title = cha_owner.getTitle();
			clan_crest_id = cha_owner.getClanCrestId();
			ally_crest_id = cha_owner.getAllyCrestId();
			decoy_clan_id = cha_owner.getClanId();
			decoy_ally_id = cha_owner.getAllyId();
			decoy_large_clan_crest_id = cha_owner.getClanCrestLargeId();
			decoy_cw_level = 0;
		}
		if(cha_owner.isMounted())
		{
			decoy_enchant = 0;
			decoy_mount_id = cha_owner.getMountNpcId() + 1000000;
			decoy_mount_type = (byte) cha_owner.getMountType();
		}
		else
		{
			decoy_enchant = (byte) cha_owner.getEnchantEffect();
			decoy_mount_id = 0;
			decoy_mount_type = 0;
		}
		if(decoy_clan_id > 0 && cha_owner.getClan() != null)
		{
			decoy_clan_rep_score = cha_owner.getClan().getReputationScore();
		}
		else
		{
			decoy_clan_rep_score = 0;
		}
		decoy_fishing = cha_owner.isFishing() ? (byte) 1 : (byte) 0;
		decoy_fishLoc = cha_owner.getFishLoc();
		decoy_swimSpd = cha_owner.getSwimSpeed();
		decoy_private_store = (byte) cha_owner.getPrivateStoreType(); // 1 - sellshop
		decoy_inv = cha_owner.getInventory();
		decoy_race = cha_owner.getBaseTemplate().race.ordinal();
		decoy_sex = cha_owner.getSex();
		decoy_base_class = cha_owner.getBaseClassId();
		decoy_move_speed = cha_owner.getMovementSpeedMultiplier();
		decoy_attack_speed = cha_owner.getAttackSpeedMultiplier();
		decoy_hair_style = cha_owner.getHairStyle();
		decoy_hair_color = cha_owner.getHairColor();
		decoy_face = cha_owner.getFace();
		decoy_sitting = cha_owner.isSitting() ? 0 : 1;
		decoy_cubics = cha_owner.getCubics().toArray(new L2CubicInstance[0]);
		decoy_rec_left = cha_owner.getRecomLeft();
		decoy_rec_have = cha_owner.isGM() ? 0 : cha_owner.getRecomHave();
		decoy_class_id = cha_owner.getClassId().getId();
		decoy_noble = cha_owner.isNoble() ? 1 : 0;
		decoy_hero = cha_owner.isHero() || cha_owner.isGM() && Config.GM_HERO_AURA ? 1 : 0; // 0x01: Hero Aura
		decoy_NameColor = cha_owner.getNameColor();
		decoy_PledgeClass = cha_owner.getPledgeClass();
		decoy_pledge_type = cha_owner.getPledgeType();
		decoy_TitleColor = cha_owner.getTitleColor();
		decoy_Transformation = cha_owner.getTransformation();
		decoy_Agathion = cha_owner.getAgathion() != null ? cha_owner.getAgathion().getId() : 0;
	}

	private void writeImpl_Decoy()
	{
		writeC(0x31);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
		writeD(_npcObjId);
		writeS(_name);
		writeD(decoy_race);
		writeD(decoy_sex);
		writeD(decoy_base_class);
		for(byte PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			writeD(decoy_inv.getPaperdollItemId(PAPERDOLL_ID));
		}
		for(byte PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			writeD(decoy_inv.getPaperdollAugmentationId(PAPERDOLL_ID));
		}
		writeD(0x00); // ?GraciaFinal
		writeD(0x00); // ?GraciaFinal
		writeD(pvp_flag);
		writeD(karma);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(decoy_swimSpd); // swimspeed
		writeD(decoy_swimSpd); // swimspeed
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(decoy_move_speed);
		writeF(decoy_attack_speed);
		writeF(colRadius);
		writeF(colHeight);
		writeD(decoy_hair_style);
		writeD(decoy_hair_color);
		writeD(decoy_face);
		writeS(_title);
		writeD(decoy_clan_id);
		writeD(clan_crest_id);
		writeD(decoy_ally_id);
		writeD(ally_crest_id);
		writeD(0);
		writeC(decoy_sitting);
		writeC(running);
		writeC(incombat);
		writeC(dead);
		writeC(0);
		writeC(decoy_mount_type);
		writeC(decoy_private_store);
		writeH(decoy_cubics.length);
		for(L2CubicInstance cubic : decoy_cubics)
		{
			writeH(cubic == null ? 0 : cubic.getId());
		}
		writeC(0x00); // find party members
		writeD(_abnormalEffect);
		writeC(decoy_rec_left);
		writeH(decoy_rec_have);
		writeD(decoy_mount_id);
		writeD(decoy_class_id);
		writeD(0); // ?
		writeC(decoy_enchant);
		writeC(team);
		writeD(decoy_large_clan_crest_id);
		writeC(decoy_noble);
		writeC(decoy_hero);
		writeC(decoy_fishing);
		writeD(decoy_fishLoc.x);
		writeD(decoy_fishLoc.y);
		writeD(decoy_fishLoc.z);
		writeD(decoy_NameColor);
		writeD(_loc.h);
		writeD(decoy_PledgeClass);
		writeD(decoy_pledge_type);
		writeD(decoy_TitleColor);
		writeD(decoy_cw_level);
		writeD(decoy_clan_rep_score);
		writeD(decoy_Transformation);
		writeD(decoy_Agathion);
		writeD(0x01); // T2
		writeD(_abnormalEffect2);
		writeD(0x00); // ? GraciaFinal
		writeD(0x00); // ? GraciaFinal
		writeD(0x00); // ? GraciaFinal
	}

	public static final byte[] PAPERDOLL_ORDER = {
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_BACK,
		Inventory.PAPERDOLL_LRHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_DHAIR,
		Inventory.PAPERDOLL_RBRACELET,
		Inventory.PAPERDOLL_LBRACELET,
		Inventory.PAPERDOLL_DECO1,
		Inventory.PAPERDOLL_DECO2,
		Inventory.PAPERDOLL_DECO3,
		Inventory.PAPERDOLL_DECO4,
		Inventory.PAPERDOLL_DECO5,
		Inventory.PAPERDOLL_DECO6,
		Inventory.PAPERDOLL_BELT // Пояс
	};

	@Override
	public String getType()
	{
		return super.getType() + (_cha != null ? " about " + _cha : "");
	}
}