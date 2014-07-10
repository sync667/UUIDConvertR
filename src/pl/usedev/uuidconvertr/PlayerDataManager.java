package pl.usedev.uuidconvertr;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.entity.Player;

public class PlayerDataManager

/*
 * Thanks for OpenInv plugin author for method!
 */
{
  public static Player loadPlayer(String name)
  {
    try
    {
      File playerfolder = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "players");
      if (!playerfolder.exists()) {
        return null;
      }
      String playername = matchUser(Arrays.asList(playerfolder.listFiles()), name);
      if (playername == null) {
        return null;
      }
      MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
      
      GameProfile profile = new GameProfile(null, playername);
      
      EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));
      

      Player target = entity == null ? null : entity.getBukkitEntity();
      if (target != null)
      {
        target.loadData();
        
        return target;
      }
    }catch(Exception e){
		UUIDConvertR.log.warning(e.getMessage());
	}
    return null;
  }
  
  private static String matchUser(Collection<File> container, String search)
  {
    String found = null;
    if (search == null) {
      return found;
    }
    String lowerSearch = search.toLowerCase();
    int delta = 2147483647;
    for (File file : container)
    {
      String filename = file.getName();
      String str = filename.substring(0, filename.length() - 4);
      if (str.toLowerCase().startsWith(lowerSearch))
      {
        int curDelta = str.length() - lowerSearch.length();
        if (curDelta < delta)
        {
          found = str;
          delta = curDelta;
        }
        if (curDelta == 0) {
          break;
        }
      }
    }
    return found;
  }
}