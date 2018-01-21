package de.redstoneworld.redblockupdate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public final class RedBlockUpdate extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("redblockupdate").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3) {
            return false;
        }
        World world = null;
        if (args.length > 3) {
            if (sender.hasPermission("rwm.blockupdate.otherworld")) {
                world = getServer().getWorld(args[3]);
            } else {
                sendMessage(sender, ChatColor.RED + "You don't have the permission to target other worlds!");
                return true;
            }
        } else if (sender instanceof Entity) {
            world = ((Entity) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        } else {
            sender.sendMessage(ChatColor.RED + "You need to specify a world when running the command from the console!");
            return false;
        }

        Location coords = new Location(world, 0, 0, 0);

        if (sender instanceof Entity) {
            coords = ((Entity) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            coords = ((BlockCommandSender) sender).getBlock().getLocation();
        }

        try {
            coords.setX(handleCoordInput(coords.getBlockX(), args[0]));
            coords.setY(handleCoordInput(coords.getBlockY(), args[1]));
            coords.setZ(handleCoordInput(coords.getBlockZ(), args[2]));
        } catch (NumberFormatException e) {
            sendMessage(sender, ChatColor.RED + "Wrong number input! " + e.getMessage());
            return true;
        }

        if (!coords.getChunk().isLoaded()) {
            if (sender.hasPermission("rwm.blockupdate.loadchunk")) {
                if (!coords.getChunk().load(false)) {
                    sendMessage(sender, ChatColor.RED + "Chunk could not be loaded! It might not be generated yet?");
                    return true;
                }
            } else {
                sendMessage(sender, ChatColor.RED + "The target chunk isn't loaded and you don't have the permission to load it!");
                return true;
            }
        }

        Block block = coords.getBlock();
        BlockState originalState = block.getState();
        if (originalState instanceof Container) {
            ((Container) originalState).getInventory().clear();
        }
        block.setType(Material.AIR);
        if (originalState.update(true)) {
            sendMessage(sender, ChatColor.YELLOW + "Updated " + coords.getBlock().getType() + " block at " + coords.getWorld().getName() + "/" + coords.getBlockX() + "/" + coords.getBlockY() + "/" + coords.getBlockZ());
        } else {
            sendMessage(sender, ChatColor.RED + "Could not update " + coords.getBlock().getType() + " block at " + coords.getWorld().getName() + "/" + coords.getBlockX() + "/" + coords.getBlockY() + "/" + coords.getBlockZ());
        }

        return true;
    }

    private int handleCoordInput(int source, String arg) throws NumberFormatException {
        if (arg.startsWith("~")) {
            if (arg.length() > 1) {
                return source + Integer.parseInt(arg.substring(1));
            }
            return source;
        }
        return Integer.parseInt(arg);
    }

    private void sendMessage(CommandSender sender, String message) {
        if (!(sender instanceof Entity) || ((Entity) sender).getWorld().getGameRuleValue("sendCommandFeedback").equals("true")) {
            sender.sendMessage(message);
        }
    }
}
