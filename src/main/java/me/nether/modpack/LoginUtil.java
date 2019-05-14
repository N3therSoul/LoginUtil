package me.nether.modpack;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = LoginUtil.MODID, name = LoginUtil.NAME, version = LoginUtil.VERSION)
public class LoginUtil {
    public static final String MODID = "loginutil";
    public static final String NAME = "Login Util";
    public static final String VERSION = "1.3";

    private static Logger logger;

    private static NethEventHandler handler = new NethEventHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        logger.log(Level.INFO, "Preinit...");

        MinecraftForge.EVENT_BUS.register(handler);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            if(FMLCommonHandler.instance().getSide().isServer()) return;
            logger.log(Level.INFO, "Initializing...");

            if (event.getSide() == Side.CLIENT && ClientCommandHandler.instance != null) {
                //Adding the commands to the game
                ClientCommandHandler.instance.getCommands().put("login", new CommandLogin());
                ClientCommandHandler.instance.getCommands().put("replace", new CommandReplace());
                ClientCommandHandler.instance.getCommands().put("register", new CommandRegister());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
