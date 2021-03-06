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

package org.esa.snap.visat;

import com.bc.ceres.swing.selection.SelectionContext;
import com.bc.ceres.swing.selection.SelectionManager;
import com.bc.swing.desktop.TabbedDesktopPane;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import org.esa.snap.framework.ui.BasicView;
import org.esa.snap.framework.ui.application.DocView;
import org.esa.snap.framework.ui.application.PageComponent;
import org.esa.snap.framework.ui.application.PageComponentPane;
import org.esa.snap.framework.ui.application.ToolView;
import org.esa.snap.framework.ui.application.ToolViewDescriptor;
import org.esa.snap.framework.ui.application.ToolViewDescriptor.State;
import org.esa.snap.framework.ui.application.support.AbstractApplicationPage;
import org.esa.snap.framework.ui.application.support.DefaultToolViewPane;
import org.esa.snap.framework.ui.command.CommandManager;
import org.esa.snap.util.Debug;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import java.awt.Container;
import java.awt.Window;
import java.beans.PropertyVetoException;

public class VisatApplicationPage extends AbstractApplicationPage {

    private final Window window;
    private final CommandManager commandManager;
    private final SelectionManager selectionManager;
    private final DockingManager dockingManager;
    private final TabbedDesktopPane documentPane;

    public VisatApplicationPage(Window window,
                                CommandManager commandManager,
                                SelectionManager selectionManager,
                                DockingManager dockingManager,
                                TabbedDesktopPane documentPane) {
        this.window = window;
        this.commandManager = commandManager;
        this.selectionManager = selectionManager;
        this.dockingManager = dockingManager;
        this.documentPane = documentPane;
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public ToolViewDescriptor getToolViewDescriptor(String id) {
        return VisatActivator.getInstance().getToolViewDescriptor(id);
    }

    @Override
    protected void doAddToolView(final ToolView toolView) {
        DockableFrame dockableFrame = (DockableFrame) toolView.getContext().getPane().getControl();
        dockableFrame.addDockableFrameListener(new DockableFrameAdapter() {
            @Override
            public void dockableFrameActivated(DockableFrameEvent dockableFrameEvent) {
                setActiveComponent();
            }

            @Override
            public void dockableFrameDeactivated(DockableFrameEvent dockableFrameEvent) {
                setActiveComponent();
            }
        });
        dockingManager.addFrame(dockableFrame);
    }

    @Override
    protected void doRemoveToolView(ToolView toolView) {
        dockingManager.removeFrame(toolView.getId());
    }

    @Override
    protected void doShowToolView(ToolView toolView) {
        dockingManager.showFrame(toolView.getId());
        if (shouldFloat(toolView)) {
            dockingManager.floatFrame(toolView.getId(), null, false);
        }
    }

    @Override
    protected void doHideToolView(ToolView toolView) {
        dockingManager.hideFrame(toolView.getId());
    }

    @Override
    protected boolean giveFocusTo(PageComponent pageComponent) {
        if (pageComponent instanceof ToolView) {
            dockingManager.activateFrame(pageComponent.getId());
        } else if (pageComponent instanceof DocView) {
            JInternalFrame frame = (JInternalFrame) pageComponent.getContext().getPane().getControl();
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                // ignore
            }
        } else {
            throw new IllegalArgumentException(pageComponent.getClass() + " not handled");
        }
        return getActiveComponent() == pageComponent;
    }

    @Override
    protected PageComponentPane createToolViewPane(ToolView toolView) {
        return new DefaultToolViewPane(toolView);
    }

    @Override
    protected JComponent createControl() {
        return dockingManager.getDockedFrameContainer();
    }

    @Override
    protected void setActiveComponent() {
        String activeFrameKey = dockingManager.getActiveFrameKey();
        Debug.trace("setActiveComponent: " + activeFrameKey);

        ToolView toolView = null;
        if (activeFrameKey != null) {
            DockableFrame activeFrame = dockingManager.getFrame(activeFrameKey);
            if (activeFrame != null) {
                toolView = getToolView(activeFrame);
            }
        }
        if (toolView != null) {
            setActiveComponent(toolView);
        } else {
            SelectionContext context = null;
            // No tool view currently selected, must look for active "DocView".
            JInternalFrame selectedFrame = documentPane.getSelectedFrame();
            if (selectedFrame != null) {
                Container pageComponent = selectedFrame.getContentPane();
                if (pageComponent instanceof BasicView) {
                    BasicView view = (BasicView) pageComponent;
                    context = view.getSelectionContext();
                }
            }
            getSelectionManager().setSelectionContext(context);
        }
    }

    private ToolView getToolView(DockableFrame activeFrame) {
        ToolView[] toolViews = getToolViews();
        for (ToolView toolView : toolViews) {
            if (activeFrame == toolView.getContext().getPane().getControl()) {
                return toolView;
            }
        }
        return null;
    }

    private boolean shouldFloat(ToolView toolView) {
        ToolViewDescriptor toolViewDescriptor = getToolViewDescriptor(toolView.getId());
        State initState = toolViewDescriptor.getInitState();
        DockableFrame frame = dockingManager.getFrame(toolView.getId());
        return frame != null
                && frame.getContext().getDockPreviousState() == null
                && frame.getContext().getFloatPreviousState() == null
                && initState == State.HIDDEN;
    }


}
