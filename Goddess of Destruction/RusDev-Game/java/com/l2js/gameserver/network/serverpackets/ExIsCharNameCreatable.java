package com.l2js.gameserver.network.serverpackets;

public class ExIsCharNameCreatable extends L2GameServerPacket 
{ 
    protected int _code; 
     
    public ExIsCharNameCreatable(int code) 
    { 
        _code = code; 
    } 
     
    @Override 
    protected final void writeImpl() 
    { 
        writeC(0xfe); 
        writeH(0x10f); 
        writeD(_code); 
    } 

    @Override 
    public String getType() 
    { 
        return "[S] 10F ExIsCharNameCreatable"; 
    } 
} 