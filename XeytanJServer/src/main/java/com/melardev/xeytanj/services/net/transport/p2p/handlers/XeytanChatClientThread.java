package com.melardev.xeytanj.services.net.transport.p2p.handlers;

import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.PacketChat;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;


public class XeytanChatClientThread implements PacketHandler<PacketChat> {

    private final P2PClientService netClientService;

    public XeytanChatClientThread(P2PClientService netClientService) {
        this.netClientService = netClientService;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CHAT_SERVICE;
    }


    @Override
    public void handlePacket(PacketChat packet) {
        netClientService.onChatMessageReceived(packet.getMessage());
    }
}
