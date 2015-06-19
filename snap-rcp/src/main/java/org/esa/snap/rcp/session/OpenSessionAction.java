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
package org.esa.snap.rcp.session;

import com.bc.ceres.core.CanceledException;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.grender.Viewport;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.esa.snap.framework.dataio.ProductIO;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.ui.command.CommandEvent;
import org.esa.snap.framework.ui.command.ExecCommand;
import org.esa.snap.framework.ui.product.ProductNodeView;
import org.esa.snap.framework.ui.product.ProductSceneView;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.SnapDialogs;
import org.esa.snap.util.SystemUtils;
import org.esa.snap.util.io.SnapFileFilter;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;


/**
 * Opens a VISAT session.
 *
 * @author Norman Fomferra
 * @version $Revision$ $Date$
 * @since BEAM 4.6
 */
public class OpenSessionAction extends ExecCommand {

    public static final String ID = "openSession";

    public static final String LAST_SESSION_DIR_KEY = "beam.lastSessionDir";
    private static final String TITLE = "Open Session";

    public static SnapFileFilter getSessionFileFilter() {
        return new SnapFileFilter("SESSION",
                                  new String[]{String.format(".%s", SystemUtils.getApplicationContextId()), ".beam"},
                                  String.format("%s Session file", SystemUtils.getApplicationName()));
    }

    @Override
    public void actionPerformed(final CommandEvent event) {

        final SnapApp snapApp = SnapApp.getDefault();

        final SnapSession snapSession = new SnapSession();
        if (snapSession.getSessionFile() != null) {
            final SnapDialogs.Answer i = SnapDialogs.requestDecision(TITLE,
                                                                     "This will close the current session.\n" +
                                                                             "Do you want to continue?", true, null);
            if (i != SnapDialogs.Answer.YES) {
                return;
            }
        }

        final File sessionFile = SnapDialogs.requestFileForOpen(TITLE, false,
                                                                getSessionFileFilter(),
                                                                LAST_SESSION_DIR_KEY);
        if (sessionFile == null) {
            return;
        }
        if (sessionFile.equals(snapSession.getSessionFile())) {
            SnapDialogs.showError(TITLE, "Session has already been opened.");
            return;
        }

        openSession(snapSession, sessionFile);
    }

    public void openSession(SnapSession snapSession, File sessionFile) {
        snapSession.setSessionFile(sessionFile);
        snapSession.closeAllProducts();
//        SwingWorker<RestoredSession, Object> worker = new SwingWorker<>(snapSession, sessionFile);
//        worker.execute();
    }

    private static class OpenSessionWorker extends ProgressMonitorSwingWorker<RestoredSession, Object> {

        private final SnapSession snapSession;
        private final File sessionFile;

        public OpenSessionWorker(SnapSession snapSession, File sessionFile) {
            super(SnapApp.getDefault().getMainFrame(), TITLE);
            this.snapSession = snapSession;
            this.sessionFile = sessionFile;
        }

        @Override
        protected RestoredSession doInBackground(ProgressMonitor pm) throws Exception {
            final Session session = SessionIO.getInstance().readSession(sessionFile);
            final File parentFile = sessionFile.getParentFile();
            final URI rootURI;
            if (parentFile != null) {
                rootURI = parentFile.toURI();
            } else {
                rootURI = new File(".").toURI();
            }
            return session.restore( new SnapApp.SnapContext(), rootURI, pm, new SessionProblemSolver());
        }

        @Override
        protected void done() {
            final RestoredSession restoredSession;
            try {
                restoredSession = get();
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof CanceledException) {
                    return;
                }
                SnapDialogs.showError(MessageFormat.format("An unexpected exception occurred!\nMessage: {0}",
                                                           e.getCause().getMessage()));
                e.printStackTrace();
                return;
            }
            final Exception[] problems = restoredSession.getProblems();
            if (problems.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("The following problem(s) occurred while opening a session:\n");
                for (Exception problem : problems) {
                    problem.printStackTrace();
                    sb.append("  ");
                    sb.append(problem.getMessage());
                    sb.append("\n");
                }
                SnapDialogs.showWarning(sb.toString());
            }

            final Product[] products = restoredSession.getProducts();
            for (Product product : products) {
                SnapApp.getDefault().getProductManager().addProduct(product);
            }


            // todo - Handle view persistence in a generic way. (nf - 08.05.2009)
            //        These are the only 3 views currently known in BEAM.
            //        NEST already uses another view type which cannot be stored/restored.
            //
//            OpenImageViewAction showImageViewAction = getAction(OpenImageViewAction.ID);
//            OpenRGBImageViewAction showImageViewRGBAction = getAction(OpenRGBImageViewAction.ID);
//            ShowMetadataViewAction showMetadataViewAction = getAction(ShowMetadataViewAction.ID);

            final ProductNodeView[] nodeViews = restoredSession.getViews();
            for (ProductNodeView nodeView : nodeViews) {
                Rectangle bounds = nodeView.getBounds();
                JInternalFrame internalFrame = null;
                if (nodeView instanceof ProductSceneView) {
                    ProductSceneView sceneView = (ProductSceneView) nodeView;

                    sceneView.getLayerCanvas().setInitiallyZoomingAll(false);
                    Viewport viewport = sceneView.getLayerCanvas().getViewport().clone();
                    if (sceneView.isRGB()) {
//                        internalFrame = opeImageViewRGBAction.openInternalFrame(sceneView, false);
                    } else {
//                        internalFrame = showImageViewAction.openInternalFrame(sceneView, false);
                    }
                    sceneView.getLayerCanvas().getViewport().setTransform(viewport);
//                } else if (nodeView instanceof ProductMetadataView) {
//                    ProductMetadataView metadataView = (ProductMetadataView) nodeView;
//
//                    internalFrame = showMetadataViewAction.openInternalFrame(metadataView);
                }
                if (internalFrame != null) {
                    try {
                        internalFrame.setMaximum(false);
                    } catch (PropertyVetoException e) {
                        // ok to ignore
                    }
                    internalFrame.setBounds(bounds);
                }
            }
        }

//        private <T> T getAction(String actionId) {
//            T action = (T) snapApp.getCommandManager().getCommand(actionId);
//            if (action == null) {
//                throw new IllegalStateException("Action not found: actionId=" + actionId);
//            }
//            return action;
//        }

        private class SessionProblemSolver implements Session.ProblemSolver {
            @Override
            public Product solveProductNotFound(int id, File file) throws CanceledException {
                final File[] newFile = new File[1];
                final SnapDialogs.Answer answer;
                final String title = MessageFormat.format(TITLE + " - Resolving [{0}]", file);
                final String msg = MessageFormat.format("Product [{0}] has been renamed or (re-)moved.\n" +
                                                                "Its location was [{1}].\n" +
                                                                "Do you wish to provide its new location?\n" +
                                                                "(Select ''No'' if the product shall no longer be part of the session.)",
                                                        id, file);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
//                            answer = SnapDialogs.requestDecision(title, msg, true, null);
//                            if (answer == SnapDialogs.Answer.YES) {
//                                newFile[0] = SnapDialogs.requestFileForOpen(title, false, null, null);
//                            }
                        }
                    });
                } catch (Exception e) {
                    throw new CanceledException();
                }

//                if (answer == SnapDialogs.Answer.CANCELLED) {
//                    throw new CanceledException();
//                }

                if (newFile[0] != null) {
                    try {
                        return ProductIO.readProduct(newFile[0]);
                    } catch (IOException e) {
                    }
                }
                return null;
            }
        }
    }

}
