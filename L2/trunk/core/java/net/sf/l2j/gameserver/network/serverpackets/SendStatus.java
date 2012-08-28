package net.sf.l2j.gameserver.network.serverpackets;
import java.util.Random;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;


public final class SendStatus extends L2GameServerPacket
{
    private static final String _S__00_STATUS = "[S] 00 rWho";
    private int online_players = 0;
    private int max_online = 0;
    private int online_priv_store = 0;
    private float priv_store_factor = 0;
    @SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(SendStatus.class.getName());

    @Override
    public void runImpl()
    {
    
    }

    @SuppressWarnings("static-access")
	@Override
    protected final void writeImpl()
    
    {
        Random ppc = new Random();
           online_players = L2World.getInstance().getAllPlayersCount() +Config.RWHO_ONLINE_INCREMENT;
                
        if(online_players>Config.RWHO_MAX_ONLINE)
        {
            Config.RWHO_MAX_ONLINE = online_players;
        }
        
        max_online = Config.RWHO_MAX_ONLINE;
        priv_store_factor = Config.RWHO_PRIV_STORE_FACTOR;
            
        online_players = L2World.getInstance().getAllPlayersCount() + (int)(L2World.getInstance().getAllPlayersCount()*Config.RWHO_ONLINE_INCREMENT/100) + Config.RWHO_FORCE_INC;
        online_priv_store = (int)(online_players* (priv_store_factor/100));
        
        writeC(0x00);                         // Packet ID
        writeD(0x01);                         // World ID
        writeD(max_online);                   // Max Online
        writeD(online_players);               // Current Online
        writeD(online_players);               // Current Online
        writeD(online_priv_store);            // Priv.Sotre Chars
      
        
        //     SEND TRASH
        if(Config.RWHO_SEND_TRASH) {

            writeH(0x30);
            writeH(0x2C);  

            writeH(0x36);
            writeH(0x2C);              

            if(Config.RWHO_ARRAY[12]==Config.RWHO_KEEP_STAT)
            {
                int z;
                  z = ppc.nextInt(6);
                if(z==0){
                    z+=2;
                }
                for (int x = 0; x<8; x++) {
                    if(x==4)
                    {
                        Config.RWHO_ARRAY[x]=44;
                    }
                    else { Config.RWHO_ARRAY[x] = 51+ppc.nextInt(z); }
                }
                  Config.RWHO_ARRAY[11] = 37265+ppc.nextInt(z*2+3);
                Config.RWHO_ARRAY[8]  = (51+ppc.nextInt(z));
                  z = 36224+ppc.nextInt(z*2);
                  Config.RWHO_ARRAY[9]  = z;
                  Config.RWHO_ARRAY[10] = z;
                  Config.RWHO_ARRAY[12] = 1;
            }
            
            for(int z=0;z<8;z++)
            {
                if(z==3)
                {
                    Config.RWHO_ARRAY[z]-=1;
                }          
                writeH(Config.RWHO_ARRAY[z]);
            }
              writeD(Config.RWHO_ARRAY[8]);
            writeD(Config.RWHO_ARRAY[9]);
            writeD(Config.RWHO_ARRAY[10]);
            writeD(Config.RWHO_ARRAY[11]);
            Config.RWHO_ARRAY[12]++;
                      
            writeD(0x00);
            writeD(0x02);
            
        }
    }

    @Override
    public String getType()
    {
        return _S__00_STATUS;
    }
}
