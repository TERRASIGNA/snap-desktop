/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.esa.snap.rcp.actions.view.overlay;

import org.esa.snap.framework.ui.product.ProductSceneView;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.Action;

/**
 * @author Marco Peters
 */
@ActionID(category = "View", id = "OverlayGcpLayerAction")
@ActionRegistration(displayName = "#CTL_OverlayGcpLayerActionName", lazy = false)
@ActionReferences({
        @ActionReference(path = "Menu/View/Overlay", position = 40),
        @ActionReference(path = "Toolbars/Overlay", position = 40)
})
@NbBundle.Messages({
        "CTL_OverlayGcpLayerActionName=Toggle GCP Overlay",
        "CTL_OverlayGcpLayerActionToolTip=Show/hide GCP overlay for the selected image"
})
public final class OverlayGcpLayerAction extends AbstractOverlayAction {

    public OverlayGcpLayerAction() {
        this(Utilities.actionsGlobalContext());
    }

    public OverlayGcpLayerAction(Lookup lkp) {
        super(lkp);
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new OverlayGcpLayerAction(lkp);
    }

    @Override
    protected void initActionProperties() {
        putValue(NAME, Bundle.CTL_OverlayGcpLayerActionName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GcpOverlay.gif", false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GcpOverlay24.gif", false));
        putValue(SHORT_DESCRIPTION, Bundle.CTL_OverlayGcpLayerActionToolTip());
    }

    @Override
    protected boolean getActionSelectionState(ProductSceneView view) {
        return view.isGcpOverlayEnabled();
    }

    @Override
    protected boolean getActionEnableState(ProductSceneView view) {
        return view.getProduct().getGcpGroup().getNodeCount() > 0;
    }

    @Override
    protected void setOverlayEnableState(ProductSceneView view) {
        view.setGcpOverlayEnabled(!getActionSelectionState(view));
    }


}
