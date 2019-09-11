package com.melardev.xeytanj.services.net.transport.p2p.handlers;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.net.packets.filesystem.PacketFileExplorer;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;

public class FilePacketHandler implements PacketHandler<PacketFileExplorer> {
    private final P2PClientService netClientService;

    public FilePacketHandler(P2PClientService netClientService) {
        this.netClientService = netClientService;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.FILE_SYSTEM;
    }

    public void handlePacket(PacketFileExplorer packet) {
        switch (packet.getAction()) {
            case LIST_ROOTS:
                netClientService.onFilesystemRootInfoReceived(packet.getFileInfoStructures());
                break;
            case LIST_FILES:
                netClientService.onFileSystemInfoUpdate(packet.getFileInfoStructures());
                break;
            default:
                break;
        }
    }


}
