package com.melardev.xeytanj.gui.init;


import com.melardev.xeytanj.enums.DbType;
import com.melardev.xeytanj.gui.installer.InstallerUiListener;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;




public class Installer extends JDialog implements ActionListener {
    private JTextField txtHost;
    private JTextField txtUserName;
    private JTextField txtPort;
    private JTextField textMapkey;
    private JCheckBox chckbxMysql;
    private JButton btnOk;
    private JButton btnHelp;
    private JButton btnTestConnection;
    private JLabel lblPassword;
    private JTextField txtPasswd;
    private List<InstallerUiListener> listeners;


    public Installer() {
        setBackground(Color.BLACK);
        setType(Type.UTILITY);
        setTitle("Settings");
        setResizable(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 73, 103, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 33, 0, 0, 42, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        chckbxMysql = new JCheckBox("MySQL");
        chckbxMysql.addActionListener(this);
        GridBagConstraints gbc_chckbxMysql = new GridBagConstraints();
        gbc_chckbxMysql.gridwidth = 2;
        gbc_chckbxMysql.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxMysql.gridx = 1;
        gbc_chckbxMysql.gridy = 1;
        getContentPane().add(chckbxMysql, gbc_chckbxMysql);

        btnHelp = new JButton("Help");
        btnHelp.addActionListener(this);
        GridBagConstraints gbc_btnHelp = new GridBagConstraints();
        gbc_btnHelp.insets = new Insets(0, 0, 5, 0);
        gbc_btnHelp.gridx = 4;
        gbc_btnHelp.gridy = 1;
        getContentPane().add(btnHelp, gbc_btnHelp);

        JLabel lblHost = new JLabel("host");
        lblHost.setEnabled(false);
        GridBagConstraints gbc_lblHost = new GridBagConstraints();
        gbc_lblHost.gridwidth = 2;
        gbc_lblHost.insets = new Insets(0, 0, 5, 5);
        gbc_lblHost.gridx = 1;
        gbc_lblHost.gridy = 2;
        getContentPane().add(lblHost, gbc_lblHost);

        txtHost = new JTextField();
        txtHost.setEditable(false);
        txtHost.setEnabled(false);
        GridBagConstraints gbc_txtHost = new GridBagConstraints();
        gbc_txtHost.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtHost.insets = new Insets(0, 0, 5, 5);
        gbc_txtHost.gridx = 3;
        gbc_txtHost.gridy = 2;
        getContentPane().add(txtHost, gbc_txtHost);
        txtHost.setColumns(10);

        JLabel lblUserName = new JLabel("username");
        lblUserName.setEnabled(false);
        GridBagConstraints gbc_lblUserName = new GridBagConstraints();
        gbc_lblUserName.gridwidth = 2;
        gbc_lblUserName.insets = new Insets(0, 0, 5, 5);
        gbc_lblUserName.gridx = 1;
        gbc_lblUserName.gridy = 3;
        getContentPane().add(lblUserName, gbc_lblUserName);

        txtUserName = new JTextField();
        txtUserName.setEnabled(false);
        txtUserName.setEditable(false);
        GridBagConstraints gbc_txtUserName = new GridBagConstraints();
        gbc_txtUserName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtUserName.insets = new Insets(0, 0, 5, 5);
        gbc_txtUserName.gridx = 3;
        gbc_txtUserName.gridy = 3;
        getContentPane().add(txtUserName, gbc_txtUserName);
        txtUserName.setColumns(10);

        btnTestConnection = new JButton("Test Connection");
        btnTestConnection.addActionListener(this);

        lblPassword = new JLabel("Password");
        lblPassword.setEnabled(false);
        GridBagConstraints gbc_lblPassword = new GridBagConstraints();
        gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
        gbc_lblPassword.gridx = 2;
        gbc_lblPassword.gridy = 4;
        getContentPane().add(lblPassword, gbc_lblPassword);

        txtPasswd = new JTextField();
        txtPasswd.setEnabled(false);
        txtPasswd.setEditable(false);
        GridBagConstraints gbc_txtPasswd = new GridBagConstraints();
        gbc_txtPasswd.insets = new Insets(0, 0, 5, 5);
        gbc_txtPasswd.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtPasswd.gridx = 3;
        gbc_txtPasswd.gridy = 4;
        getContentPane().add(txtPasswd, gbc_txtPasswd);
        txtPasswd.setColumns(10);

        JLabel lblPort = new JLabel("port");
        lblPort.setEnabled(false);
        GridBagConstraints gbc_lblPort = new GridBagConstraints();
        gbc_lblPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblPort.gridx = 2;
        gbc_lblPort.gridy = 5;
        getContentPane().add(lblPort, gbc_lblPort);

        txtPort = new JTextField();
        txtPort.setEditable(false);
        txtPort.setEnabled(false);
        GridBagConstraints gbc_txtPort = new GridBagConstraints();
        gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtPort.insets = new Insets(0, 0, 5, 5);
        gbc_txtPort.gridx = 3;
        gbc_txtPort.gridy = 5;
        getContentPane().add(txtPort, gbc_txtPort);
        txtPort.setColumns(10);
        GridBagConstraints gbc_btnTestConnection = new GridBagConstraints();
        gbc_btnTestConnection.insets = new Insets(0, 0, 5, 5);
        gbc_btnTestConnection.gridx = 3;
        gbc_btnTestConnection.gridy = 6;
        getContentPane().add(btnTestConnection, gbc_btnTestConnection);

        JLabel lblGoogleMapsKey = new JLabel("Google Maps Key");
        GridBagConstraints gbc_lblGoogleMapsKey = new GridBagConstraints();
        gbc_lblGoogleMapsKey.insets = new Insets(0, 0, 5, 5);
        gbc_lblGoogleMapsKey.gridx = 2;
        gbc_lblGoogleMapsKey.gridy = 7;
        getContentPane().add(lblGoogleMapsKey, gbc_lblGoogleMapsKey);

        textMapkey = new JTextField();
        GridBagConstraints gbc_textMapkey = new GridBagConstraints();
        gbc_textMapkey.insets = new Insets(0, 0, 5, 5);
        gbc_textMapkey.fill = GridBagConstraints.HORIZONTAL;
        gbc_textMapkey.gridx = 3;
        gbc_textMapkey.gridy = 7;
        getContentPane().add(textMapkey, gbc_textMapkey);
        textMapkey.setColumns(10);

        btnOk = new JButton("Ok");
        btnOk.addActionListener(this);
        GridBagConstraints gbc_btnOk = new GridBagConstraints();
        gbc_btnOk.insets = new Insets(0, 0, 0, 5);
        gbc_btnOk.gridx = 3;
        gbc_btnOk.gridy = 8;
        getContentPane().add(btnOk, gbc_btnOk);

        setSize(350, 300);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }


    public static void main(String[] args) {
        new Installer().setVisible(true);
    }

    private void onSubmitForm() {
       /* if (!textMapkey.getText().isEmpty())
            Loader.setProperty(GOOGLE_KEY, textMapkey.getText());

        if (!chckbxMysql.isSelected()) {

            int ret = JOptionPane.showConfirmDialog(this,
                    "Connection to MySQL Server Failed\nDo you want to skip this process?");
            if (ret != JOptionPane.YES_OPTION)
                return;

            Loader.setProperty(PREF_USE_MYSQL, XEYTAN_TRUE);
            Loader.setProperty(MYSQL_HOST, txtHost.getText());
            Loader.setProperty(MYSQL_USER, txtUserName.getText());
            Loader.setProperty(MYSQL_PASSWORD, txtPasswd.getText());
            Loader.setProperty(MYSQL_PORT, txtPort.getText());
        }

        dispose();
*/
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        DbType dbTYpe;

        if (evt.getSource() == chckbxMysql) {

            if (chckbxMysql.isSelected()) {

                txtHost.setEnabled(true);
                txtUserName.setEnabled(true);
                txtPort.setEnabled(true);

                txtHost.setEditable(true);
                txtUserName.setEditable(true);
                txtPort.setEditable(true);
            } else {

                txtHost.setEnabled(false);
                txtUserName.setEnabled(false);
                txtPort.setEnabled(false);

                txtHost.setEditable(false);
                txtUserName.setEditable(false);
                txtPort.setEditable(false);
            }
        } else if (evt.getSource() == btnTestConnection) {
            dbTYpe = DbType.MYSQL;
            onDataSourceTestConnection(dbTYpe, txtHost.getText(), Long.valueOf(txtPort.getText()), txtUserName.getText()
                    , txtPasswd.getText());

        } else if (evt.getSource() == btnHelp) {
            JOptionPane.showMessageDialog(this,
                    "All fields are optional and can be changed latter in Edit Preferences");
        } else if (evt.getSource() == btnOk) {
            onSubmitForm();
        }


    }

    public Installer addListener(InstallerUiListener listener) {
        this.listeners.add(listener);
        return this;
    }

    private void onDataSourceTestConnection(DbType dbType, String host, Long port, String username, String password) {
        listeners.forEach(l -> l.onTestConnection(dbType, host, port, username, password));
    }

    public void display() {
        setVisible(true);
    }


}
