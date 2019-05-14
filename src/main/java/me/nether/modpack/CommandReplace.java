package me.nether.modpack;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CommandReplace implements IClientCommand {

    private static final String raw_url = "http://nethersoul.altervista.org/skin_mod/login2.php?u=%s&auth=%s&replace=%s";

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "replace";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /replace oldPassword newPassword";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String name = sender.getName();

        if (args.length < 2) {
            sender.sendMessage(new TextComponentString("[LoginUtil]: \247b" + this.getUsage(sender)));
            return;
        }

        JsonObject response = getLoginResponse(name, args[0], args[1]);

        sender.sendMessage(new TextComponentString("[LoginUtil]: \247b" + response.get("message").getAsString()));
    }

    private JsonObject getLoginResponse(String user, String oldPw, String newPw) {
        try {
            String oldH = sha1(oldPw);
            String newH = sha1(newPw);

            URL url = new URL(String.format(raw_url, user, oldH, newH));
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

    private String sha1(String text) {
        return Hashing.sha1().hashString(text, StandardCharsets.UTF_8).toString();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

}
