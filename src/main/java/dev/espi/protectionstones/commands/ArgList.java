/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones.commands;

import dev.espi.protectionstones.PSL;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArgList implements PSCommandArg {
    @Override
    public List<String> getNames() {
        return Collections.singletonList("list");
    }

    @Override
    public boolean allowNonPlayersToExecute() {
        return false;
    }

    @Override
    public List<String> getPermissionsToExecute() {
        return Arrays.asList("protectionstones.list");
    }

    @Override
    public HashMap<String, Boolean> getRegisteredFlags() {
        return null;
    }

    @Override
    public boolean executeArgument(CommandSender s, String[] args, HashMap<String, String> flags) {
        if (!s.hasPermission("protectionstones.list"))
            return PSL.msg(s, PSL.NO_PERMISSION_LIST.msg());

        if (args.length == 2 && !s.hasPermission("protectionstones.list.others"))
            return PSL.msg(s, PSL.NO_PERMISSION_LIST_OTHERS.msg());

        if (args.length == 2 && !UUIDCache.nameToUUID.containsKey(args[1]))
            return PSL.msg(s, PSL.PLAYER_NOT_FOUND.msg());

        PSPlayer psp = PSPlayer.fromPlayer((Player) s);

        // run query async to reduce load
        Bukkit.getScheduler().runTaskAsynchronously(ProtectionStones.getInstance(), () -> {
            if (args.length == 1) {
                List<PSRegion> regions = psp.getPSRegions(psp.getPlayer().getWorld(), true);
                display(s, regions, psp.getPlayer().getName(), psp.getUuid());
            } else if (args.length == 2) {
                UUID uuid = UUIDCache.nameToUUID.get(args[1]);
                List<PSRegion> regions = PSPlayer.fromUUID(uuid).getPSRegions(psp.getPlayer().getWorld(), true);
                display(s, regions, args[1], UUIDCache.nameToUUID.get(args[1]));
            } else {
                PSL.msg(s, PSL.LIST_HELP.msg());
            }
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }

    private void display(CommandSender s, List<PSRegion> regions, String pName, UUID pUUID) {
        List<String> ownerOf = new ArrayList<>(), memberOf = new ArrayList<>();
        for (PSRegion r : regions) {
            if (r.isOwner(pUUID)) {
                if (r.getName() == null) {
                    ownerOf.add(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getID());
                } else {
                    ownerOf.add(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getName() + " (" + r.getID() + ")");
                }
            }
            if (r.isMember(pUUID)) {
                if (r.getName() == null) {
                    memberOf.add(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getID());
                } else {
                    memberOf.add(ChatColor.GRAY + "> " + ChatColor.AQUA + r.getName() + " (" + r.getID() + ")");
                }
            }
        }

        PSL.msg(s, PSL.LIST_HEADER.msg().replace("%player%", pName));

        if (!ownerOf.isEmpty()) {
            PSL.msg(s, PSL.LIST_OWNER.msg());
            for (String str : ownerOf) s.sendMessage(str);
        }
        if (!memberOf.isEmpty()) {
            PSL.msg(s, PSL.LIST_MEMBER.msg());
            for (String str : memberOf) s.sendMessage(str);
        }
    }

}
