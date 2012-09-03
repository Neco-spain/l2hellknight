package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;

/**
 sample
 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000    - 0 damage (missed 0x80)
 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10                                     3....
 format
 dddc dddh (ddc)
 */
public class Attack extends L2GameServerPacket
{

	public static final int FLAG = 0x00;   //Обычный удар без надписей.
	public static final int FLAG_MISS = 0x01; //Увернулся от удара
	public static final int FLAG_BLOCK = 0x02; //Блокировал удар.
	public static final int FLAG_BLOCK_NONE = 0x03; //Блокировал удар.
	public static final int FLAG_CRIT = 0x04;    //Крит.
	public static final int FLAG_CRIT_NONE = 0x05;    //Крит.
	public static final int FLAG_SHIELD = 0x06;  //Заблокировал Крит.
	public static final int FLAG_SHIELD_NONE = 0x07;  //Заблокировал Крит.

	public static final int FLAG_SOULSHOT = 0x08; //Удар с соской.
	public static final int FLAG_SOULSHOT_MISS = 0x0b; //Удар с соской.
	public static final int SOULSHOT_FLAG_CRIT = 0x0c; //12 - Удар с соской и крит.

	public static final int SOULSHOT_NG = 0x00;
	public static final int SOULSHOT_D = 0x01;
	public static final int SOULSHOT_C = 0x02;
	public static final int SOULSHOT_B = 0x03;
	public static final int SOULSHOT_A = 0x04;
	public static final int SOULSHOT_S = 0x05;
	public static final int SOULSHOT_R = 0x06;


	private class Hit
	{
		int _targetId, _damage, _flags;

		Hit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if(!_soulshot)
            {
                _flags = FLAG;
            }
            if(!_soulshot && miss)
            {
                _flags = FLAG_MISS;
            }
            if(!_soulshot && crit)
            {
               _flags = FLAG_CRIT;
            }
            if(!_soulshot && shld)
            {
                _flags = FLAG_SHIELD;
            }

            if(_soulshot)
            {
                _flags = FLAG_SOULSHOT;
            }
            if(_soulshot && miss)
            {
                _flags = FLAG_SOULSHOT_MISS;
            }
            if(_soulshot && crit)
            {
                _flags = SOULSHOT_FLAG_CRIT;
            }
		}
	}

	public final int _attackerId;
	public final boolean _soulshot;
	public final int _rGrade;
	private final int _x, _y, _z, _tx, _ty, _tz;
	private Hit[] hits;

	public Attack(L2Character attacker, L2Character target, boolean ss, int rGrade)
	{
		_attackerId = attacker.getObjectId();
		_soulshot = ss;
		_rGrade = rGrade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		hits = new Hit[0];
	}

	/** Add this hit (target, damage, miss, critical, shield) to the Server-Client packet Attack.<BR><BR> */
	public void addHit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
	{
		// Get the last position in the hits table
		int pos = hits.length;
		// Create a new Hit object
		Hit[] tmp = new Hit[pos + 1];
		// Add the new Hit object to hits table
		System.arraycopy(hits, 0, tmp, 0, hits.length);
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}

	/** Return True if the Server-Client packet Attack conatins at least 1 hit.<BR><BR> */
	public boolean hasHits()
	{
		return hits.length > 0;
	}

	@Override
	protected final void writeImpl()
	{
        writeC(0x33);
        //TODO: FIX ME
        writeD(_attackerId);
        writeD(hits[0]._targetId);
        writeC(0x00);
        writeD(hits[0]._damage);
        writeD(hits[0]._flags);
		if(_soulshot) 
			writeD(Attack.this._rGrade);
		else
			writeD(0);
        writeD(_x);
        writeD(_y);
        writeD(_z);

        writeH(hits.length - 1);
        // prevent sending useless packet while there is only one target.
        if (hits.length > 1)
        {
            for (int i = 1; i < hits.length; i++)
            {
                writeD(hits[i]._targetId);
                writeD(hits[i]._damage);
                writeD(hits[i]._flags);
				if(_soulshot) 
					writeD(Attack.this._rGrade);
				else
					writeD(0);
            }
        }

        writeD(_tx);
        writeD(_ty);
        writeD(_tz);
	}
}