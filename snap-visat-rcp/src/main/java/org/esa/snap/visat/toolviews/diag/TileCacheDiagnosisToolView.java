/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.visat.toolviews.diag;

import org.esa.snap.framework.ui.application.support.AbstractToolView;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TileCacheDiagnosisToolView extends AbstractToolView {
    public static final String ID = TileCacheDiagnosisToolView.class.getName();
    private Timer timer;
    private TileCacheMonitor tileCacheMonitor;

    public TileCacheDiagnosisToolView() {
    }

    @Override
    protected JComponent createControl() {
        tileCacheMonitor = new TileCacheMonitor();
        JPanel panel = tileCacheMonitor.createPanel();
        timer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isVisible()) {
                    tileCacheMonitor.updateState();
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
        return panel;
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    @Override
    public void componentOpened() {
        timer.restart();
    }

    /**
     * The default implementation does nothing.
     * <p>Clients shall not call this method directly.</p>
     */
    @Override
    public void componentClosed() {
        timer.stop();
    }
}
