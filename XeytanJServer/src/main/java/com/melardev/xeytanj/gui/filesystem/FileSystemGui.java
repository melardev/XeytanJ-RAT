package com.melardev.xeytanj.gui.filesystem;


import com.melardev.xeytanj.enums.ServiceType;
import com.melardev.xeytanj.gui.IGuiUserOwned;
import com.melardev.xeytanj.gui.mediator.IUiMediator;
import com.melardev.xeytanj.models.Client;
import com.melardev.xeytanj.models.FileInfoStructure;
import com.melardev.xeytanj.services.IAppMessageProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;

public class FileSystemGui extends JDialog implements IGuiUserOwned<FileSystemUiListener> {


    private JTable explorerTable;
    private JTextField txtPath;
    private JList<FileInfoStructure> listDrive;
    private DefaultListModel<FileInfoStructure> listModel;
    private JMenuItem mntmUpload;
    private JMenuItem mntmOpenFile;
    private PathStack pathTrack;
    private UUID id;
    private FileSystemUiListener listener;
    private IUiMediator mediator;
    private JPopupMenu popupMenu;
    private IAppMessageProvider messageProvider;
    private JScrollPane listPane;
    private JButton btnGo;
    private Client client;

    public FileSystemGui() {
        pathTrack = new PathStack();
        setupUi();
    }

    public void updateFilesystemView(FileInfoStructure[] files) {
        ((NonEditableTableModel) explorerTable.getModel()).setRowCount(0);
        for (FileInfoStructure f : files) {
            if (f.isFile) {
                ((NonEditableTableModel) explorerTable.getModel())
                        .addRow(new Object[]{f, f.fileSize, f.canExecute, f.canRead, f.canWrite});
            } else {
                ((NonEditableTableModel) explorerTable.getModel()).addRow(new Object[]{f, "", "", "", ""});
            }
        }
    }

    protected void onMoveBackHistory() {
        if (!pathTrack.isEmpty()) {
            if (pathTrack.size() > 1)
                pathTrack.pop();
            getMediator().onFileSystemPathRequested(getClient(), pathTrack.toString());
        }
    }


    protected void onExplorerTableClicked() {
        FileInfoStructure toLsPath = (FileInfoStructure) explorerTable.getValueAt(explorerTable.getSelectedRow(), 0);
        if (!toLsPath.isFile) {
            pathTrack.push("/" + toLsPath.getBaseName());
            getMediator().onFileSystemPathRequested(getClient(), toLsPath.getFullPath());
        }
    }

    protected void onDrivesClicked() {
        String fileSystemPath = listDrive.getSelectedValue().fullPath;
        pathTrack.clear();
        pathTrack.push(fileSystemPath);
        getMediator().onFileSystemPathRequested(getClient(), fileSystemPath);
    }

    public void showRoots(FileInfoStructure[] fileInfoStructures) {
        listModel.clear();
        for (FileInfoStructure f : fileInfoStructures) {
            listModel.addElement(f);
        }
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
        setTitle("FileSystem view - " + id);
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.FILE_SYSTEM;
    }

    @Override
    public void setStatus(String status) {

    }


    @Override
    public void display() {
        setupUi();
        setupLogic();
    }

    private void setupUi() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{143, 302, 363, 302, 1};
        gridBagLayout.rowHeights = new int[]{38, 159, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);

        btnGo = new JButton("Go");
        GridBagConstraints gbc_btnGo = new GridBagConstraints();
        gbc_btnGo.insets = new Insets(0, 0, 5, 5);
        gbc_btnGo.gridx = 0;
        gbc_btnGo.gridy = 0;
        getContentPane().add(btnGo, gbc_btnGo);

        txtPath = new JTextField();
        GridBagConstraints gbcTxtPath = new GridBagConstraints();
        gbcTxtPath.gridwidth = 2;
        gbcTxtPath.insets = new Insets(0, 0, 5, 5);
        gbcTxtPath.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtPath.gridx = 1;
        gbcTxtPath.gridy = 0;
        getContentPane().add(txtPath, gbcTxtPath);
        txtPath.setColumns(10);

        listDrive = new JList<FileInfoStructure>();
        listPane = new JScrollPane(listDrive);
        listModel = new DefaultListModel<FileInfoStructure>();

        listDrive.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listDrive.setLayoutOrientation(JList.VERTICAL);
        listDrive.setCellRenderer(new DriveListRenderer());
        listDrive.setModel(listModel);
        listDrive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                    onDrivesClicked();
            }
        });

        GridBagConstraints gbc_listDrive = new GridBagConstraints();
        gbc_listDrive.insets = new Insets(0, 0, 0, 5);
        gbc_listDrive.fill = GridBagConstraints.VERTICAL;
        gbc_listDrive.gridx = 0;
        gbc_listDrive.gridy = 1;
        getContentPane().add(listPane, gbc_listDrive);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 3;
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 1;
        getContentPane().add(scrollPane, gbc_scrollPane);

        explorerTable = new JTable();
        explorerTable.setShowGrid(false);
        explorerTable.setShowVerticalLines(false);

        String[] columnNames = {"Name", "fileSize", "Executable", "Readable", "Writeable"};
        Object[][] data = new Object[0][];
        explorerTable.setModel(new NonEditableTableModel(data, columnNames));
        explorerTable.setDefaultRenderer(Object.class, new FsTableRenderer());
        explorerTable.setShowHorizontalLines(false);
        explorerTable.setAutoCreateRowSorter(true);

        explorerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2)
                    onExplorerTableClicked();

            }

        });

        explorerTable.setFillsViewportHeight(true);
        scrollPane.setViewportView(explorerTable);

        setSize(800, 500);

        popupMenu = new JPopupMenu();
        addPopup(this, popupMenu);

        mntmUpload = new JMenuItem("Upload to ...");
        mntmOpenFile = new JMenuItem("Open File");

        JMenuItem mntmDownload = new JMenuItem("Download");
        popupMenu.add(mntmDownload);

        JMenuItem mntmRename = new JMenuItem("Rename");
        popupMenu.add(mntmRename);

        JMenuItem mntmDelete = new JMenuItem("Delete");
        popupMenu.add(mntmDelete);

        addPopup(explorerTable, popupMenu);

        popupMenu.addPopupMenuListener(new FilePopupMenuListener(this));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void addListener(FileSystemUiListener listener) {
        this.listener = listener;
    }

    @Override
    public FileSystemUiListener getListener() {
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

    public JTable getExplorerTable() {
        return explorerTable;
    }

    public void setExplorerTable(JTable explorerTable) {
        this.explorerTable = explorerTable;
    }

    public JMenuItem getMntmOpenFile() {
        return mntmOpenFile;
    }

    public void setMntmOpenFile(JMenuItem mntmOpenFile) {
        this.mntmOpenFile = mntmOpenFile;
    }

    public JMenuItem getMntmUpload() {
        return mntmUpload;
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    private class PathStack extends Stack<String> {
        @Override
        public synchronized String toString() {
            Iterator<String> it = this.iterator();
            String result = "";
            String temp;
            while (it.hasNext()) {
                temp = it.next();
                if (temp.endsWith("/"))
                    temp += "/";
                result += temp;
            }

            return result;
        }
    }


    private void addPopup(Component component, JPopupMenu popupMenu) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                if (!(e.getComponent() instanceof JTable))
                    return;
                JTable table = (JTable) e.getComponent();
                int selectedRows = table.getSelectedRowCount();
                int row = table.rowAtPoint(e.getPoint());
                if (selectedRows < 2)
                    table.setRowSelectionInterval(row, row);

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

    }

    private void setupLogic() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                notifyMediatorOnClose();
            }
        });

        explorerTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    onMoveBackHistory();
                }
            }
        });
        requestFocus();
    }

    @Override
    public void setMessageProvider(IAppMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public void disableUi() {
        txtPath.setEnabled(false);
        listDrive.setEnabled(false);
        listPane.setEnabled(false);
        btnGo.setEnabled(false);
    }
}
