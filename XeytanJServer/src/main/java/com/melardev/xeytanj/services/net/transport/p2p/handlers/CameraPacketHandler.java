package com.melardev.xeytanj.services.net.transport.p2p.handlers;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.Packet;
import com.melardev.xeytanj.net.packets.multimedia.PacketCameraConfigResponse;
import com.melardev.xeytanj.net.packets.multimedia.PacketMediaResponse;
import com.melardev.xeytanj.net.packets.voice.AbstractMediaPacketFile;
import com.melardev.xeytanj.services.net.RemoteCameraPacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;

import java.util.concurrent.ArrayBlockingQueue;

public class CameraPacketHandler implements RemoteCameraPacketHandler {

    // TODO: move some P2PClientService code here

    private P2PClientService netClientService;
    private ArrayBlockingQueue<AbstractMediaPacketFile> filesRecord;


    public CameraPacketHandler(P2PClientService clientService) {
        this.netClientService = clientService;
        filesRecord = new ArrayBlockingQueue(10);
    }


    private void monitorRemainingFiles() {
        while (true) {
            try {
                AbstractMediaPacketFile p = filesRecord.take();

                netClientService.onAudioFileReceived(p.data);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    @Override
    public ServiceType getServiceType() {
        return ServiceType.CAMERA;
    }


    @Override
    public void handlePacket(Packet packet) {
        if (packet.getClass() == PacketMediaResponse.class) {
            PacketMediaResponse packetCamera = (PacketMediaResponse) packet;
            netClientService.onCameraImagePacketReceived(packetCamera.getImage());

        } else if (packet.getClass() == PacketCameraConfigResponse.class) {
            PacketCameraConfigResponse packetCamera = (PacketCameraConfigResponse) packet;
            netClientService.onCameraConfigInfoReceived(packetCamera.getScreenDeviceInfoList());
        }

        /*else if (packet.getClass() == PacketVoice.class)
            netClientService.onCameraAudioPacketReceived(((PacketVoice) packet).data, ((PacketVoice) packet).bytesRead);
*/
        else if (packet.getClass() == AbstractMediaPacketFile.class) {
            if (filesRecord.size() < 10) {
                filesRecord.offer((AbstractMediaPacketFile) packet);
            } else {
                filesRecord.poll();
                filesRecord.offer((AbstractMediaPacketFile) packet);
            }
        }
    }


}
