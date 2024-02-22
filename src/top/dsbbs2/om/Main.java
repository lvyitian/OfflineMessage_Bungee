package top.dsbbs2.om;

import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import top.dsbbs2.common.config.IConfig;
import top.dsbbs2.common.config.SimpleConfig;
import top.dsbbs2.om.commands.OffMsgCommand;
import top.dsbbs2.om.storage.MessageStorage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.UUID;

public class Main extends Plugin implements Listener {
    public volatile IConfig<MessageStorage> storage=new SimpleConfig<MessageStorage>(new File(this.getDataFolder(),"storage.json").getAbsolutePath(),"UTF8",MessageStorage.class){{try{this.loadConfig();}catch(Throwable t){throw new RuntimeException(t);}}};
    public static volatile Main instance;
    {
        instance=this;
    }
    @Override
    public void onEnable(){
        ProxyServer.getInstance().registerChannel("offlinemessage");
        ProxyServer.getInstance().getPluginManager().registerListener(this,this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, OffMsgCommand.INSTANCE);
    }
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws Throwable {
        if(e.getSender() instanceof ServerConnection && e.getReceiver() instanceof ProxiedPlayer){
            if(e.getTag().equals("offlinemessage")){
                try(ByteArrayInputStream bai=new ByteArrayInputStream(e.getData())){
                    try(DataInputStream dis=new DataInputStream(bai)){
                        UUID receiver=UUID.fromString(dis.readUTF());
                        boolean isOnline=ProxyServer.getInstance().getPlayer(receiver)!=null;
                        String msg=dis.readUTF();
                        ProxiedPlayer sender=(ProxiedPlayer)e.getReceiver();
                        if(!isOnline)
                            this.storage.getConfig().addToMessages(receiver,sender.getName(),sender.getUniqueId(),msg);
                        else ProxyServer.getInstance().getPlayer(receiver).sendMessage(sender.getUniqueId(),TextComponent.fromLegacyText(sender.getName()+"给你发来一条消息: "+msg));
                    }
                }
            }
        }
    }
  @EventHandler
  public void onPermissionCheck(PermissionCheckEvent e){
        if(!e.getSender().getPermissions().contains("offlinemessage.offmsg")){
            e.getSender().setPermission("offlinemessage.offmsg",true);
            e.setHasPermission(true);
        }
  }
  @EventHandler
  public void onPlayerLogin(PostLoginEvent e)
  {
      this.storage.getConfig().storeUUID(e.getPlayer());
      this.storage.getConfig().sendOfflineMessages(e.getPlayer());
  }
}
