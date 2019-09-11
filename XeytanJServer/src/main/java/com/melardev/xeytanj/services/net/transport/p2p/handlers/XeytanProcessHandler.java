package com.melardev.xeytanj.services.net.transport.p2p.handlers;

import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.models.ProcessStructure;
import com.melardev.xeytanj.net.packets.process.PacketProcess;
import com.melardev.xeytanj.net.packets.process.PacketProcessList;
import com.melardev.xeytanj.services.net.PacketHandler;
import com.melardev.xeytanj.services.net.transport.p2p.P2PClientService;

import java.util.List;

public class XeytanProcessHandler implements PacketHandler<PacketProcess> {

    private static final boolean DEBUG = false;

    private P2PClientService netClientService;


    public XeytanProcessHandler(P2PClientService netClientService) {
        this.netClientService = netClientService;
    }


    @Override
    public ServiceType getServiceType() {
        return ServiceType.LIST_PROCESS;
    }


    @Override
    public void handlePacket(PacketProcess packet) {
        if (packet.getClass() == PacketProcessList.class) {
            List<ProcessStructure> info = ((PacketProcessList) packet).getProcessStructures();

            netClientService.onProcessInfoReceived(info);

                /*
                ArrayList<ArrayList<String>> rows;
                if (packet.format == PacketProcess.Format.CSV) {
                    rows = parseCSVData(packet.result);
                    gui.populateTable(rows, packet.icons);
                } else if (packet.format == PacketProcess.Format.LIST) {
                    rows = parseListData(packet);
                    gui.populateTableList(rows, packet.icons);
                }
                */
        }
    }

}
