package com.melardev.xeytanj.gui.desktop;

import com.melardev.xeytanj.enums.NetworkProtocol;
import com.melardev.xeytanj.gui.IUiListener;
import com.melardev.xeytanj.models.Client;

public interface RdpUiListener extends IUiListener {
    void onRdpPlayBtnClicked(Client id, NetworkProtocol networkProtocol, String displayName, int scaleX, int scaleY, int delay);

    void onRdpPauseBtnClicked(Client id);

    void onRdpStopBtnClicked(Client client);
}
