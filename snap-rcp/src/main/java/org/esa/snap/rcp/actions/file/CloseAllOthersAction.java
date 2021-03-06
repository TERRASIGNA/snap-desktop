/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package org.esa.snap.rcp.actions.file;

import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductNode;
import org.esa.snap.rcp.SnapApp;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This action closes all opened products other than the one selected.
 */
@ActionID(
        category = "File",
        id = "CloseAllOthersAction"
)
@ActionRegistration(
        displayName = "#CTL_CloseAllOthersActionName"
)
@ActionReferences({
        @ActionReference(path = "Menu/File", position = 41),
        @ActionReference(
                path = "Context/Product/Product",
                position = 850
        ),
})
@NbBundle.Messages({
        "CTL_CloseAllOthersActionName=Close All Other Products"
})
public class CloseAllOthersAction extends AbstractAction implements ContextAwareAction, LookupListener {

    private final Lookup lkp;

    public CloseAllOthersAction() {
        this(Utilities.actionsGlobalContext());
    }

    public CloseAllOthersAction(Lookup lkp) {
        super("Close All Other Products");
        this.lkp = lkp;
        Lookup.Result<ProductNode> lkpContext = lkp.lookupResult(ProductNode.class);
        lkpContext.addLookupListener(WeakListeners.create(LookupListener.class, this, lkpContext));
        setEnableState();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new CloseAllOthersAction(actionContext);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        setEnableState();
    }

    private void setEnableState() {
        ProductNode productNode = lkp.lookup(ProductNode.class);
        setEnabled(productNode != null);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final Product selectedProduct = SnapApp.getDefault().getSelectedProduct();
        final Product[] products = SnapApp.getDefault().getProductManager().getProducts();
        final List<Product> productsToClose = new ArrayList<>(products.length);
        for (Product product : products) {
            if (product != selectedProduct) {
                productsToClose.add(product);
            }
        }
        new CloseProductAction(productsToClose).execute();
    }
}
