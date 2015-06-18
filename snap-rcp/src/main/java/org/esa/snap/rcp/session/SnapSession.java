package org.esa.snap.rcp.session;

import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductNode;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.actions.file.CloseAllProductsAction;
import org.esa.snap.util.io.FileUtils;

import java.io.File;


public class SnapSession {
    private File sessionFile;

    public void setSessionFile(File sessionFile) {
        this.sessionFile = sessionFile;
        updateMainFrameTitle();
    }
    public File getSessionFile() {
        return sessionFile;
    }


    public void closeAllProducts(){
        new CloseAllProductsAction().execute();


    }
    public void updateMainFrameTitle() {
        final StringBuilder docTitle = new StringBuilder();

        final ProductNode productNode = SnapApp.getDefault().getSelectedProductNode();
        if (productNode != null) {
            docTitle.append(productNode.getDisplayName());

            docTitle.append(" - [");
            final Product product = productNode.getProduct();
            File productFile = product.getFileLocation();
            if (productFile != null) {
                if (product.isModified()) {
                    docTitle.append("*");
                }
                docTitle.append(FileUtils.getDisplayText(productFile, 100));
            } else {
                docTitle.append("Product not saved");
            }
            docTitle.append("]");

            docTitle.append(" - [");
            final File sessionFile = getSessionFile();
            if (sessionFile != null) {
                docTitle.append(FileUtils.getDisplayText(sessionFile, 50));
            } else {
                docTitle.append("Session not saved");
            }
            docTitle.append("]");
        }

//        setCurrentDocTitle(docTitle.toString());
    }

}
