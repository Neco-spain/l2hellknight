package l2rt.gameserver.network.serverpackets;

import java.util.logging.Logger;

import l2rt.Config;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2CubicInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.util.Location;

public class CharInfo extends L2GameServerPacket
{
	private static final Logger _log = Logger.getLogger(CharInfo.class.getName());
	private L2Player _cha;
	private Inventory _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private float _moveMultiplier;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private float speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face, _abnormalEffect, _abnormalEffect2;
	private int clan_id, clan_crest_id, large_clan_crest_id, ally_id, ally_crest_id, class_id;
	private byte _sit, _run, _combat, _dead, private_store, _enchant;
	private byte _noble, _hero, _fishing, mount_type;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color, _transform, _agathion;
	private L2CubicInstance[] cubics;
	private boolean can_writeImpl = false;
	private boolean partyRoom = false;
	private boolean isFlying = false;
	private int _territoryId;
	public static final byte[] PAPERDOLL_ORDER = { 0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };

	protected boolean logHandled()
	{
		return true;
	}

	public CharInfo(L2Player cha)
	{
		if((_cha = cha) == null || _cha.isInvisible() || _cha.isDeleting())
		{
			return;
		}
		// Проклятое оружие и трансформации для ТВ скрывают имя и все остальные опознавательные знаки
		if(_cha.getTransformationName() != null || _cha.getReflection().getId() < 0 && _cha.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			_name = _cha.getTransformationName() != null ? _cha.getTransformationName() : _cha.getName();
			_title = "";
			clan_id = 0;
			clan_crest_id = 0;
			ally_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			if(_cha.isCursedWeaponEquipped())
			{
				cw_level = CursedWeaponsManager.getInstance().getLevel(_cha.getCursedWeaponEquippedId());
			}
		}
		else
		{
			_name = _cha.getName();
			if(_cha.getPrivateStoreType() != 0)
			{
				_title = "";
			}
			else if(!_cha.isConnected())
			{
				_title = "DISCONNECTED";
				_title_color = 255;
			}
			else
			{
				_title = _cha.getTitle();
				_title_color = _cha.getTitleColor();
			}
			clan_id = _cha.getClanId();
			clan_crest_id = _cha.getClanCrestId();
			ally_id = _cha.getAllyId();
			ally_crest_id = _cha.getAllyCrestId();
			large_clan_crest_id = _cha.getClanCrestLargeId();
			cw_level = 0;
		}
		if(_cha.isMounted())
		{
			_enchant = 0;
			mount_id = _cha.getMountNpcId() + 1000000;
			mount_type = (byte) _cha.getMountType();
		}
		else
		{
			_enchant = (byte) _cha.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}
		_inv = _cha.getInventory();
		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_moveMultiplier = _cha.getMovementSpeedMultiplier();
		_runSpd = (int) (_cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_cha.getWalkSpeed() / _moveMultiplier);
		_flRunSpd = 0; // TODO
		_flWalkSpd = 0; // TODO
		if(_cha.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}
		_swimSpd = _cha.getSwimSpeed();
		_loc = _cha.getLoc();
		_objId = _cha.getObjectId();
		_race = _cha.getBaseTemplate().race.ordinal();
		_sex = _cha.getSex();
		base_class = _cha.getBaseClassId();
		pvp_flag = _cha.getPvpFlag();
		karma = _cha.getKarma();
		speed_move = _cha.getMovementSpeedMultiplier();
		speed_atack = _cha.getAttackSpeedMultiplier();
		col_radius = _cha.getColRadius();
		col_height = _cha.getColHeight();
		hair_style = _cha.getHairStyle();
		hair_color = _cha.getHairColor();
		face = _cha.getFace();
		if(clan_id > 0 && _cha.getClan() != null)
		{
			clan_rep_score = _cha.getClan().getReputationScore();
		}
		else
		{
			clan_rep_score = 0;
		}
		_sit = _cha.isSitting() ? (byte) 0 : (byte) 1; // standing = 1 sitting = 0
		_run = _cha.isRunning() ? (byte) 1 : (byte) 0; // running = 1 walking = 0
		_combat = _cha.isInCombat() ? (byte) 1 : (byte) 0;
		_dead = _cha.isAlikeDead() ? (byte) 1 : (byte) 0;
		private_store = (byte) _cha.getPrivateStoreType(); // 1 - sellshop
		cubics = _cha.getCubics().toArray(new L2CubicInstance[0]);
		_abnormalEffect = _cha.getAbnormalEffect();
		_abnormalEffect2 = _cha.getAbnormalEffect2();
		rec_have = _cha.isGM() ? 0 : _cha.getRecomHave();
		class_id = _cha.getClassId().getId();
		_noble = _cha.isNoble() ? (byte) 1 : (byte) 0; // 0x01: symbol on char menu ctrl+I
		_hero = _cha.isHero() || _cha.isGM() && Config.GM_HERO_AURA ? (byte) 1 : (byte) 0; // 0x01: Hero Aura
		_fishing = _cha.isFishing() ? (byte) 1 : (byte) 0;
		_fishLoc = _cha.getFishLoc();
		_nameColor = _cha.getNameColor(); // New C5
		plg_class = _cha.getPledgeClass();
		pledge_type = _cha.getPledgeType();
		_transform = _cha.getTransformation();
		_agathion = _cha.getAgathion() != null ? _cha.getAgathion().getId() : 0;
		partyRoom = PartyRoomManager.getInstance().isLeader(_cha);
		isFlying = _cha.isInFlyingTransform();
		_territoryId = _cha.getTerritorySiege();
		can_writeImpl = true;
		if (_cha.isAwaking())
			class_id = _cha.getAwakingId();
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
		{
			return;
		}
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.equals(_cha))
		{
			_log.severe("You cant send CharInfo about his character to active user!!!");
			Thread.dumpStack();
			return;
		}
		writeC(0x31);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(0);
		writeD(_objId);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(base_class);
		for(byte PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeD(_inv.getPaperdollItemId(PAPERDOLL_ID));

		for(byte PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			writeH(_inv.getPaperdollAugmentationId(PAPERDOLL_ID));
			writeH(0x00);
		}
		writeD(0x00); // ? GraciaFinal
		writeD(0x01); // ? GraciaFinal
		writeD(pvp_flag);
		writeD(karma);
		writeD(0x00); //???
		writeD(0x00); //???
		writeD(0x00); //???
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(0x00);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimSpd); // swimspeed
		writeD(_swimSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0x00); // is invisible
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(L2CubicInstance cubic : cubics)
		{
			writeH(cubic == null ? 0 : cubic.getId());
		}
		writeC(partyRoom ? 0x01 : 0x00);
		writeD(_abnormalEffect);
		writeC(isFlying ? 0x02 : 0x00);
		writeH(rec_have);
		writeD(mount_id);
		writeD(class_id);
		writeD(0);
		writeC(_enchant);
		if(_cha.getTeam() < 3)
		{
			writeC((byte) _cha.getTeam()); // team circle around feet 1 = Blue, 2 = red
		}
		else if(activeChar.getTeam() == 0)
		{
			writeC(0);
		}
		else
		{
			writeC(activeChar.getTeam() == _cha.getTeam() ? 1 : 2);
		}
		writeD(large_clan_crest_id);
		writeC(_noble);
		writeC(_hero);
		writeC(_fishing);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);
		writeD(_nameColor);
		writeD(_loc.h);
		writeD(plg_class);
		writeD(pledge_type);
		writeD(_title_color);
		writeD(cw_level);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);
		writeD(0x01); // T2
		writeD(_abnormalEffect2);
		//writeD(_territoryId > 0 ? 80 + _territoryId : 0);
		//writeD(0);
		//writeD(0);
	}

	/**
	public static final byte[] PAPERDOLL_ORDER = {Inventory.PAPERDOLL_UNDER, Inventory.PAPERDOLL_HEAD,
						      Inventory.PAPERDOLL_RHAND, Inventory.PAPERDOLL_LHAND, Inventory.PAPERDOLL_GLOVES, Inventory.PAPERDOLL_CHEST,
						      Inventory.PAPERDOLL_LEGS, Inventory.PAPERDOLL_FEET, Inventory.PAPERDOLL_BACK, Inventory.PAPERDOLL_LRHAND,
						      Inventory.PAPERDOLL_HAIR, Inventory.PAPERDOLL_DHAIR, Inventory.PAPERDOLL_RBRACELET,
						      Inventory.PAPERDOLL_LBRACELET, Inventory.PAPERDOLL_DECO1, Inventory.PAPERDOLL_DECO2, Inventory.PAPERDOLL_DECO3,
						      Inventory.PAPERDOLL_DECO4, Inventory.PAPERDOLL_DECO5, Inventory.PAPERDOLL_DECO6, Inventory.PAPERDOLL_BELT // Пояс
	};
	**/
}