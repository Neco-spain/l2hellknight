/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;
/**
 * This class ...
 *
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends L2GameServerPacket
{


	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private L2Character _activeChar;
	private int _x, _y, _z, _heading;
	private int _idTemplate;
	private boolean _isAttackable, _isSummoned;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private int _rhand, _lhand;
    private int _collisionHeight, _collisionRadius;
    private String _name = "";
    private String _title = "";
    private int _clanCrest; 
 	private int _clanId; 
 	private int _allyCrest; 
 	private int _allyId; 

	/**
	 * @param _characters
	 */
	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem(); 
		_lhand = cha.getLeftHandItem(); 
		_isSummoned = false;
        _collisionHeight = cha.getCollisionHeight();
        _collisionRadius = cha.getCollisionRadius();
        if (cha.getTemplate().serverSideName)
        	_name = cha.getTemplate().name;
        
        if (Config.CHAMPION_ENABLE && cha.isChampion())
            _title = Config.CHAMPION_TITLE;
        if (cha.getTemplate().serverSideTitle)
            _title += " " + cha.getTemplate().title;
        else if (cha.getTitle() != null) 
            _title += " " + cha.getTitle();
        
        if ((Config.SHOW_NPC_CREST) && (cha instanceof L2NpcInstance) && (cha.isInsideZone(2)) && (cha.getCastle().getOwnerId() != 0)) 
        { 
        	 int _x = cha.getX(); 
        	 int _y = cha.getY(); 
        	 int _z = cha.getZ(); 
        	  
        	 L2TownZone Town = TownManager.getInstance().getTown(_x, _y, _z); 
        	 if (Town != null) 
        	 { 
        	 	int townId = Town.getTownId(); 
        	 	if ((townId != 33) && (townId != 22)) 
        	 	{ 
        	 	   L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId()); 
        	 	    this._clanCrest = clan.getCrestId(); 
        	 		this._clanId = clan.getClanId(); 
        	 		this._allyCrest = clan.getAllyCrestId(); 
        	 		this._allyId = clan.getAllyId(); 
        	 	} 
        	 } 
        	 		 
        } 
		if (Config.SHOW_NPC_LVL && _activeChar instanceof L2MonsterInstance)
			{
				String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
		if (_title != null && !_title.isEmpty())
				t += " " + _title;
				_title = t;
			}
        _x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	public NpcInfo(L2Summon cha, L2Character attacker)
	{
		_activeChar = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker); //(cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = cha.isShowSummonAnimation();
        _collisionHeight = _activeChar.getTemplate().collisionHeight;
        _collisionRadius = _activeChar.getTemplate().collisionRadius;
        _name = cha.getName();
        _title = cha.getOwner() != null ? (cha.getOwner().isOnline() == 0 ? "" : cha.getOwner().getName()) : "";

        _x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_runSpd = _activeChar.getRunSpeed();
		_walkSpd = _activeChar.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	@Override
	protected final void writeImpl()
	{
        if (_activeChar instanceof L2Summon)
            if (((L2Summon)_activeChar).getOwner() != null
                    && ((L2Summon)_activeChar).getOwner().getAppearance().getInvisible())
                return;
		writeC(0x16);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate+1000000);  // npctype id
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/*0x32*/);  // swimspeed
		writeD(_swimWalkSpd/*0x32*/);  // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1.1/*_activeChar.getProperMultiplier()*/);
		//writeF(1/*_activeChar.getAttackSpeedMultiplier()*/);
		writeF(_pAtkSpd/277.478340719);
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1);	// name above char 1=true ... ??
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_title);
		writeD(0);
		writeD(0);
		writeD(0000);  // hmm karma ??

		writeD(_activeChar.getAbnormalEffect());  // C2
		if (Config.SHOW_NPC_CREST) 
		{ 
		 	writeD(this._clanId); 
		 	writeD(this._clanCrest); 
		 	writeD(this._allyId); 
		 	writeD(this._allyCrest);
		 	writeC(0000);  // C2
		} 
		else 
		{ 
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeC(0000);  // C2
		}
		if (Config.CHAMPION_AURA && _activeChar.isChampion())
			writeC(0x02);  // C3  team circle 1-blue, 2-red
		else
			writeC(0x00);  // C3  team circle 1-blue, 2-red
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD(0x00);  // C4
		writeD(0x00);  // C6
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}
