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
package org.esa.snap.visat.actions.session;

import org.esa.snap.framework.ui.command.CommandEvent;
import org.esa.snap.framework.ui.command.ExecCommand;
import org.esa.snap.visat.VisatApp;


/**
 * Saves a VISAT session with a new filename.
 *
 * @author Norman Fomferra
 * @version $Revision$ $Date$
 * @since BEAM 4.6
 */
public class SaveSessionAsAction extends ExecCommand {
    public static final String ID = "saveSessionAs";
    
    @Override
    public final void actionPerformed(final CommandEvent event) {
        final SaveSessionAction action = (SaveSessionAction) VisatApp.getApp().getCommandManager().getCommand(SaveSessionAction.ID);
        action.saveSession(true);
    }

    @Override
    public final void updateState(final CommandEvent event) {
        final VisatApp app = VisatApp.getApp();
        setEnabled(app.getSessionFile() != null);
    }
}
