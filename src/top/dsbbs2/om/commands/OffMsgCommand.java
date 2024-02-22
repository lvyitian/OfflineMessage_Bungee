package top.dsbbs2.om.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import top.dsbbs2.om.Main;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class OffMsgCommand extends Command {
    public static final OffMsgCommand INSTANCE=new OffMsgCommand();
    private OffMsgCommand(){super("offmsg","offlinemessage.offmsg");if(INSTANCE!=null) throw new IllegalStateException();}

    @Override
    public void execute(CommandSender sender, String[] args) {
        UUID senderUUID;
        if(sender instanceof ProxiedPlayer) senderUUID=((ProxiedPlayer) sender).getUniqueId();
        else senderUUID=UUID.nameUUIDFromBytes(sender.getName().getBytes(StandardCharsets.UTF_8));
        if(args.length<1) {sender.sendMessage(TextComponent.fromLegacyText("缺少必要参数receiver！用法：/offmsg <receiver> <msg>"));return;}
        if(args.length<2) {sender.sendMessage(TextComponent.fromLegacyText("缺少必要参数msg！用法：/offmsg <receiver> <msg>"));return;}
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=1;i<args.length;i++)
        {
            stringBuilder.append(args[i]);
            if(i+1<args.length) stringBuilder.append(' ');
        }
        String receiverName=args[0];
        ProxiedPlayer receiver=ProxyServer.getInstance().getPlayer(receiverName);
        if(receiver==null) {
            UUID receiverUUID = Main.instance.storage.getConfig().nameToUUID.get(receiverName);
            if (receiverUUID == null) {
                sender.sendMessage(TextComponent.fromLegacyText(receiverName + "从未加入过服务器！"));
                return;
            }
            Main.instance.storage.getConfig().addToMessages(receiverUUID, sender.getName(), senderUUID, stringBuilder.toString());
        }else receiver.sendMessage(senderUUID,TextComponent.fromLegacyText(sender.getName()+"给你发来一条消息: "+stringBuilder));
    }
}
