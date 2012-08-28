// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MagicSkillLaunched.java

package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

// Referenced classes of package net.sf.l2j.gameserver.serverpackets:
//            L2GameServerPacket

public class MagicSkillLaunched extends L2GameServerPacket
{

    public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object targets[])
    {
        _charObjId = cha.getObjectId();
        _skillId = skillId;
        _skillLevel = skillLevel;
        _numberOfTargets = targets.length;
        _targets = targets;
        _singleTargetId = 0;
    }

    public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
    {
        _charObjId = cha.getObjectId();
        _skillId = skillId;
        _skillLevel = skillLevel;
        _numberOfTargets = 1;
        _singleTargetId = cha.getTargetId();
    }

    protected final void writeImpl()
    {
        writeC(118);
        writeD(_charObjId);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_numberOfTargets);
        if(_singleTargetId != 0 || _numberOfTargets == 0)
        {
            writeD(_singleTargetId);
        } else
        {
            L2Object arr$[] = _targets;
            int len$ = arr$.length;
            for(int i$ = 0; i$ < len$; i$++)
            {
                L2Object target = arr$[i$];
                try
                {
                    writeD(target.getObjectId());
                }
                catch(NullPointerException e)
                {
                    writeD(0);
                }
            }

        }
    }

    public String getType()
    {
        return "[S] 8E MagicSkillLaunched";
    }

    @SuppressWarnings("unused")
	private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
    private int _charObjId;
    private int _skillId;
    private int _skillLevel;
    private int _numberOfTargets;
    private L2Object _targets[];
    private int _singleTargetId;
}
