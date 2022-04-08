/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.deecat.slayer;

import com.google.inject.Provides;
import dev.hoot.api.entities.Players;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.magic.Magic;
import dev.hoot.api.magic.Regular;
import dev.hoot.fighter.VirtualKeyboard;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.deecat.MyItems;
import net.runelite.client.plugins.deecat.externals.utils.ExtUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;

import dev.hoot.api.game.Game;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.WorldResult;


import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Integer.parseInt;


@PluginDescriptor(
        name = "DC Slayer",
        description = "Slayer task helper",
        tags = {"slayer", "dc", "overlay", "tags"}
)
@Slf4j
public class DCSlayerPlugin extends Plugin
{

    private static final int MAX_PLAYER_COUNT = 1950;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private DCSlayerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DCSlayerOverlay DCSlayerOverlay;

    @Inject
    private MyItems items;

    @Inject
    private ExtUtils utils;

    @Inject
    private KeyManager keyManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private WorldService worldService;

    private int lastRubbed;
    private long lastHopped;
    private List<NPC> interacting;

    public Player activePker;
    public Player targeted;
    public int level;
    public boolean pkers = false;
    public boolean hopFailed = false;
    public boolean teleblocked = false;
    public boolean teleportFailed = false;

    private final HotkeyListener teleKeyListener = new HotkeyListener(() -> config.teleKey())
    {
        @Override
        public void hotkeyPressed()
        {
            clientThread.invoke(() -> teleport());
        }
    };


    @Provides
    DCSlayerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DCSlayerConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(DCSlayerOverlay);
        keyManager.registerKeyListener(teleKeyListener);
        interacting = client.getNpcs();
        reset();
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(DCSlayerOverlay);
        keyManager.unregisterKeyListener(teleKeyListener);
        interacting.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {

        Player local = Players.getLocal();
        wildyLevel = Game.getWildyLevel();
        minCombatLevel = Math.max(3, local.getCombatLevel() - wildyLevel);
        maxCombatLevel = Math.min(Experience.MAX_COMBAT_LEVEL, local.getCombatLevel() + wildyLevel);

        if (wildyLevel == 0) teleblocked = false;

        if (config.blastOff()) gottaBlast();

        if (config.appendDefault() && wildyLevel <= 30 && wildyLevel != 0)
        {
            teleport();
        }
    }
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals("dcslayer"))
        {
            executor.execute(this::reset);
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

    }

    @Subscribe
    public void onChatMessage(ChatMessage message){

        String msg = Text.removeTags(message.getMessage());
        if (message.getType() == ChatMessageType.GAMEMESSAGE &&
                msg.startsWith("Congratulations"))
        {
            VirtualKeyboard.sendKeys(KeyEvent.VK_SPACE);
        }

        if (message.getType() == ChatMessageType.PUBLICCHAT &&
                msg.contains("//"))
        {
            int world = Integer.parseInt(msg);
            log.info("Attempting hop to world " + world);
            hop(world);
        }

        if (message.getType() == ChatMessageType.GAMEMESSAGE &&
                msg.contains("You cannot switch worlds so soon after combat"))
        {
            hopFailed = true;
        }

        if (message.getType() == ChatMessageType.GAMEMESSAGE && msg.contains("Tele Block spell"))
        {
            teleblocked = true;
        }
        if (message.getType() == ChatMessageType.GAMEMESSAGE && msg.contains("You can't use this teleport after level"));
        {
            teleportFailed = true;
        }
        if (message.getType() == ChatMessageType.GAMEMESSAGE && msg.contains("Oh dear, you are dead"));
        {
            teleportFailed = false;
            teleblocked = false;
            hopFailed = false;
        }
        if (message.getType() == ChatMessageType.GAMEMESSAGE && msg.contains("You rub the"))
        {
            log.info(msg.toUpperCase());
            if (items.isGlory(lastRubbed)) {
                VirtualKeyboard.sendKeys(1);
            }

        }
        if (message.getType() == ChatMessageType.SPAM && msg.contains("You rub the"))
        {
            if (items.isWealth(lastRubbed))
            {
                VirtualKeyboard.sendKeys(2);
            }
        }

    }

    private void reset() {

        activePker = null;
        hopFailed = false;
        hopDisabled = false;
        teleblocked = false;
        teleportFailed = false;
    }

    private boolean hopDisabled = false;
    private void gottaBlast() {

        Player local = client.getLocalPlayer();

        if (Game.getWildyLevel() == 0) {
            activePker = null;
            teleblocked = false;
            hopFailed = false;
            hopDisabled = false;
            return;
        }

        if (Instant.now().toEpochMilli() - lastHopped >= 4000)
        {
            hopFailed = false;
        }

        int count = 0;
        for (Player player : client.getPlayers())
        {
            if (player == null || player == local || config.ignore().contains(player.getName()))
            {
                continue;
            }
            if (withinRange(player))
            {
                count ++;

                WorldPoint localWp = local.getWorldLocation();
                WorldPoint pWp = player.getWorldLocation();
                int range = localWp.distanceTo(pWp);

                if (player.getSkullIcon() == SkullIcon.SKULL)
                {
                    client.playSoundEffect(SoundEffectID.TOWN_CRIER_BELL_DING);
                    activePker = player;
                }

                if (player.getInteracting() == client.getLocalPlayer())
                {
                    targeted = player;
                    girdYourLoins();
                }

                if (hopFailed || hopDisabled)
                {
                    log.info("Hop failed: " + hopFailed + "; disabled: " + hopDisabled);
                    hopFailed = false;
                    hopDisabled = false;

                    if (teleblocked)
                    {
                        client.playSoundEffect(SoundEffectID.PRAYER_DEPLETE_TWINKLE);
                        log.info("Teleblocked -> " + player.getName());
                        girdYourLoins();
                    }
                    else {
                        if (config.teleOptions() == BlastOff.IN_RANGE && range > 11
                            || config.teleOptions() == BlastOff.TARGETED && activePker != player
                            || config.teleOptions() == BlastOff.NEVER)
                        {
                            continue;
                        }
                        log.info("Teleporting..");
                        teleport();
                        break;
                    }
                }
                else {
                    if ((config.hopOptions() == BlastOff.IN_RANGE && range > 11)
                            || (config.hopOptions() == BlastOff.TARGETED && activePker != player)
                            || (config.hopOptions() == BlastOff.HOP_ABOVE_20 && utils.wildernessLevel < 20)
                            || (config.hopOptions() == BlastOff.HOP_ABOVE_30 && utils.wildernessLevel < 30)
                            || (config.hopOptions() == BlastOff.NEVER))
                    {
//                        hopDisabled = true;
                        continue;
                    }

                    log.info((config.hopOptions().getName() + " -> " + player.getName() + " [lvl " + player.getCombatLevel() + "]"));
                    if (Instant.now().toEpochMilli() - lastHopped > config.hopThresh())
                    {
                        client.playSoundEffect(SoundEffectID.TELEPORT_VWOOP);
                        log.info("Hopping with hotkey...");
                        // THIS IS HOW TOU HOP OUT. MUST MATCH WORLD HOPPER "PREVIOUS" KEY
//                        clientThread.invoke(() -> hop(true));
                        VirtualKeyboard.sendKeys(config.hopKey().getKeyCode());
                        hopDisabled = false;
                        lastHopped = Instant.now().toEpochMilli();
                        break;
                    }
                }
            }
        }
        pkers = count > 0;

    }

    public int wildyLevel = 0;
    public int minCombatLevel = 0;
    public int maxCombatLevel = 0;

    private boolean withinRange(Player p) {
        Player local = Players.getLocal();
        wildyLevel = Game.getWildyLevel();
        minCombatLevel = Math.max(3, local.getCombatLevel() - wildyLevel);
        maxCombatLevel = Math.min(Experience.MAX_COMBAT_LEVEL, local.getCombatLevel() + wildyLevel);
        if (wildyLevel == 0) return false;
        return p.getCombatLevel() >= minCombatLevel && p.getCombatLevel() <= maxCombatLevel;
    }

    private void teleport() {
        Item glory = Inventory.getFirst(item -> item.getName().contains("glory("));
        Item tab = Inventory.getFirst(item -> item.hasAction("Break"));
        if (glory != null)
        {
            log.info("Teleporting ABOVE lvl 20 -> "+ glory.getName());
            glory.interact("Rub");
            VirtualKeyboard.sendKeys(KeyEvent.VK_1);
            lastRubbed = glory.getId();
        }
        else if (tab != null)
        {
            log.info("Teleporting BELOW lvl 20 -> " + tab.getName());
            tab.interact("Break");
            lastRubbed = tab.getId();
        }
        else if (Inventory.contains(ItemID.LAW_RUNE) && Inventory.contains(ItemID.FIRE_RUNE))
        {
            log.info("Teleporting -> VARROCK");
            Magic.cast(Regular.VARROCK_TELEPORT);
        }
    }

    private void girdYourLoins(){
        Rectangle b;

        if (client.getVar(Varbits.QUICK_PRAYER) == 0) {
            b = client.getWidget(WidgetInfo.MINIMAP_PRAYER_ORB).getBounds();
//            VirtualKeyboard.click(Utility.randomPoint(b));
        }

    }

    private void hop(boolean random)
    {
        WorldResult worldResult = worldService.getWorlds();
        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        net.runelite.http.api.worlds.World currentWorld = worldResult.findWorld(client.getWorld());

        if (currentWorld == null)
        {
            return;
        }

        EnumSet<net.runelite.http.api.worlds.WorldType> currentWorldTypes = currentWorld.getTypes().clone();

        currentWorldTypes.remove(net.runelite.http.api.worlds.WorldType.PVP);
        currentWorldTypes.remove(net.runelite.http.api.worlds.WorldType.HIGH_RISK);
        // Don't regard these worlds as a type that must be hopped between
        currentWorldTypes.remove(net.runelite.http.api.worlds.WorldType.BOUNTY);
        currentWorldTypes.remove(net.runelite.http.api.worlds.WorldType.SKILL_TOTAL);
        currentWorldTypes.remove(net.runelite.http.api.worlds.WorldType.LAST_MAN_STANDING);

        List<net.runelite.http.api.worlds.World> worlds = worldResult.getWorlds();

        int worldIdx = worlds.indexOf(currentWorld);
        int totalLevel = client.getTotalLevel();

        net.runelite.http.api.worlds.World world;
        do
        {
            if (random)
            {
                worldIdx = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, worlds.size());
            }
            else
            {
                worldIdx++;

                if (worldIdx >= worlds.size())
                {
                    worldIdx = 0;
                }
            }

            world = worlds.get(worldIdx);

            EnumSet<net.runelite.http.api.worlds.WorldType> types = world.getTypes().clone();

            types.remove(net.runelite.http.api.worlds.WorldType.BOUNTY);
            // Treat LMS world like casual world
            types.remove(net.runelite.http.api.worlds.WorldType.LAST_MAN_STANDING);

            if (types.contains(net.runelite.http.api.worlds.WorldType.SKILL_TOTAL))
            {
                try
                {
                    int totalRequirement = Integer.parseInt(world.getActivity().substring(0, world.getActivity().indexOf(" ")));

                    if (totalLevel >= totalRequirement)
                    {
                        types.remove(net.runelite.http.api.worlds.WorldType.SKILL_TOTAL);
                    }
                }
                catch (NumberFormatException ex)
                {
                    log.warn("Failed to parse total level requirement for target world", ex);
                }
            }

            // Avoid switching to near-max population worlds, as it will refuse to allow the hop if the world is full
            if (world.getPlayers() >= MAX_PLAYER_COUNT)
            {
                continue;
            }

            // Break out if we've found a good world to hop to
            if (currentWorldTypes.equals(types))
            {
                break;
            }
        }
        while (world != currentWorld);

        if (world == currentWorld)
        {
            String chatMessage = new ChatMessageBuilder()
                    .append(ChatColorType.NORMAL)
                    .append("Couldn't find a world to quick-hop to.")
                    .build();

            chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.CONSOLE)
                    .runeLiteFormattedMessage(chatMessage)
                    .build());
        }
        else
        {
            log.info("found world -> " + world.getId());

            final int worldId = world.getId();

            clientThread.invoke(() -> hop(worldId));
        }
    }


    private void hop(int worldId)
    {
        assert client.isClientThread();

        WorldResult worldResult = worldService.getWorlds();
        // Don't try to hop if the world doesn't exist
        net.runelite.http.api.worlds.World world = worldResult.findWorld(worldId);
        if (world == null)
        {
            return;
        }

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        log.info("changing to new world");

        clientThread.invoke(() -> client.changeWorld(rsWorld));

        log.info("invoked world hop");
        if (client.getGameState() == GameState.LOGIN_SCREEN)
        {
            // on the login screen we can just change the world by ourselves
            return;
        }

        if (config.showWorldHopMessage())
        {
            String chatMessage = new ChatMessageBuilder()
                    .append(ChatColorType.NORMAL)
                    .append("Quick-hopping to World ")
                    .append(ChatColorType.HIGHLIGHT)
                    .append(Integer.toString(world.getId()))
                    .append(ChatColorType.NORMAL)
                    .append("..")
                    .build();

            chatMessageManager
                    .queue(QueuedMessage.builder()
                            .type(ChatMessageType.CONSOLE)
                            .runeLiteFormattedMessage(chatMessage)
                            .build());
        }

    }
}
