package com.melardev.xeytanj.gui;

import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.models.Client;

public interface IGuiUserOwned<T> extends IGui<T> {
    Client getClient();

    void setClient(Client client);

    ServiceType getServiceType();

    @Override
    default void notifyMediatorOnClose() {
        getMediator().onWindowUserOwnedClose(getClient(), this, getServiceType());
    }

    void setStatus(String status);

    void disableUi();
}
