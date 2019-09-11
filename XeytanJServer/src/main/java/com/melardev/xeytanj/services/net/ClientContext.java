package com.melardev.xeytanj.services.net;

import java.util.UUID;

public class ClientContext {
    private INetworkClientService netClientService;
    private INetworkServerService netServerService;


    public ClientContext(UUID id) {
    }

    public void setNetClientService(INetworkClientService networkClientService) {
        this.netClientService = networkClientService;
    }

    public INetworkClientService getNetClientService() {
        return netClientService;
    }

    public INetworkServerService getNetServerService() {
        return netServerService;
    }

    public void setNetServerService(INetworkServerService netServerService) {
        this.netServerService = netServerService;
    }
}
