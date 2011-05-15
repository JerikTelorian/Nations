package shizu.bukkit.nations;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import shizu.bukkit.nations.event.NationsBlockListener;
import shizu.bukkit.nations.event.NationsUserListener;
import shizu.bukkit.nations.manager.GroupManagement;
import shizu.bukkit.nations.manager.PlotManagement;
import shizu.bukkit.nations.manager.UserManagement;
import shizu.bukkit.nations.object.User;

/**
 * Nations At War plugin class
 * 
 * @author Shizukesa
 */
public class Nations extends JavaPlugin {
	
	// TODO: Add color to player notifications

	private static final Logger log = Logger.getLogger("Minecraft");

	public PlotManagement plotManager = new PlotManagement(this);
	public UserManagement userManager = new UserManagement(this);
	public GroupManagement groupManager = new GroupManagement(this);
	public NationsBlockListener blockListener = new NationsBlockListener(this);
	public NationsUserListener userListener = new NationsUserListener(this);
    
	public void onEnable() {
		
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, userListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, userListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, userListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, userListener, Event.Priority.High, this);

		plotManager.loadAll();
		groupManager.loadAll();
		userManager.loadAll();
		sendToLog("Nations At War Plugin Loaded");
	}

	public void onDisable() {
		
		plotManager.saveAll();
		groupManager.saveAll();
		userManager.saveAll();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		
		// TODO: Make this not suck
		String name = ((Player) sender).getDisplayName();
		User user = (userManager.exists(name)) ? userManager.getUser((Player) sender) : null;
		
		if (user != null) {
			
			if (commandLabel.equalsIgnoreCase("naw")) {
				
				//FOR QUICK TESTING/DEBUGGING
				if (args[0].equalsIgnoreCase("test")) {
					
				}
				
				if (args[0].equalsIgnoreCase("invites")) {
					
					if (args[1].equalsIgnoreCase("")) {
						user.viewInvites();
					}
					
					if (args[1].equalsIgnoreCase("accept")) {
						userManager.acceptInvite(user, args[2]);
					}
					
					if (args[1].equalsIgnoreCase("clear")) {
						user.clearInvites();
					}
				}
				
				if (args[0].equalsIgnoreCase("plot")) {
					
					if (args[1].equalsIgnoreCase("claim")) {
						plotManager.claimPlot(user);
					}
					
					if (args[1].equalsIgnoreCase("raze")) {
						plotManager.razePlot(user);
					}
					
					if (args[1].equalsIgnoreCase("sell")) {
						plotManager.resellPlot(user);
					}
					
					if (args[1].equalsIgnoreCase("rent")) {
						plotManager.rentPlot(user);
					}
					
					if (args[1].equalsIgnoreCase("region")) {
						plotManager.setRegion(user, args[2]);
					}
					
					if (args[1].equalsIgnoreCase("buy")) {
						plotManager.buyPlot(user);
					}
				}
				
				if (args[0].equalsIgnoreCase("nation")) {
					
					if (args[1].equalsIgnoreCase("found")) {
						groupManager.foundNation(user, args[2]);
					}
					
					if (args[1].equalsIgnoreCase("invite")) {
						groupManager.inviteUserToNation(user, args[2]);
					}
					
					if (args[1].equalsIgnoreCase("kick")) {
						groupManager.kickUserFromNation(user, args[2]);
					}
					
					//TODO: test
					if (args[1].equalsIgnoreCase("promote")) {
						groupManager.promoteUser(user, args[2]);
					}
					
					//TODO: test
					if (args[1].equalsIgnoreCase("demote")) {
						groupManager.demoteUser(user, args[2]);
					}
					
					if (args[1].equalsIgnoreCase("leave")) {
						groupManager.leaveNation(user);
					}
					
					if (args[1].equalsIgnoreCase("disband")) {
						//PLACEHOLDER - disbands the nation; kick all members, raze all plots, delete object
					}
					
					if (args[1].equalsIgnoreCase("rename")) {
						//PLACEHOLDER - renames the nation
					}
					
					if (args[1].equalsIgnoreCase("tax")) {
						//PLACEHOLDER - sets the tax rate for the nation; used for renting/buying plots, maybe more?
					}
				}
				
				/* Diplomacy Section
				 * 
				 * Info - Lists your nation's allies and enemies
				 * Ally - Allies the subsequent nation
				 * */
				if (args[0].equalsIgnoreCase("diplomacy")) {
					
					if (args[1].equalsIgnoreCase("info")) {
						ArrayList<String> allies = groupManager.getGroup(user.getNation()).getAllies();
						ArrayList<String> enemies = groupManager.getGroup(user.getNation()).getEnemies();
						String allyList = "";
						String enemyList = "";
						
						user.message(ChatColor.getByCode(5) + "YOUR NATION: " + (groupManager.exists(user.getNation()) ? user.getNation() : "No Nation"));
						
						if (allies.size() > 0) {
							for(int i=0;i<allies.size();i++)
							{
								allyList = allyList + allies.get(i) + ", ";
							}
							user.message(ChatColor.getByCode(2) + "Allies: " + allyList.substring(0, allyList.length() - 2) + ".");
						}
						else {
							user.message(ChatColor.getByCode(2) + "Allies: None");
						}
						
						if (enemies.size() > 0) {
							for(int i=0;i<enemies.size();i++)
							{
								enemyList = enemyList + enemies.get(i) + ", ";
							}
							user.message(ChatColor.getByCode(12) + "Enemies: " + enemyList.substring(0, enemyList.length() - 2) + ".");
						}
						else {
							user.message(ChatColor.getByCode(12) + "Enemies: None");
						}
					}
					
					if (args[1].equalsIgnoreCase("status") && userManager.isLeader(user) == true) {
						groupManager.changeStatus(user, args[2], args[3]);
					}
				}
				
				
				
			}
			return true;
		}
		
		((Player) sender).sendMessage("You must be registered in NAW to use this functionality");
		return false;
	}
	
	public void sendToLog(String message) {
		log.info("[NationsAtWar]: " + message);
	}
	
	public void messageAll(String message) {
		this.getServer().broadcastMessage("[NationsAtWar]: " + message);
	}
	
	public World getWorld() {
		return this.getServer().getWorld(properties.getProperty("world_name"));
	}
}
