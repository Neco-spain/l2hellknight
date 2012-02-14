package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.util.Rnd;

/**
 * @author: Drizzy
 * @date: 08.02.2011
 * @time: 18:39
 * AI for location Sel Mahum Training ground. This AI launch random animation on group monster.
 */
public class SelMahumLeader extends Fighter
{
	//Время ожидания запуска первого соц.актиона.
	private long waitTime = System.currentTimeMillis() + 45000;  //45 секунд

	public SelMahumLeader(L2Character actor)
	{
		super(actor);	
	}

	//АИ работает независимо от того есть ли рядом игрок
	public boolean isGlobalAI()
	{
		return true;
	}
	
	//Отключаем хождение мобов
    protected boolean randomWalk()
	{
		return false;
	}

    //Отключаем анимацию у владельца АИ (у других мобов через ai_params).
    protected boolean randomAnimation()
	{
		return false;
	}
	
	//Метод выполняется когда мод находится в спокойном состоянии.
	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if (actor == null) 
			return true;

        //Проверяем время
        if(CheckTime())
        {
            //Рандомно берём социал актион.
            int rnd = Rnd.get(4,7);
            // Отправляем С/А актору (владельцу АИ)
            actor.broadcastPacket(new SocialAction(actor.getObjectId(), rnd));
            //Определяем рядом стоящих нпс
            for(L2NpcInstance arround : L2World.getAroundNpc(actor, 700, 700))
                //если нпс имеет ИД 22777, то отправляем пакет.
                if(arround.getNpcId() == 22780 || arround.getNpcId() == 22782 || arround.getNpcId() == 22783 || arround.getNpcId() == 22784 || arround.getNpcId() == 22785)
                {
                    //Не посылаем С\А на мёртвых мобов.
                    if(arround.isDead())
                        return true;
                    // Отправляем С/А мобам вокруг.
                    arround.broadcastPacket(new SocialAction(arround.getObjectId(), rnd));
                }
            // Меняем время для следующего запуска
            waitTime = System.currentTimeMillis() + Rnd.get(45000, 70000);
        }
		return super.thinkActive();
	}

    //Метод для определения времени (true\false).
    public boolean CheckTime()
    {
        if(waitTime < System.currentTimeMillis())
            return true; // Время вышло, запускаем анимацию
        if(waitTime > System.currentTimeMillis())
            return false; //Время не вышло, продолжаем ждать.
        return false;
    }
}