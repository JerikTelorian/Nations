package shizu.bukkit.nations.manager;

import java.util.HashMap;

import shizu.bukkit.nations.Nations;
import shizu.bukkit.nations.enums.GroupMemberType;
import shizu.bukkit.nations.enums.VoteResponse;
import shizu.bukkit.nations.object.Group;
import shizu.bukkit.nations.object.NAWObject;
import shizu.bukkit.nations.object.User;

import com.iConomy.*;

/**
 * Manages instances of the Group class and their interactions
 * between the server and data source.
 * 
 * @author Shizukesa
 */
public class GroupManagement extends Management {

	public GroupManagement(Nations instance) {
		
		super(instance);
		collection = new HashMap<String, NAWObject>();
		type = "group";
	}
	
	/**
	 * Fetches the Group for the provided group name, if it exists.
	 * 
	 * @param key The group name of the Group to get
	 * @return the Group with a matching name
	 */
	public Group getGroup(String key) {

		return (exists(key)) ? (Group) collection.get(key) : null;
	}
	
	/**
	 * Creates a nation Group for the commanding User and sets them
	 * in a position of leadership for that Group.
	 * 
	 * @param user The User creating the nation Group
	 * @param name The name of the new nation Group
	 * @return true if the nation was founded, false otherwise
	 */
	public boolean foundNation(User user, String name) {
		
		// TODO: Case insensitive check on nation name
		if (!exists(name)) {
			
			if (user.getNation().equals("")) {
				
				Group group = new Group(name);
				group.addMember(user.getName());
				group.addLeader(user.getName());
				user.setNation(name);
				iConomy.Accounts.create(name);
				collection.put(name, group);
				saveObject(name);
				plugin.messageAll("The Nation of '" + name + "' has been founded!");
				return true;
			} else {
				
				user.message("You are already a member of a nation. You must leave that nation before you can found a new one!");
				return false;
			}
		} else {
			
			user.message("A Nation with that name already exists!");
			return false;
		}	
	}
	
	/**
	 * Renames a nation and updates the ownership info for all members and plots in
	 * the nation.
	 * 
	 * @param user The commanding user
	 * @param name The new nation name
	 * @return true if the nation was renamed, false otherwise
	 */
	public boolean renameNation(User user, String name) {
		
		if (exists(name)) {
			
			user.message("A Nation with that name already exists!");
			return false;
		}
		
		if (!user.getNation().equals("")){
			
			Group nation = getGroup(user.getNation());
			
			if (nation.hasLeader(user.getName())) {
				
				plugin.messageAll("The Nation of '" + user.getNation() + "' is now the Nation of '" + name + "'!");
				deleteObject(user.getNation());
				nation.setName(name);
				collection.put(name, nation);
				saveObject(name);
				
				for (String plot : nation.getPlots()) {
					
					plugin.plotManager.getPlot(plot).setOwner(name);
				}
				
				for (String member : nation.getMembers()) {
					
					plugin.userManager.getUser(member).setNation(name);
				}
				
				return true;
				
			} else {
				
				user.message("You must be a leader to rename the nation!");
				return false;
			}
		} else {
			
			user.message("You are not part of a nation, and therefore, cannot rename it!");
			return false;
		}
		
	}
	
	public double getTaxRate(User user) {
		return getGroup(user.getNation()).getTax();
	}
	
	/**
	 * Sets the tax rate for the nation
	 * 
	 * @param user The commanding user
	 * @param rate The new tax rate
	 * @return true if the tax rate was set, false otherwise
	 */
	public boolean setTaxRate(User user, double rate) {
		
		if (!user.getNation().equals("")) {
			
			Group nation = getGroup(user.getNation());
			
			if (nation.hasLeader(user.getName())) {
				
				if (rate < 100 && rate > 0) {
					nation.setTax(rate);
					messageGroup(nation.getName(), GroupMemberType.MEMBERS, "The " + user.getNation() + " tax rate is now %" + nation.getTax());
					return true;
				}
				
				else {
					user.message("You must enter a number between 0 and 100.");
					return false;
				}
				
			} else {
				
				user.message("You must be a leader to set the tax rate!");
				return false;
			}
		} else {
			
			user.message("You must be the leader of a nation to set a tax rate!");
			return false;
		}
	}
	
	/**
	 * Changes diplomatic relationship with another nation.
	 * 
	 * @param user The User changing the diplomacy of his nation
	 * @param nation The Nation the user wants to change his diplomacy with
	 * @param status The status of the diplomatic relationship (ally, neutral, enemy)
	 */
	public void changeStatus(User user, String nation, String status) {
		
		Group group = getGroup(user.getNation());
		
		if (this.exists(nation) == true) {
			
			if (status.equalsIgnoreCase("ally")) {
				group.addAlly(nation);
				user.message(nation + " is now your ally.");
			}
			else if (status.equalsIgnoreCase("neutral")) {
				group.addNeutral(nation);
				user.message(nation + " is now neutral.");
			}
			else if (status.equalsIgnoreCase("enemy")) {
				group.addEnemy(nation);
				user.message(nation + " is now your enemy.");
			}
			else {
				user.message("Status parameters: 'ally', 'neutral', and 'enemy'");
				user.message("For example, '/naw diplomacy status Kentucky enemy'");
			}
		}
		
		else {
			user.message(nation + " does not exist.");
		}
	}
	
	/**
	 * Invites a User to the commanding User's nation.
	 * 
	 * @param user The commanding User
	 * @param invited The User to invite
	 * @return true if an invitation was sent, false otherwise
	 */
	public boolean inviteUserToNation(User user, String invited) {
		
		if (!plugin.userManager.exists(invited)) {
			
			user.message("That user does not exist or is not registered!");
			return false;
		}
		
		User invitee = plugin.userManager.getUser(invited);
		
		if (plugin.userManager.isLeader(user)) {
			
			if (invitee.getNation().equals("")) {
				
				invitee.addInvite(user.getNation());
				user.message("Invitation sent!");
				return true;
			} else {
				user.message("This user already belongs to a nation!");
				return false;
			}
		} else {
			user.message("You must be a leader to invite a user to the nation!");
			return false;
		}
	}
	
	/**
	 * Kicks a User from the commanding User's nation.
	 * 
	 * @param user The commanding User
	 * @param kicked The User to kick
	 * @return true if the User was kicked, false otherwise
	 */
	public boolean kickUserFromNation(User user, String kicked) {
		
		if (!plugin.userManager.exists(kicked)) {
			
			user.message("That user does not exist or is not registered!");
			return false;
		}
		
		User kickee = plugin.userManager.getUser(kicked);
		
		if (plugin.userManager.isLeader(user)) {
			
			if (kickee.getNation().equals(user.getNation())) {
				
				Group group = getGroup(user.getNation());
				kickee.message("You have been kicked from " + user.getNation() + "!");
				kickee.setNation("");
				group.removeMember(kickee.getName());
				group.removeLeader(kickee.getName());
				plugin.groupManager.messageGroup(user.getNation(), GroupMemberType.MEMBERS, kicked + " has been kicked from the nation!");
				return true;
			} else {
				user.message("This user does not belong to your nation, and cannot be kicked!");
				return false;
			}
		} else {
			user.message("You must be a leader to kick a user from the nation!");
			return false;
		}
	}
	
	/**
	 * Allows leaders to promote other members of the same nation to leader status.
	 * 
	 * @param user The User that is promoting the member.
	 * @param promoted The User that is being promoted
	 */
	public boolean promoteUser(User user, String promoted) {

		User member = plugin.userManager.getUser(promoted);
		Group nation = this.getGroup(user.getNation());
		
		if (!plugin.userManager.exists(promoted)) {
			user.message("That user does not exist or is not registered!");
			return false;
		}
		
		if (plugin.userManager.isLeader(user)) {

			if (user.getNation().equals(member.getNation())) {
				
				nation.addLeader(promoted);
				plugin.groupManager.messageGroup(nation.getName(), GroupMemberType.MEMBERS, promoted + " has been promoted to leadership!");
				return true;
			} else {
				user.message("You cannot promote users outside of your nation!");	
				return false;
			}
		}
		else {
			user.message("You must be the leader of your nation to promote a member!");
			return false;
		}
	}
	
	/**
	 * Allows leaders to demote other members of the same nation from leader status.
	 * 
	 * @param user The User that is demoting the leader.
	 * @param promoted The User that is being demoted
	 */
	public boolean demoteUser(User user, String demoted) {

		User member = plugin.userManager.getUser(demoted);
		Group nation = this.getGroup(user.getNation());
		
		if (!plugin.userManager.exists(demoted)) {
			user.message("That user does not exist or is not registered!");
			return false;
		}
		
		if (plugin.userManager.isLeader(user)) {
			
			if (user.getNation().equals(member.getNation())) {
			
				nation.removeLeader(demoted);
				plugin.groupManager.messageGroup(nation.getName(), GroupMemberType.MEMBERS, demoted + " has been demoted from leadership!");
				return true;
			} else {
				user.message("You cannot demote users outside of your own nation!");
				return false;
			}
		}
		else {
			user.message("You must be the leader of your nation to demote a leader!");
			return false;
		}
	}
	
	/**
	 * Joins a User to the given Nation as a member.
	 * 
	 * @param user The User to join
	 * @param nation The Nation to join
	 * @return true if the User has joined, false otherwise
	 */
	public boolean joinNation(User user, String nation) {
	
		if (!exists(nation)) {
			
			user.message("No nation with that name exists!");
			return false;
		}
		
		if (user.getNation().equals("")) {
			
			Group group = getGroup(nation);
			user.setNation(nation);
			group.addMember(user.getName());	
			user.message("You have joined " + nation + "!");
			return true;
		} else {
			user.message("You must first leave your nation before you can join another!");
		}
		
		return false;
	}
	
	/**
	 * Removes a User from the Nation they are currently in.
	 * 
	 * @param user The commanding User
	 * @return true if the user left, false otherwise
	 */
	public boolean leaveNation(User user) {
	
		if (!user.getNation().equals("")) {
			
			Group group = getGroup(user.getNation());
			user.message("You have left " + user.getNation() + "!");
			user.setNation("");
			group.removeMember(user.getName());
			group.removeLeader(user.getName());
			return true;
		} else {
			user.message("You cannot leave a nation you are not a part of!");
		}
		
		return false;
	}
	
	/**
	 * Disbands a nation, razing all plots and removing all members and leaders.
	 * @param user
	 * The user that initiated the disband
	 * @param groupKey
	 * The key for the group to be disbanded
	 */
	public void disbandNation(User user, String groupKey)
	{		
		// TODO: It may be useful to set this on a timer to allow people to get their affairs in order
		Group group = getGroup(groupKey);
		
		if (group.hasLeader(user.getName()))
		{
			String disbandStartMsg = "Despair denizens of " + group.getName() + "! Your nation is ending!" ;
			String disbandFinishMsg = group.getName() + " has been lost to the dusts of time!";			
			this.messageGroup(groupKey, GroupMemberType.MEMBERS, disbandStartMsg);
			
			// Remove all plots. Should be first so people are not locked out of their stuff.
			Boolean result = plugin.plotManager.razeGroupPlots(group.getPlots());
			if (result)
				messageGroup(groupKey, GroupMemberType.MEMBERS, "All plots razed.");
			else
				messageGroup(groupKey, GroupMemberType.MEMBERS, "Error: Some plots not razed! (GroupManagement.disbandNation)");
			
			// Remove all leaders.
			for (String leader : group.getLeaders())
			{
				group.removeLeader(leader);
			}
			messageGroup(groupKey, GroupMemberType.MEMBERS, group.getName() +"'s leaders deposed!");
			
			// Remove all members and let them know how that shit be.
			for (String member : group.getMembers())
			{
				group.removeLeader(member);
				User memberToRemove = plugin.userManager.getUser(member);
				memberToRemove.message(group.getName() +"is gone. You are thrust into the wilderness alone!");
				memberToRemove.setNation("");
			}
			plugin.getServer().broadcastMessage(disbandFinishMsg);
		}
		else
			user.message("You lack the autority to disband a Nation.");
	}
	
	/**
	 * Sends a message to a subset of group members.
	 * @param key 
	 * The key for the group to message
	 * @param memberType 
	 * The subset of members to send the message to, based on type.
	 * @param message 
	 * The message to send.
	 */
	public void messageGroup(String key, GroupMemberType memberType, String message)
	{

		Group group = getGroup(key);
		switch(memberType)
		{
		case NONLEADERS:
			for (String member : group.getMembers())
			{
				User user = plugin.userManager.getUser(member);
				if(group.hasMember(member) && !group.hasLeader(member))
						user.message("[" + key + "]: " + message);
			}
			break;
		case LEADERS:
			for (String member : group.getMembers())
			{
				User user = plugin.userManager.getUser(member);
				if(group.hasLeader(member))
						user.message("[" + key + "]: " + message);
			}
			break;
		case MEMBERS:
			for (String member : group.getMembers())
			{
				User user = plugin.userManager.getUser(member);
				if(group.hasMember(member))
						user.message("[" + key + "]: " + message);
			}
			break;
		}
	}
	
	
	/**
	 * Initiates a vote in the User's group.
	 * @param user The user that initiated the vote
	 * @param targetGroup The group 
	 * @return
	 */
	public boolean startGroupVote(User user, GroupMemberType targetGroup, String issueOnTable)
	{
		// TODO: UNFINISHED -- Jerik
		boolean result = false;
		Group group = this.getGroup(user.getNation());
		
		if(user.getNation().equals(""))
		{
			user.message("You are not a member of any Nation!");
		} else // If the user is in a Nation
		{	
			if (targetGroup == GroupMemberType.LEADERS)
				if(group.hasLeader(user.getName()))
				{
					//group.setVotingGroup(targetGroup); group.setVoteInProgress(true);
					result = true;
				}
				else
				{
					user.message("You cannot start a Leadership vote.");
				}
			else
				//group.setVotingGroup(targetGroup); group.setVoteInProgress(true);
				result = true;
		}		
		
		if (result)
			messageGroup(group.getName(), targetGroup, "Vote initiated by " + user.getName() + "Voting is now open for one minute.");
			//group.resetVoteStartTime();
		
		return result;
	}
	
	// TODO: Finish --Jerik
	public void placeVote(User user, VoteResponse vote)
	{
		//Group group = getGroup(user.getNation());
		//if(group.isVoteInProgress())
		{
			
		}
	}
	
	// TODO: Finish --Jerik
	public int getNumberOfActivePlayers()
	{
		int result = 0;
		
		//for(String player : members)
		{
			
			
		}
		
		return result;
	}
	
	
	public void createGroup() {
		//PLACEHOLDER
	}
	
	public void deleteGroup() {
		//PLACEHOLDER
	}
}
