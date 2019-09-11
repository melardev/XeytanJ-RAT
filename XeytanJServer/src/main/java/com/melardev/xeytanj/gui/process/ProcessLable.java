package com.melardev.xeytanj.gui.process;

import javax.swing.*;

public class ProcessLable extends JLabel {

    public ProcessLable(String myar) {
        super(myar);
    }

    public ProcessLable(Icon imageIcon, String myar) {
        super(myar);
        if (imageIcon != null)
            setIcon(imageIcon);
    }

    @Override
    public String toString() {
        return getText();
    }
}

