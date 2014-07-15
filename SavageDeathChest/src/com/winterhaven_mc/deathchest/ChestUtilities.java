package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WGBukkit;

public class ChestUtilities {
    final DeathChestMain plugin;

    public ChestUtilities(DeathChestMain plugin) {
        this.plugin = plugin;
    }


    /**
     * Get the cardinal compass direction.<br>
     * Converts direction in degrees to BlockFace direction (N,E,S,W)
     * 
     * @param yaw	Direction in degrees
     * @return BlockFace of cardinal direction
     */
    public BlockFace getChestDirectionFace(float yaw) {
    	double rot = yaw % 360;
    	if (rot < 0) {
    		rot += 360.0;
    	}
    	if (45 <= rot && rot < 135) {
    		return BlockFace.EAST;
    	}
    	else if (135 <= rot && rot < 225) {
    		return BlockFace.SOUTH;
    	}
    	else if (225 <= rot && rot < 315) {
    		return BlockFace.WEST;
    	}
    	else {
    		return BlockFace.NORTH;
    	}
    }


    /**
	 * Get cardinal compass direction.<p>
	 * Converts direction in degrees to Chest byte
	 * representing cardinal direction chest is facing. (N,E,S,W)
	 * 
	 * @param yaw		Direction in degrees	
	 * @return Byte		representing chest face direction
	 */
	public Byte getChestDirectionByte(float yaw) {
		double rot = yaw % 360;
		if (rot < 0) {
			rot += 360.0;
		}
		if (45 <= rot && rot < 135) {		// east
			return 0x5;
		}
		else if (135 <= rot && rot < 225) {	// south
			return 0x3;
		}
		else if (225 <= rot && rot < 315) {	// west
			return 0x4;
		}
		else {								// north
			return 0x2;
		}
	}
	

	/**
	 * Get the cardinal compass direction for sign post. (N,E,S,W)
	 * 
	 * @param yaw		Direction in degrees
	 * @return Byte		representing sign post direction
	 */
	public Byte getSignPostDirectionByte(float yaw) {
		double rot = yaw % 360;
		if (rot < 0) {
			rot += 360.0;
		}
		if (45 <= rot && rot < 135) { 		 // east
			return 0xC;
		}
		else if (135 <= rot && rot < 225) {	// south
			return 0x0;
		}
		else if (225 <= rot && rot < 315) {	// west
			return 0x4;
		}
		else {								// north
			return 0x8;
		}
	}


    /**
	 * Create a unique key string based on block location
	 * 
	 * @param blockstate Block to create unique location key string
	 * @return String key
	 */
	public String makeKey(BlockState blockstate) {
		String worldname = blockstate.getWorld().getName();
		String locX = String.valueOf(blockstate.getX());
		String locY = String.valueOf(blockstate.getY());
		String locZ = String.valueOf(blockstate.getZ());
		String key = worldname + "|" + locX + "|" + locY + "|" + locZ;
		return key;
	}


	/** Check if player has GriefPrevention chest access at location
	 * 
	 * @param player	Player to check permission
	 * @param location	Location to check permission
	 * @return boolean	true/false player has chest access at location
	 */
	public boolean gpPermission(Player player, Location location) {
		// if GriefPrevention option is enabled and GriefPrevention is present, check for chest access
		if (plugin.getConfig().getBoolean("griefprevention-nabled", true) && plugin.gp_loaded) {
			// if player does not have Grief Prevention chest access, spill inventory
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				String gpErrorMessage = claim.allowContainers(player);
				if (gpErrorMessage != null) {
					plugin.getLogger().info(gpErrorMessage);
					return false;
				}
			}
		}
		return true;
	}


	/** Check if player has WorldGuard build permission at location
	 * 
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean	true/false player has build permission at location
	 */
	private boolean wgPermission(Player player, Location location) {
		// if WorldGuard option is enabled and WorldGuard is installed, check for chest access
		if (plugin.getConfig().getBoolean("worldguard-enabled", true)) {
			if (WGBukkit.getPlugin().isEnabled()) {
				if (!WGBukkit.getPlugin().canBuild(player, location)) {
					return false;
				}
			}
		}
		return true;
	}


	/** Combine item stacks of same material up to max stack size
	 * 
	 * @param itemlist	List of itemstacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	public List<ItemStack> consolidateItems(List<ItemStack> itemlist) {

		List<ItemStack> returnlist = new ArrayList<ItemStack>();
		
		for (ItemStack itemstack : itemlist) {
			if (itemstack == null) {
				continue;
			}
			
			for (ItemStack checkstack : returnlist) {
				if (checkstack == null) {
					continue;
				}
				if (checkstack.isSimilar(itemstack)) {
					int transfer = Math.min(itemstack.getAmount(),checkstack.getMaxStackSize() - checkstack.getAmount());
					itemstack.setAmount(itemstack.getAmount() - transfer);
					checkstack.setAmount(checkstack.getAmount()	+ transfer);
				}
			}
			if (itemstack.getAmount() > 0) {
				returnlist.add(itemstack);
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info("There are " + returnlist.size() + " consolidated item stacks.");
		}
		return returnlist;
	}

  /*  public List<ItemStack> consolidateItems(List<ItemStack> itemlist) {
    	
        ArrayList<ItemStack> returnlist = new ArrayList<ItemStack>();

        for (ItemStack itemstack : itemlist) {
            if (itemstack == null) {
            	continue;
            }
            Iterator<ItemStack> iterator = returnlist.iterator();
            while (iterator.hasNext()) {
                ItemStack checkstack = (ItemStack)iterator.next();
                if (checkstack == null) {
                	continue;
                }
                if (checkstack.isSimilar(itemstack)) {
                	int transfer = Math.min(itemstack.getAmount(), checkstack.getMaxStackSize() - checkstack.getAmount());
                	itemstack.setAmount(itemstack.getAmount() - transfer);
                	checkstack.setAmount(checkstack.getAmount() + transfer);
                }
            }
            if (itemstack.getAmount() <= 0) {
            	continue;
            }
            returnlist.add(itemstack);
        }
        return returnlist;
    } */

	

	/** remove one chest from list of item stacks
	 * 
	 * @param list	List of itemstacks to remove chest
	 * @return List of itemstacks with one chest removed
	 */
	public List<ItemStack> removeOneChest(List<ItemStack> list) {
		
		for (ItemStack stack : list) {
			if (stack.isSimilar(new ItemStack(Material.CHEST))) {
				list.remove(stack);
				stack.setAmount(stack.getAmount() - 1);
				if (stack.getAmount() > 0) {
					list.add(stack);
				}
			break;
			}
		}
		return list;
	}


	/** Check if list of item stacks contains at least one chest
	 * 
	 * @param list List of itemstacks to check for chest
	 * @return boolean
	 */
	public boolean hasChest(List<ItemStack> list) {
		boolean haschest = false;
		for (ItemStack item : list) {
			if (item.getType().equals(Material.CHEST)) {
				haschest = true;
				break;
			}
		}
		return haschest;	
	}


	/**
	 * Get location to left of location based on yaw
	 * @param location initial location
	 * @return location one block to left
	 */
	public Location locactionToLeft(Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getChestDirectionFace(yaw)).getLocation();
	}


	/**
	 * Get block to left of location based on yaw
	 * @param location initial location
	 * @return block to left of location
	 */
	public Block blockToLeft(Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getChestDirectionFace(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 * @param location inital location
	 * @return block to right of initial location
	 */
	public Block blockToRight(Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getChestDirectionFace(yaw));
	}


	/**
	 * Get block in front of location based on yaw
	 * @param location initial location
	 * @return block in front of initial location
	 */
	public Block blockInFront(Location location) {
		float yaw = location.getYaw() + 180;
		return location.getBlock().getRelative(getChestDirectionFace(yaw));
	}

	
	/**
	 * Get block to rear of location based on yaw
	 * @param location initial location
	 * @return block behind inital location
	 */
	public Block blockToRear(Location location) {
		float yaw = location.getYaw();
		return location.getBlock().getRelative(getChestDirectionFace(yaw));
	}


	/**
	 * Search for a valid location to place a single chest,
	 * taking into account replaceable blocks, as well as 
	 * WorldGuard regions and GriefPrevention claims if configured
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for a single chest, or original location if valid location cannot be found
	 */
	public Location findValidSingleLocation(Player player) {

		Location origin = player.getLocation();
		
		int radius = plugin.getConfig().getInt("search-distance", 5);

		int ox = origin.getBlockX();
		int oy = origin.getBlockY();
		int oz = origin.getBlockZ();
		float oyaw = origin.getYaw();
		float opitch = origin.getPitch();
		World world = origin.getWorld();

		Location location = new Location(world,ox,oy,oz,oyaw,opitch);
		
		for (int y = 0; y < radius; y++) {
			for (int x = 0; x < radius; x++) {
				for (int z = 0; z < radius; z++) {
					location = new Location(world,ox+x,oy+y,oz+z,oyaw,opitch);
					if (validLocation(player,location)) {
						return location;
					}
					if (x == 0 && z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz+z,oyaw,opitch);
					if (validLocation(player,location)) {
						return location;
					}
					location = new Location(world,ox-x,oy+y,oz-z,oyaw,opitch);
					if (validLocation(player,location)) {
						return location;
					}
					location = new Location(world,ox+x,oy+y,oz-z,oyaw,opitch);
					if (validLocation(player,location)) {
						return location;
					}
				}
			}
		}
		return origin;
	}

	/**
	 * Search for valid location to place a double chest, 
	 * taking into account replaceable blocks, as well as 
	 * WorldGuard regions and GriefPrevention claims if configured
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for double chest deployment, or original location if valid location cannot be found
	 */
	public Location findValidDoubleLocation(Player player) {
	
		Location origin = player.getLocation();
		
		int radius = plugin.getConfig().getInt("search-distance", 5);
	
		int ox = origin.getBlockX();
		int oy = origin.getBlockY();
		int oz = origin.getBlockZ();
		float oyaw = origin.getYaw();
		float opitch = origin.getPitch();
		World world = origin.getWorld();
	
		Location location = new Location(world,ox,oy,oz,oyaw,opitch);
		
		for (int y = 0; y < radius; y++) {
			for (int x = 0; x < radius; x++) {
				for (int z = 0; z < radius; z++) {
					location = new Location(world,ox+x,oy+y,oz+z,oyaw,opitch);
					if (validLocation(player,location) &&
							validLocation(player,locactionToLeft(location))) {
						return location;
					}
					if (x == 0 && z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz-z,oyaw,opitch);
					if (validLocation(player,location) &&
							validLocation(player,locactionToLeft(location))) {
						return location;
					}
					if (x == 0 || z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz+z,oyaw,opitch);
					if (validLocation(player,location) &&
							validLocation(player,locactionToLeft(location))) {
						return location;
					}
					location = new Location(world,ox+x,oy+y,oz-z,oyaw,opitch);
					if (validLocation(player,location) &&
							validLocation(player,locactionToLeft(location))) {
						return location;
					}
				}
			}
		}
		return origin;
	}


	/** Check if chest can be placed at location
     * 
     * @param player	Player to check permissions
     * @param location	Location to check permissions
     * @return boolean
     */
    public boolean validLocation(Player player, Location location) {
    	Block block = location.getBlock();
    	// check if block at location is a ReplaceableBlock
    	if(!plugin.chestmanager.getReplaceableBlocks().contains(block.getType())) {
    		return false;
    	}
    	// check if player has GP permission at location
    	if (!gpPermission(player,location)) {
    		return false;
    	}
    	// check if player has WG permission at location 
    	if (!wgPermission(player,location)) {
    		return false;
    	}
    	return true;
    }

}

