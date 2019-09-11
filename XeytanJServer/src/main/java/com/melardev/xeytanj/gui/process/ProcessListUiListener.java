package com.melardev.xeytanj.gui.process;

import com.melardev.xeytanj.models.Client;

public interface ProcessListUiListener {
    void onProcessKillRequest(Client client, int pid);
}
