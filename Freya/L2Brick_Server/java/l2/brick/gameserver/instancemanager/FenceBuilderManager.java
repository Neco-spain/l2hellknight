package l2.brick.gameserver.instancemanager;

import java.util.Map;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import l2.brick.gameserver.idfactory.IdFactory;
import l2.brick.gameserver.model.actor.instance.L2FenceInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.L2WorldRegion;

public class FenceBuilderManager
{
   private static FenceBuilderManager _instance;
   private Map<Integer, L2FenceInstance> _fences = new FastMap<Integer, L2FenceInstance>();
   private Map<Integer, L2FenceInstance> _fence = new FastMap<Integer, L2FenceInstance>();
   
   
   public static FenceBuilderManager getInstance()
   {
      if ( _instance == null )
      {
         _instance = new FenceBuilderManager();
      }
      
      return _instance;
   }
   
   public void main_fence(L2PcInstance pc)
   {
      NpcHtmlMessage html = new NpcHtmlMessage(5);
        TextBuilder sb = new TextBuilder();
        sb.append("<html><title>Brick's Fence Builder</title><body>");
        sb.append("<table width=270>");
        sb.append("<tr></tr>");
        sb.append("<tr><td>New Fence:</td></tr>");
        sb.append("<tr><td>Type: </td></tr>");
        sb.append("<tr><td><combobox width=75 var=ts list=0;1;2></td></tr>");
        sb.append("<tr><td>Width: (min 150)</td></tr>");
        sb.append("<tr><td><td><edit var=\"tdist\"></td></tr>");
        sb.append("<tr><td>Lenght: (min 150)</td></tr>");
        sb.append("<tr><td><td><edit var=\"tyaw\"></td></tr>");
        sb.append("<tr><td>Hight: </td></tr>");
        sb.append("<tr><td><td><combobox width=75 var=tscreen list=1;2;3></td></tr>");
        sb.append("<tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<BR>");
        sb.append("<BR>");
        sb.append("<table width=270>");
        sb.append("<tr>");
        sb.append("<td><button value=\"spawn fence\" width=90 action=\"bypass -h admin_fence $ts $tdist $tyaw $tscreen\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td><button value=\"delete last created\" width=90 action=\"bypass -h admin_dellastspawned\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        sb.append("<td><button value=\"delete all fences\" width=90 action=\"bypass -h admin_delallspawned\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("</tr>");
        sb.append("</table></body></html>");
        html.setHtml(sb.toString());
        pc.sendPacket(html);
   }
   
   public void spawn_fence(L2PcInstance pc, int type, int wid, int hi, int size)
   {
      _fence.clear();
      for (int i = 0; i < size; i++)
      {
         int id = IdFactory.getInstance().getNextId();
         L2FenceInstance inst = new L2FenceInstance(id, type, wid, hi);
         inst.spawnMe(pc.getX(), pc.getY(), pc.getZ());
         _fences.put(id, inst);
         _fence.put(id, inst);
      }
      main_fence(pc);
   }
   
   public void del_last(L2PcInstance pc)
   {
      if (!_fence.isEmpty())
      {
         for (L2FenceInstance f : _fence.values())
         {
            if ( f != null )
            {
               L2WorldRegion region = f.getWorldRegion();
               f.decayMe();
               if (region != null)
                  region.removeVisibleObject(f);
               f.getKnownList().removeAllKnownObjects();
               L2World.getInstance().removeObject(f);
               _fences.remove(f);
               _fence.remove(f);
            }
         }
      }
      main_fence(pc);
   }
   
   public void del_all(L2PcInstance pc)
   {
      if (!_fences.isEmpty())
      {
         for (L2FenceInstance f : _fences.values())
         {
            if ( f != null )
            {
               L2WorldRegion region = f.getWorldRegion();
               f.decayMe();
               if (region != null)
                  region.removeVisibleObject(f);
               f.getKnownList().removeAllKnownObjects();
               L2World.getInstance().removeObject(f);
               _fences.remove(f);
               _fence.clear();
            }
         }
      }
      main_fence(pc);
   }
}