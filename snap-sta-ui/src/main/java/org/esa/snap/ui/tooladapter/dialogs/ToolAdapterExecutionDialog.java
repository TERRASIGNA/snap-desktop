/*
 *
 *  * Copyright (C) 2015 CS SI
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.esa.snap.ui.tooladapter.dialogs;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.gpf.GPF;
import org.esa.snap.framework.gpf.Operator;
import org.esa.snap.framework.gpf.descriptor.ParameterDescriptor;
import org.esa.snap.framework.gpf.descriptor.ToolAdapterOperatorDescriptor;
import org.esa.snap.framework.gpf.operators.tooladapter.ToolAdapterConstants;
import org.esa.snap.framework.gpf.operators.tooladapter.ToolAdapterIO;
import org.esa.snap.framework.gpf.operators.tooladapter.ToolAdapterOp;
import org.esa.snap.framework.gpf.ui.OperatorMenu;
import org.esa.snap.framework.gpf.ui.OperatorParameterSupport;
import org.esa.snap.framework.gpf.ui.SingleTargetProductDialog;
import org.esa.snap.framework.ui.AppContext;
import org.esa.snap.rcp.SnapDialogs;
import org.esa.snap.rcp.actions.file.SaveProductAsAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Form dialog for running a tool adapter operator.
 *
 * @author Lucian Barbulescu.
 * @author Cosmin Cara
 */
@NbBundle.Messages({
        "NoSourceProductWarning_Text=Please make sure you have selected the necessary input products",
        "RequiredTargetProductMissingWarning_Text=A target product is required in adapter's template, but none was provided",
        "NoOutput_Text=The operator did not produce any output",
        "BeginOfErrorMessages_Text=The operator completed with the following errors:\n",
        "OutputTitle_Text=Process output"
})
public class ToolAdapterExecutionDialog extends SingleTargetProductDialog {

    public static final String SOURCE_PRODUCT_FIELD = "sourceProduct";
    /**
     * Operator identifier.
     */
    private ToolAdapterOperatorDescriptor operatorDescriptor;
    /**
     * Parameters related info.
     */
    private OperatorParameterSupport parameterSupport;
    /**
     * The form used to get the user's input
     */
    private ToolExecutionForm form;

    private Product result;

    private OperatorTask operatorTask;

    public static final String helpID = "sta_execution";

    /**
     * Constructor.
     *
     * @param descriptor    The operator descriptor
     * @param appContext    The application context
     * @param title         The dialog title
     */
    public ToolAdapterExecutionDialog(ToolAdapterOperatorDescriptor descriptor, AppContext appContext, String title) {
        super(appContext, title, helpID);
        this.operatorDescriptor = descriptor;

        this.parameterSupport = new OperatorParameterSupport(descriptor);

        form = new ToolExecutionForm(appContext, descriptor, parameterSupport.getPropertySet(),
                getTargetProductSelector());
        OperatorMenu operatorMenu = new OperatorMenu(this.getJDialog(),
                descriptor,
                parameterSupport,
                appContext,
                helpID);
        getJDialog().setJMenuBar(operatorMenu.createDefaultMenu());
    }

    /* Begin @Override methods section */

    @Override
    protected void onApply() {
        final Product[] sourceProducts = form.getSourceProducts();
        List<ParameterDescriptor> descriptors = Arrays.stream(operatorDescriptor.getParameterDescriptors())
                .filter(p -> ToolAdapterConstants.TOOL_TARGET_PRODUCT_FILE.equals(p.getName()))
                .collect(Collectors.toList());
        String templateContents = "";
        try {
            templateContents = ToolAdapterIO.readOperatorTemplate(operatorDescriptor.getName());
        } catch (IOException ignored) {
        }
        if (Arrays.stream(sourceProducts).anyMatch(p -> p == null)) {
            SnapDialogs.showWarning(Bundle.NoSourceProductWarning_Text());
        } else if (descriptors.size() == 1 && form.getPropertyValue(ToolAdapterConstants.TOOL_TARGET_PRODUCT_FILE) == null &&
                templateContents.contains("$" + ToolAdapterConstants.TOOL_TARGET_PRODUCT_FILE)) {
                SnapDialogs.showWarning(Bundle.RequiredTargetProductMissingWarning_Text());
        } else {
            if (!canApply()) {
                onClose();
                ToolAdapterEditorDialog dialog = new ToolAdapterEditorDialog(appContext, operatorDescriptor, false);
                dialog.show();
            } else {
                if (validateUserInput()) {
                    String productDir = targetProductSelector.getModel().getProductDir().getAbsolutePath();
                    appContext.getPreferences().setPropertyString(SaveProductAsAction.PREFERENCES_KEY_LAST_PRODUCT_DIR, productDir);
                    Map<String, Product> sourceProductMap = new HashMap<>();
                    sourceProductMap.put(SOURCE_PRODUCT_FIELD, sourceProducts[0]);
                    Operator op = GPF.getDefaultInstance().createOperator(operatorDescriptor.getName(), parameterSupport.getParameterMap(), sourceProductMap, null);
                    op.setSourceProducts(sourceProducts);
                    operatorTask = new OperatorTask(op, ToolAdapterExecutionDialog.this::operatorCompleted);
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle(this.getTitle());
                    ((ToolAdapterOp) op).setProgressMonitor(new ProgressWrapper(progressHandle));
                    ProgressUtils.runOffEventThreadWithProgressDialog(operatorTask, this.getTitle(), progressHandle, true, 1, 1);
                }
            }
        }
    }

    @Override
    public int show() {
        form.prepareShow();
        setContent(form);
        return super.show();
    }

    @Override
    public void hide() {
        form.prepareHide();
        super.hide();
    }

    @Override
    protected Product createTargetProduct() throws Exception {
        return result;
    }

    @Override
    protected boolean canApply() {
        try {
            Path toolLocation = operatorDescriptor.getExpandedLocation(operatorDescriptor.getMainToolFileLocation()).toPath();
            if (!(Files.exists(toolLocation) && Files.isExecutable(toolLocation))) {
                SnapDialogs.showWarning(Bundle.MSG_Inexistent_Tool_Path_Text());
                return false;
            }
            File workingDir = operatorDescriptor.getExpandedLocation(operatorDescriptor.getWorkingDir());
            if (!(workingDir != null && workingDir.exists() && workingDir.isDirectory())) {
                SnapDialogs.showWarning(Bundle.MSG_Inexistent_WorkDir_Text());
                return false;
            }
            ParameterDescriptor[] parameterDescriptors = operatorDescriptor.getParameterDescriptors();
            if (parameterDescriptors != null && parameterDescriptors.length > 0) {
                for (ParameterDescriptor parameterDescriptor : parameterDescriptors) {
                    Class<?> dataType = parameterDescriptor.getDataType();
                    String defaultValue = parameterDescriptor.getDefaultValue();
                    if (File.class.isAssignableFrom(dataType) &&
                            (parameterDescriptor.isNotNull() || parameterDescriptor.isNotEmpty()) &&
                            (defaultValue == null || defaultValue.isEmpty() || !Files.exists(Paths.get(defaultValue)))) {
                        SnapDialogs.showWarning(String.format(Bundle.MSG_Inexistem_Parameter_Value_Text(),
                                parameterDescriptor.getName(), parameterDescriptor.isNotNull() ? "NotNull" : "NotEmpty"));
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    protected void onCancel() {
        super.onCancel();
    }

    @Override
    protected void onClose() {
        super.onClose();
    }

    /* End @Override methods section */

    /**
     * Performs any validation on the user input.
     *
     * @return  <code>true</code> if the input is valid, <code>false</code> otherwise
     */
    private boolean validateUserInput() {
        boolean isValid;
        File productDir = targetProductSelector.getModel().getProductDir();
        isValid = (productDir != null) && productDir.exists();
        Product[] sourceProducts = form.getSourceProducts();
        isValid &= (sourceProducts != null) && sourceProducts.length > 0;

        return isValid;
    }

    /**
     * This is actually the callback method to be passed to the runnable
     * wrapping the operator execution.
     *
     * @param result    The output product
     */
    private void operatorCompleted(Product result) {
        this.result = result;
        super.onApply();
        displayErrors();
    }

    private void tearDown(Throwable throwable) {
        boolean hasBeenCancelled = operatorTask != null && !operatorTask.hasCompleted;
        if (operatorTask != null) {
            operatorTask.cancel();
        }
        if (throwable != null) {
            if (!hasBeenCancelled)
                SnapDialogs.showError("Execution failed", throwable.getMessage());
                //handleInitialisationError(throwable);
        }
        displayErrors();
    }

    private void displayErrors() {
        if (form.shouldDisplayOutput()) {
            List<String> output = operatorTask.getOutput();
            StringBuilder builder = new StringBuilder();
            if (output.size() > 0) {
                for (String anOutput : output) {
                    builder.append(String.format("%s%n", shrinkText(anOutput)));
                }
                SnapDialogs.showInformation(Bundle.OutputTitle_Text(), builder.toString(), null);
            } else {
                builder.append(Bundle.NoOutput_Text());
            }
        } else if (operatorTask != null) {
            List<String> errors = operatorTask.getErrors();
            if (errors != null && errors.size() > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(Bundle.BeginOfErrorMessages_Text());
                int messageCount = Math.min(errors.size(), 10);
                for (int i = 0; i < messageCount; i++) {
                    builder.append(String.format("[%s] %s%n", i + 1, shrinkText(errors.get(i))));
                }
                SnapDialogs.showWarning(builder.toString());
            }
        }
    }

    private String shrinkText(String input) {
        int charLimit = 80;
        if (input.length() <= charLimit) {
            return input;
        } else {
            StringBuilder builder= new StringBuilder();
            boolean endOfString = false;
            int start = 0, end;
            while (start < input.length() - 1) {
                int charCount = 0, lastSpace = 0;
                while (charCount < charLimit) {
                    if (input.charAt(charCount + start) == ' ') {
                        lastSpace = charCount;
                    }
                    charCount++;
                    if (charCount + start == input.length()) {
                        endOfString = true;
                        break;
                    }
                }
                end = endOfString ? input.length() : (lastSpace > 0) ? lastSpace + start : charCount + start;
                builder.append(input.substring(start, end)).append(String.format("%n\t"));
                start = end + 1;
            }
            return builder.toString();
        }
    }

    /**
     * Runnable for executing the operator. It requires a callback
     * method that is to be called when the operator has finished its
     * execution.
     */
    public class OperatorTask implements Runnable, Cancellable {

        private Operator operator;
        private Consumer<Product> callbackMethod;
        private boolean hasCompleted;

        /**
         * Constructs a runnable for the given operator that will
         * call back the given method when the execution has finished.
         *
         * @param op        The operator to be executed
         * @param callback  The callback method to be invoked at completion
         */
        public OperatorTask(Operator op, Consumer<Product> callback) {
            operator = op;
            callbackMethod = callback;
        }

        @Override
        public boolean cancel() {
            if (!hasCompleted) {
                if (operator instanceof ToolAdapterOp) {
                    ((ToolAdapterOp) operator).stop();
                    onCancel();
                }
                hasCompleted = true;
            }
            return true;
        }

        @Override
        public void run() {
            try {
                callbackMethod.accept(operator.getTargetProduct());
            } catch (Throwable t) {
                tearDown(t);
            } finally {
                hasCompleted = true;
            }
        }

        public List<String> getErrors() {
            List<String> errors = null;
            if (operator != null && operator instanceof ToolAdapterOp) {
                errors = ((ToolAdapterOp) operator).getErrors();
            }
            return errors;
        }

        public List<String> getOutput() {
            List<String> allMessages = null;
            if (operator != null && operator instanceof ToolAdapterOp) {
                allMessages = ((ToolAdapterOp) operator).getExecutionOutput();
            }
            return allMessages;
        }
    }

    class ProgressWrapper implements ProgressMonitor {

        private ProgressHandle progressHandle;

        ProgressWrapper(ProgressHandle handle) {
            this.progressHandle = handle;
        }

        @Override
        public void beginTask(String taskName, int totalWork) {
            this.progressHandle.setDisplayName(taskName);
            this.progressHandle.start(totalWork, -1);
        }

        @Override
        public void done() {
            this.progressHandle.finish();
        }

        @Override
        public void internalWorked(double work) {
            this.progressHandle.progress((int)work);
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.progressHandle.suspend("Cancelled");
        }

        @Override
        public void setTaskName(String taskName) {
            this.progressHandle.progress(taskName);
        }

        @Override
        public void setSubTaskName(String subTaskName) {
            this.progressHandle.progress(subTaskName);
        }

        @Override
        public void worked(int work) {
            internalWorked(work);
        }
    }
}
