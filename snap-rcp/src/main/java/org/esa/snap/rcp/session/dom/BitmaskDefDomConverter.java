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

package org.esa.snap.rcp.session.dom;

import com.bc.ceres.binding.dom.DomElement;
import org.esa.snap.framework.datamodel.BitmaskDef;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductManager;

class BitmaskDefDomConverter extends ProductNodeDomConverter<BitmaskDef> {

    BitmaskDefDomConverter(ProductManager productManager) {
        super(BitmaskDef.class, productManager);
    }

    @Override
    protected BitmaskDef getProductNode(DomElement parentElement, Product product) {
        final String bitmaskName = parentElement.getChild("bitmaskName").getValue();
        return product.getBitmaskDef(bitmaskName);
    }

    @Override
    protected void convertProductNodeToDom(BitmaskDef bitmaskDef, DomElement parentElement) {
        final DomElement bitmaskName = parentElement.createChild("bitmaskName");
        bitmaskName.setValue(bitmaskDef.getName());
    }
}
