package com.melardev.xeytanj.services.net.transport.p2p.handlers;

import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.Packet;
import com.melardev.xeytanj.net.packets.multimedia.PacketDesktopConfigResponse;
import com.melardev.xeytanj.net.packets.multimedia.PacketMediaResponse;
import com.melardev.xeytanj.services.net.transport.RemoteDesktopPacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;

import javax.swing.*;

public class DesktopMediaPacketHandler implements RemoteDesktopPacketHandler {
    // TODO: move some code from P2PClientService to this class

    private P2PClientService netClientService;

    public DesktopMediaPacketHandler(P2PClientService netClientService) {
        this.netClientService = netClientService;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.REMOTE_DESKTOP;
    }

    @Override
    public void handlePacket(Packet packet) {
        if (packet.getClass() == PacketDesktopConfigResponse.class) {

            netClientService.onDesktopConfigInfoReceived(
                    ((PacketDesktopConfigResponse) packet).getScreenDeviceInfoList());
        } else if (packet.getClass() == PacketMediaResponse.class) {
            ImageIcon img = null;
            Runtime rt = Runtime.getRuntime();
            if (img != null)
                img.getImage().flush();
            img = null; //Garbage collect the image
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rt.gc();
            img = ((PacketMediaResponse) packet).getImage();
            netClientService.onDesktopImageReceived(img);
        }
    }
}
