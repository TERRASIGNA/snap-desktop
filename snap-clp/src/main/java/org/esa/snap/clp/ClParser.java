package org.esa.snap.clp;

import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.OperatorSpiRegistry;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Marco Peters
 */
@ServiceProvider(service = OptionProcessor.class)
@NbBundle.Messages({
        "MSG_GPT=Invokes the Graph Processing Tool and what happens if this is a very extremely extraordinary gigantic string phrase and green like the incredible Hulk. And when it is even longer without any line break what may happen to the console output. Will it have a slider?"
})

public class ClParser extends OptionProcessor {

    private static final Logger LOG = Logger.getLogger(ClParser.class.getName());
    private Option gptOption = Option.shortDescription(Option.withoutArgument('g', "gpt"), "org.esa.snap.clp.Bundle", "MSG_GPT");
    private Option paramOptions = Option.additionalArguments('p', "param");

    @Override
    protected Set<Option> getOptions() {
        HashSet<Option> options = new HashSet<>();
        options.add(gptOption);
        options.add(paramOptions);
        return options;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        env.usage();
        if (optionValues.containsKey(gptOption)) {

//            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
//
//            // Set JAI tile scheduler parallelism
//            int processorCount = Runtime.getRuntime().availableProcessors();
//            int parallelism = Integer.getInteger("snap.jai.parallelism", processorCount);
//            JAI.getDefaultInstance().getTileScheduler().setParallelism(parallelism);
//
//            loadJaiRegistryFile(ReinterpretDescriptor.class, "/META-INF/registryFile.jai");
//            loadJaiRegistryFile(JAI.class, "/META-INF/javax.media.jai.registryFile.jai");

//            initGPF();

            String[] strings = optionValues.get(paramOptions);
//            if (optionValues.containsKey(defaultOptions)) {
//                try {
//                    GPT.run(optionValues.get(defaultOptions));
//                } catch (Exception e) {
//                    throw new CommandException(12345, e.getMessage());
//                }
//            }
        }
    }

    private static void loadJaiRegistryFile(Class<?> cls, String jaiRegistryPath) {
        // Must use a new operation registry in order to register JAI operators defined in Ceres and BEAM
        OperationRegistry operationRegistry = OperationRegistry.getThreadSafeOperationRegistry();
        InputStream is = cls.getResourceAsStream(jaiRegistryPath);
        if (is != null) {
            final PrintStream oldErr = System.err;
            try {
                // Suppress annoying and harmless JAI error messages saying that a descriptor is already registered.
                System.setErr(new PrintStream(new ByteArrayOutputStream()));
                operationRegistry.updateFromStream(is);
                operationRegistry.registerServices(cls.getClassLoader());
                JAI.getDefaultInstance().setOperationRegistry(operationRegistry);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, MessageFormat.format("Error loading {0}: {1}", jaiRegistryPath, e.getMessage()), e);
            } finally {
                System.setErr(oldErr);
            }
        } else {
            LOG.warning(MessageFormat.format("{0} not found", jaiRegistryPath));
        }
    }

    private static void initGPF() {
        OperatorSpiRegistry operatorSpiRegistry = GPF.getDefaultInstance().getOperatorSpiRegistry();
        operatorSpiRegistry.loadOperatorSpis();
        Set<OperatorSpi> services = operatorSpiRegistry.getServiceRegistry().getServices();
        for (OperatorSpi service : services) {
            LOG.info(String.format("CLA##############GPF operator SPI: %s (alias '%s')", service.getClass(), service.getOperatorAlias()));
        }
    }

}
