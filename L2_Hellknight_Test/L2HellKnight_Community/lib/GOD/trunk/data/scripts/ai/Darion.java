package ai;

/**
 * @author Nox
 */
 
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.tables.DoorTable;

public class Darion extends Fighter
{

    public Darion(L2Character actor)
    {
        super(actor);
    }

    protected void onEvtSpawn()
    {
        doors(false);
        super.onEvtSpawn();
    }

    protected void onEvtDead(L2Character killer)
    {
        doors(true);
        super.onEvtDead(killer);
    }

    private static void doors(boolean open)
    {
        int arr$[] = doors;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            int i = arr$[i$];
            L2DoorInstance door = DoorTable.getInstance().getDoor(Integer.valueOf(i));
            if(open)
                door.openMe();
            else
                door.closeMe();
        }

    }

    private static int doors[] = {
        0x134fd94, 0x134fd95, 0x134fd96, 0x134fd97, 0x134fd99
    };

}