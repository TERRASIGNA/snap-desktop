/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.esa.snap.rcp.actions.view.overlay;

import org.esa.snap.framework.ui.product.ProductSceneView;
import org.esa.snap.util.ProductUtils;
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
@ActionID(category = "View", id = "OverlayGraticuleLayerAction")
@ActionRegistration(displayName = "#CTL_OverlayGraticuleLayerActionName", lazy = false)
@ActionReferences({
        @ActionReference(path = "Menu/View/Overlay", position = 20),
        @ActionReference(path = "Toolbars/Overlay", position = 20)
})
@NbBundle.Messages({
        "CTL_OverlayGraticuleLayerActionName=Toggle Graticule Overlay",
        "CTL_OverlayGraticuleLayerActionToolTip=Show/hide graticule overlay for the selected image"
})
public final class OverlayGraticuleLayerAction extends AbstractOverlayAction {

    public OverlayGraticuleLayerAction() {
        this(Utilities.actionsGlobalContext());
    }

    public OverlayGraticuleLayerAction(Lookup lkp) {
        super(lkp);
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new OverlayGraticuleLayerAction(lkp);
    }

    @Override
    protected void initActionProperties() {
        putValue(NAME, Bundle.CTL_OverlayGraticuleLayerActionName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GraticuleOverlay.gif", false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon("org/esa/snap/rcp/icons/GraticuleOverlay24.gif", false));
        putValue(SHORT_DESCRIPTION, Bundle.CTL_OverlayGraticuleLayerActionToolTip());
    }

    @Override
    protected boolean getActionSelectionState(ProductSceneView view) {
        return view.isGraticuleOverlayEnabled();
    }

    @Override
    protected boolean getActionEnableState(ProductSceneView view) {
        return ProductUtils.canGetPixelPos(view.getRaster());
    }

    @Override
    protected void setOverlayEnableState(ProductSceneView view) {
        view.setGraticuleOverlayEnabled(!getActionSelectionState(view));
    }


}
