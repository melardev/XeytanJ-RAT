package com.melardev.xeytanj.gui.chat;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.services.IAppMessageProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatGUI extends JFrame implements IGuiUserOwned<ChatUiListener> {

    private JTextArea txtMsg;
    private JTextArea txtChat;
    private ChatUiListener listener;
    private IUiMediator mediator;
    private IAppMessageProvider messageProvider;
    private JButton btnSend;
    private Client client;


    public ChatGUI() {
    }

    private void onSendTriggered() {
        String msg = txtMsg.getText().trim();
        appendMsg("Me: " + msg + "\n");
        txtMsg.setText("");
        getMediator().onSendChatMessage(getClient(), msg);

    }

    public void appendMsg(String message) {
        if (!message.endsWith("\n"))
            txtChat.append(message + "\n");
        else
            txtChat.append(message);
    }

    @Override
    public Client getClient() {
        return null;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
        setTitle("[Master] " + client.getPcName());
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CHAT_SERVICE;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public void disableUi() {
        txtChat.setEnabled(false);
        txtMsg.setEnabled(false);
        txtChat.append("Disconnected");
        btnSend.setEnabled(false);
    }

    @Override
    public void display() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                notifyMediatorOnClose();
            }
        });


        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{53, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{7, 611, 28, 63, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        txtChat = new JTextArea();
        GridBagConstraints gbc_txtChat = new GridBagConstraints();
        gbc_txtChat.insets = new Insets(0, 0, 5, 5);
        gbc_txtChat.fill = GridBagConstraints.BOTH;
        gbc_txtChat.gridx = 1;
        gbc_txtChat.gridy = 1;

        JScrollPane scrollChat = new JScrollPane(txtChat);
        getContentPane().add(scrollChat, gbc_txtChat);

        txtMsg = new JTextArea();
        GridBagConstraints gbc_txtMsg = new GridBagConstraints();
        gbc_txtMsg.insets = new Insets(0, 0, 5, 5);
        gbc_txtMsg.fill = GridBagConstraints.BOTH;
        gbc_txtMsg.gridx = 1;
        gbc_txtMsg.gridy = 3;

        JScrollPane scrollMsg = new JScrollPane(txtMsg);
        txtMsg.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    onSendTriggered();

            }

        });
        getContentPane().add(scrollMsg, gbc_txtMsg);

        btnSend = new JButton("Send");
        GridBagConstraints gbc_btnSend = new GridBagConstraints();
        gbc_btnSend.insets = new Insets(0, 0, 5, 5);
        gbc_btnSend.gridx = 2;
        gbc_btnSend.gridy = 3;
        getContentPane().add(btnSend, gbc_btnSend);
        btnSend.addActionListener((ActionEvent e) -> {
            onSendTriggered();
        });
        setSize(1204, 706);
        setResizable(false);
        setTitle("Master");
        setVisible(true);
    }

    @Override
    public void addListener(ChatUiListener listener) {
        this.listener = listener;
    }

    @Override
    public ChatUiListener getListener() {
        return this.listener;
    }

    @Override
    public IUiMediator getMediator() {
        return mediator;
    }

    @Override
    public void setMediator(IUiMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void resetState() {

    }

    @Override
    public void setMessageProvider(IAppMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

}
