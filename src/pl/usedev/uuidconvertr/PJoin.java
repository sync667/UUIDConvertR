package pl.usedev.uuidconvertr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PJoin implements Listener{

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event){
		String[] player = UUIDConvertR.getPlayer(event.getPlayer().getName());
		
		if(player!=null)
		{
			Player p = event.getPlayer();
			
			String inv = player[0];
			String armor = player[1];
			String ender = player[2];
			int exp = 0;
			try{
				exp = Integer.valueOf(player[3]);
			}catch(NumberFormatException e){
				e.printStackTrace();
			}
			String location = player[4];
			
			if(inv!=null)
			{
				p.getInventory().setContents(UUIDConvertR.restoreStacks(inv));
			}
			if(armor!=null)
			{
				p.getInventory().setArmorContents(UUIDConvertR.restoreStacks(armor));
			}
			if(ender!=null)
			{
				p.getEnderChest().setContents(UUIDConvertR.restoreStacks(ender));
			}
			if(exp!=0)
			{
				p.setTotalExperience(exp);
			}
			if(location!=null)
			{
				String[] loc = location.split(";");
				try{
				Location locL = null;
					locL = new Location(Bukkit.getWorld(loc[4]), Double.valueOf(loc[0]), Double.valueOf(loc[1]), Double.valueOf(loc[2]));
				if(locL != null)
					p.teleport(locL);
				}catch(Exception e){
					UUIDConvertR.log.warning(e.getMessage());
				}
				
			}
			
			UUIDConvertR.delPlayer(p.getName());
 		}
	}
}
