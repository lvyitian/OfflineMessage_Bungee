package top.dsbbs2.om.storage;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import top.dsbbs2.om.Main;

import java.text.DateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MessageStorage {
    public volatile ConcurrentMap<UUID, Set<Message>> messages=new ConcurrentHashMap<>();
    public static class Message implements Comparable<Message> {
        public String name;
        public UUID uuid;
        public String message;
        public long timestamp=System.currentTimeMillis();

        public Message(String name, UUID uuid, String message) {
            this.name = name;
            this.uuid = uuid;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Message)) return false;
            Message message1 = (Message) o;
            return timestamp == message1.timestamp && Objects.equals(name, message1.name) && Objects.equals(uuid, message1.uuid) && Objects.equals(message, message1.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, uuid, message, timestamp);
        }

        @Override
        public String toString() {
            return "Message{" +
                    "name='" + name + '\'' +
                    ", uuid=" + uuid +
                    ", message='" + message + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }

        @Override
        public int compareTo(Message o) {
            return (int) (this.timestamp-o.timestamp);
        }
        public void sendToPlayer(ProxiedPlayer player){
            player.sendMessage(this.uuid, TextComponent.fromLegacyText(this.name+"于"+DateFormat.getDateTimeInstance().format(Date.from(Instant.ofEpochMilli(this.timestamp)))+"向你发送了一条消息: "+this.message));
        }
    }
    public void addToMessages(UUID receiver,String sender_name,UUID sender,String message){
        Set<Message> set=this.messages.computeIfAbsent(receiver,ignored->new LinkedHashSet<>());
        set.add(new Message(sender_name,sender,message));
        this.messages.put(receiver,set);
        try {
            Main.instance.storage.saveConfig();
        }catch(Throwable t){throw new RuntimeException(t);}
    }
    public void sendOfflineMessages(ProxiedPlayer player){
        Set<Message> msg=this.messages.get(player.getUniqueId());
        if(msg!=null&&!msg.isEmpty()){
            msg.forEach(i->i.sendToPlayer(player));
            msg.clear();
            try{
                Main.instance.storage.saveConfig();
            }catch(Throwable t){throw new RuntimeException(t);}
        }
    }
    public volatile ConcurrentMap<String,UUID> nameToUUID=new ConcurrentHashMap<>();
    public void storeUUID(ProxiedPlayer player){
        UUID oldVal=this.nameToUUID.put(player.getName(),player.getUniqueId());
        if(oldVal==null||!Objects.equals(oldVal,player.getUniqueId())){
            try {
                Main.instance.storage.saveConfig();
            }catch(Throwable t){throw new RuntimeException(t);}
        }
    }
}
