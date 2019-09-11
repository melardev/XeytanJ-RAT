package com.melardev.xeytanj.gui.installer;

import com.melardev.xeytanj.enums.DbType;

public interface InstallerUiListener {
    void onTestConnection(DbType dbType, String host, Long port, String username, String password);
}
