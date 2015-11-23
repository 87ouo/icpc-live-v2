package ru.ifmo.acm.backend.player.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author: pashka
 */
public class GreenScreenWidget extends Widget {
    private final Color color = Color.black;

    public GreenScreenWidget() {
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {
        if (isVisible()) {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        }
    }
}
