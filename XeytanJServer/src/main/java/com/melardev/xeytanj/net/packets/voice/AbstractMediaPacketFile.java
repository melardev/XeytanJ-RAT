package com.melardev.xeytanj.net.packets.voice;

import com.melardev.xeytanj.net.packets.Packet;
import com.melardev.xeytanj.net.packets.PacketType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AbstractMediaPacketFile extends Packet {

    public byte[] data;

    public AbstractMediaPacketFile(byte[] _data) {
        super(PacketType.FILE);
        data = _data;
    }

    public AbstractMediaPacketFile(File f) {
        super(PacketType.FILE);
        if (f.exists()) {
            try {
                data = Files.readAllBytes(f.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
