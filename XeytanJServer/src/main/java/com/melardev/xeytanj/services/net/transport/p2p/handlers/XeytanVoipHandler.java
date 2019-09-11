package com.melardev.xeytanj.services.net.transport.p2p.handlers;


import com.melardev.xeytanj.enums.MediaState;
import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;
import com.melardev.xeytanj.services.net.RemoteMediaPacketHandler;

import java.net.Socket;

public class XeytanVoipHandler implements RemoteMediaPacketHandler<PacketVoice> {

    //    private SoundPlayer gui;
    private PacketVoice.Mode dataType;
    private Socket socket;
    private MediaState mediaState;

    public XeytanVoipHandler() {

    }

    public void initService() {
       /* Packet packet;
        try {
            while (true) {
                ObjectInputStream sockIn = new ObjectInputStream(socket.getInputStream());
                packet = (PacketVoice) sockIn.readObject();
                if (packet.get() == PacketVoice.DataType.FILE) {
                    while (!gui.completed)
                        continue;
                    if (com.melardev.xeytanj.net.packet instanceof AbstractMediaPacketFile)
                        gui.playFile(((AbstractMediaPacketFile) com.melardev.xeytanj.net.packet).data);
                } else if (protocol == PacketVoice.DataType.RAW) {
                    if (com.melardev.xeytanj.net.packet instanceof PacketVoice) {
                        PacketVoice pv = (PacketVoice) com.melardev.xeytanj.net.packet;
                        if (Arrays.equals(pv.data, pv.dataS.getBytes())) {
                            System.out.println("not the same");
                        }
                        gui.playRaw(pv.data, pv.bytesRead);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/
    }

    @Override
    public ServiceType getServiceType() {
        return null;
    }

    @Override
    public void handlePacket(PacketVoice packet) {
        packet.getData();
    }
}
