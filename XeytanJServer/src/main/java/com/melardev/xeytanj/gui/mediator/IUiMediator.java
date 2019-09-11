package com.melardev.xeytanj.gui.mediator;

import com.melardev.xeytanj.IApplication;
import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.IGui;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.builder.BuilderDialogListener;
import com.melardev.xeytanj.gui.camera.CameraUiListener;
import com.melardev.xeytanj.gui.chat.ChatUiListener;
import com.melardev.xeytanj.gui.desktop.RdpUiListener;
import com.melardev.xeytanj.gui.disclaimer.DisclaimerUiListener;
import com.melardev.xeytanj.gui.filesystem.FileSystemUiListener;
import com.melardev.xeytanj.gui.installer.InstallerUiListener;
import com.melardev.xeytanj.gui.language.LanguageUiListener;
import com.melardev.xeytanj.gui.main.MainUiListener;
import com.melardev.xeytanj.gui.process.ProcessListUiListener;
import com.melardev.xeytanj.gui.shell.ShellUiListener;
import com.melardev.xeytanj.maps.MapUiListener;
import com.melardev.xeytanj.models.*;
import com.melardev.xeytanj.services.IAppMessageProvider;
import com.melardev.xeytanj.services.IService;
import com.melardev.xeytanj.services.logger.ILogger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IUiMediator extends MainUiListener, CameraUiListener, RdpUiListener, DisclaimerUiListener, FileSystemUiListener,
        ProcessListUiListener, ShellUiListener, InstallerUiListener, LanguageUiListener, ChatUiListener, BuilderDialogListener,
        MapUiListener,
        IService {

    void setMessageProvider(IAppMessageProvider messageProvider);

    void showLanguageSelectorDialog();

    void showDisclaimerDialog();

    void showInstaller();

    void showDialogMessageError(String message);

    void showMainFrame(MainUiListener listener);

    // void showClientInfo(Client info, ClientInfoUiListener listener);

    void addClientRowTable(Client client);


    void onClientDisconnected(Client client);

    void showBuilderWindow(String defaultClientPath);

    void setApplication(IApplication application);

    void updateRemoteDesktopWindow(Client client, ImageIcon image);

    void showOrUpdateRemoteDesktopConfigInfo(Client client, List<ScreenDeviceInfo> screenDeviceInfoList);

    void showOrUpdateRemoteCameraUi(Client client, List<CameraDeviceInfo> cameraIds);

    void showOrUpdateRemoteCameraUi(Client client, Icon image);

    void showRoots(Client client, FileInfoStructure[] fileInfoStructures);

    void closeAllUserOwnedWindows(Client client);

    void closeRemoteDesktopWindow(Client client);

    <T> void onWindowClose(IGui gui);

    <T> void onWindowUserOwnedClose(Client client, IGuiUserOwned gui, ServiceType serviceType);
    // void onWindowUserOwnedClose(UUID id, IGuiUserOwned gui, ServiceType serviceType);

    void updateProcessInfoWindow(Client client, List<ProcessStructure> processStructures);

    void showShellWindow(Client infoStructure);

    void appendShellOutput(Client client, String text);

    void notifyShellWindowDisconnectedForClient(Client client);

    void notifyCameraSessionClosedForClient(Client client);

    void showFileExplorerWindow(Client client);

    void updateFileSystemView(Client client, FileInfoStructure[] fileInfoStructures);

    void playAudioFromCamera(Client client, byte[] data, int bytesRead);

    void playAudioFile(Client client, byte[] data);

    void showChatMessage(Client client, String message);

    void showChatWindow(Client client);

    void disableChat(Client client);

    void closeBuilderUi();

    void setLogger(ILogger logger);

    void showErrorMessage(String message);

    void showErrorMessage(String title, String message);

    void showMapRequested();

    void showMap(String googleKey, ArrayList<Client> locations);

    void showClientInfo(Client client, Map<String, String> env, Map<String, String> properties);


}
