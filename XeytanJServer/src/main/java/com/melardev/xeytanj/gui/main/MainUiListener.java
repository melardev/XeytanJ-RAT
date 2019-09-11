package com.melardev.xeytanj.gui.main;

import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.gui.IUiListener;

import java.util.UUID;

public interface MainUiListener extends IUiListener {
    void onShowClientInfo(UUID installerListener);

    void onRemoteShellRequested(UUID uuid);

    void onProcessListClicked(ProcessInfoDetails infoLevel, UUID uuid);

    void onCameraStartRequested(NetworkProtocol mode, UUID uuid);

    void onRemoteDesktopClicked(UUID uuid);

    void onRemoteFileManagerClicked(UUID uuid);

    void onStartChatRequested(UUID uuid);

    void onTurnOnOffDisplayClicked(UUID uuid, boolean on);

    void onRebootClicked(UUID uuid);

    void onShutdownClicked(UUID uuid);

    void onLogOffSystem(UUID uuid);

    void onLockSystemClicked(UUID uuid);

    void onSessionKeyloggerClicked(UUID uuid);

    void onVoIpRawClicked(UUID uuid);

    void onVoIpClicked(UUID uuid);

    void onBuilderShowClicked();

    void onPreferencesClicked();

    void onCloseConnectionClicked(UUID uuid);

    void onUninstallClicked(UUID uuid);
}
