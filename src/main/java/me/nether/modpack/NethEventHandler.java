package me.nether.modpack;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NethEventHandler {

    public static final String login = "http://nethersoul.altervista.org/skin_mod/login2.php?u=%s";

    public static final Map<EntityPlayer, Boolean[]> playerLoginMap = Maps.newHashMap();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(LoginUtil.MODID)) {
            ConfigManager.sync(LoginUtil.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    @Subscribe
    @EventHandler
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(Minecraft.getMinecraft().isSingleplayer()) return;

        try {
            JsonObject response = getLoginResponse(event.player.getName());
            boolean registered = response.get("registered").getAsBoolean();

            event.player.setGameType(GameType.SPECTATOR);

            event.player.sendMessage(new TextComponentString("[LoginUtil]: \247b" + (registered ? "Login with /login password" : "Register with /register password password")));

            updateMap(event.player, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    @Subscribe
    @EventHandler
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(Minecraft.getMinecraft().isSingleplayer()) return;

        try {
            updateMap(event.player, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JsonObject getLoginResponse(String user) {
        try {
            URL url = new URL(String.format(login, user));
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonObject response = jp.parse(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void updateMap(EntityPlayer player, boolean join) {
        if (!join && playerLoginMap.containsKey(player)) {
            playerLoginMap.remove(player);
        }
        if (join && !playerLoginMap.containsKey(player)) {
            playerLoginMap.put(player, new Boolean[]{false, false});
        }
    }


    @SubscribeEvent
    @Subscribe
    @EventHandler
    public void onTick(TickEvent.PlayerTickEvent event) {
        if(Minecraft.getMinecraft().isSingleplayer()) return;

        try {
            boolean needsLogin = playerLoginMap.get(event.player)[0];
            boolean alreadyLogged = playerLoginMap.get(event.player)[1];

            if (event.side == Side.SERVER && !needsLogin) {
                event.player.setGameType(GameType.SPECTATOR);
            } else if (event.side == Side.SERVER && !alreadyLogged) {
                event.player.setGameType(GameType.SURVIVAL);
                playerLoginMap.replace(event.player, new Boolean[]{true, true});
            }

            if (FMLCommonHandler.instance().getSide().isClient() && event.side == Side.CLIENT && !needsLogin) {
                event.player.posX = event.player.prevPosX;
                event.player.posY = event.player.prevPosY;
                event.player.posZ = event.player.prevPosZ;
                event.player.motionX = 0;
                event.player.motionY = 0;
                event.player.motionZ = 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}