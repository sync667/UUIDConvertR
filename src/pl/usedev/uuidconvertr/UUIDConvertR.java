package pl.usedev.uuidconvertr;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.utility.StreamSerializer;

public class UUIDConvertR extends JavaPlugin{

	public static UUIDConvertR plugin;
	public final static Logger log = Logger.getLogger("Minecraft");
		public static Database localConn;
	    public String storageHostname;
	    public String storageDatabase;
	    public String storageUsername;
	    public String storagePassword;
	    public String storagePrefix;
		public int storagePort;


	@Override
	public void onEnable() {
		reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        storagePort = getConfig().getInt("storage.port");
        storageHostname = getConfig().getString("storage.hostname");
        storageDatabase = getConfig().getString("storage.database");
        storageUsername = getConfig().getString("storage.username");
        storagePassword = getConfig().getString("storage.password");
        storagePrefix = getConfig().getString("storage.prefix");
       
        plugin = this;
		
		if (getServer().getPluginManager().getPlugin("ProtocolLib") != null)
	    {
			// Initialise database
	        localConn = new Database(storageUsername, storagePassword, "jdbc:mysql://" + storageHostname + ":" + storagePort + "/" + storageDatabase + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=3" + ("&useUnicode=true&characterEncoding=utf-8"), plugin);

	        if (!localConn.checkConnection()) {
	                log.severe("Unable to connect to the database, it has been disabled");
	                plugin.getPluginLoader().disablePlugin(this);
	                return;
	        }
	        try {
				create_tables();
			} catch (SQLException e) {
				log.warning("[UUIDConvertR] Can't create database table - disabling!");
				plugin.getPluginLoader().disablePlugin(this);
                return;
			}
						
			if(!this.getConfig().getBoolean("restore"))
			{		    
				if(Bukkit.getServer().getVersion().contains("1.7.9"))
				{
					log.warning("[UUIDConvertR] Your server is on 1.7.9 change restore mode or use 1.7.2-R0.3!");
					plugin.getPluginLoader().disablePlugin(this);
					return;
				}
				saveAll();
			}
			else
			{
				Bukkit.getPluginManager().registerEvents(new PJoin(), plugin);

					if(left() == 0)
					{
						log.warning("[UUIDConvertR] You're in restore mode, but I don't have data to restore. Maybe your all players get theirs data :) or something whent wrong ;(");
					}
					else
					{
						log.info("[UUIDConvertR] Players to restore left: " + left());
					}

			}
	    }
		else
		{
			log.warning("[UUIDConvertR] ProtocolLib NOT found - disabling!");
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable() {
		closeConnection();
	}
	
	  static String buildArmorData(ItemStack[] itemStacks)
	  {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (int i = 0; i < itemStacks.length; i++)
	    {
	      if (i > 0) {
	        stringBuilder.append(";");
	      }
	      if (itemStacks[i] != null) {
	        try
	        {
	          stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStacks[i]));
	        }
	        catch (IOException e)
	        {
	          e.printStackTrace();
	        }
	      }
	    }
	    return stringBuilder.toString();
	  }
	  
	  static String buildStacksData(ItemStack[] itemStacks)
	  {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (int i = 0; i < itemStacks.length; i++)
	    {
	      if (i > 0) {
	        stringBuilder.append(";");
	      }
	      if ((itemStacks[i] != null) && (itemStacks[i].getType() != Material.AIR)) {
	        try
	        {
	          stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStacks[i]));
	        }
	        catch (IOException e)
	        {
	          e.printStackTrace();
	        }
	      }
	    }
	    return stringBuilder.toString();
	  }
	  
	  static ItemStack[] restoreStacks(String string)
	  {
	    if (string != null)
	    {
	      String[] strings = string.split(";");
	      ItemStack[] itemStacks = new ItemStack[strings.length];
	      for (int i = 0; i < strings.length; i++) {
	        if (!strings[i].equals("")) {
	          try
	          {
	            itemStacks[i] = StreamSerializer.getDefault().deserializeItemStack(strings[i]);
	          }
	          catch (IOException e)
	          {
	            e.printStackTrace();
	          }
	        }
	      }
	      return itemStacks;
	    }
	    return new ItemStack[] { new ItemStack(Material.AIR) };
	  }
	  
	  static String buildLocationData(Location loc)
	  {
		  StringBuilder stringBuilder = new StringBuilder();
		  stringBuilder.append(loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getWorld().getName());
		  
		  return stringBuilder.toString();
	  }
	  
	  public static int left()
	  {
		  int left = 0;
		  ResultSet result = null;
	        try{
	            result = localConn.query("SELECT `nick` FROM `UUIDConvertR_data` WHERE 1");
	          
	            while(result.next()){
	            	left++;
	            }
	            
	            result.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return left;
	  }
	  
	public static String[] getPlayer(String nick)
	  {
		  ResultSet result = null;
		  String[] res = null;

		  try{
			  String query = "SELECT * FROM `UUIDConvertR_data` WHERE `nick` = '" + nick + "'";
			  
			  result = localConn.query(query);
			  
			if(result.next()){
				 res = new String[]{result.getString("inv"), result.getString("armor"), result.getString("ender"), String.valueOf(result.getInt("exp")), result.getString("location")};
			}
			
			if(res == null){
				log.warning("GetPlayer null? !");
			}
			
			result.close();
		        
		  }
		  catch(SQLException e)
		  {
			  e.printStackTrace();
		  }
		  return res;
	  }
	  
	  public static void delPlayer(String nick)
	  {	 
		  String query = "DELETE FROM `UUIDConvertR_data` WHERE `nick` = '" + nick + "'";
		  localConn.query(query);
	
	  }
	  
	  public static void insertPlayer(String nick)
	  {
		 
		  String query = "INSERT INTO UUIDConvertR_data (`nick`, `inv`, `armor`, `ender`, `exp`, `location`)"
				  + "VALUES ('" + nick + "', null, null, null, '0', null)";
		  localConn.query(query);		
	  }
	  
	  public static void saveAll()
	  {
		  log.info("[UUIDConvertR] Saving Proccess Started! Please be patient.");
		  localConn.query("TRUNCATE TABLE `UUIDConvertR_data`");
		  
	    OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
	    if (players.length > 0) {
	      
	        int progress = 0;
	        int errors = 0;
	        for (OfflinePlayer player : players)
	        {

	        	Player target;
	        	target = pl.usedev.uuidconvertr.PlayerDataManager.loadPlayer(player.getName());

                if (target == null) {
                	errors++;
                	log.warning("[UUIDConvertR]  Player not found: " + player.getName());
                   
                }
                else
                {
                    if(player.getName()==null)
            		{
                    	errors++;
            			log.warning("[UUIDConvertR] Failed to saving data of player: " + player.getName());
            		}
            		else
            		{
            			
            			insertPlayer(player.getName());
    	        	String query = "UPDATE `UUIDConvertR_data` SET `inv` = '" + buildStacksData(target.getInventory().getContents()) + 
    	  	        		"', `armor` = '" + buildStacksData(target.getInventory().getArmorContents()) + 
    	  	        		"', `ender` = '" + buildStacksData(target.getEnderChest().getContents()) + 
    	  	        		"', `exp` = '" + target.getTotalExperience() + 
    	  	        		"', `location` = '" + buildLocationData(target.getLocation()) + 
    	  	        		"' WHERE `nick` = '" + player.getName() + "'";
    	  	        
    	  	        localConn.query(query);

            		}
    	        	
                }
	        	
	          progress++;
	          
	          if(progress % 100 == 0)
	        	  log.info("\r[UUIDConvertR] Saving Progress |" + progress + "/" + players.length + "|");
	          
	        }
	        
	        if(errors==0)
	        {
	        	log.info("\n[UUIDConvertR] saved all players data (" + progress + ")! Now turn off server, change 'restore' in config to true, and turn server on. Players will get theirs things on join.");
	        }
	        else
	        {
		        log.info("[UUIDConvertR] saved: " + (progress-errors) + " players, but fail: " + errors + "! If you like to skip errors turn off server, change 'restore' in config to true, and turn server on. Players will get theirs things on join.");
	        }
	      
	    }
		}
		  
	  public void create_tables() throws SQLException {
          boolean Table = localConn.createTable("CREATE TABLE IF NOT EXISTS `UUIDConvertR_data` (`nick` varchar(18) NOT NULL,`inv` TEXT NULL,`armor` TEXT NULL,`ender` TEXT NULL,`exp` int(100) DEFAULT '0', `location` varchar(255) NULL,PRIMARY KEY (`nick`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");

          if (!Table){
                 log.severe("Unable to create UUIDConvertR table");
          }
	  }
	  
	    public void closeConnection() {
			localConn.close();		
		}

}
