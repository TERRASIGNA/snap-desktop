package org.esa.snap.clp;

import org.esa.beam.framework.gpf.main.GPT;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Marco Peters
 */
@ServiceProvider(service = OptionProcessor.class)
@NbBundle.Messages({"MSG_GPT=Invokes the Graph Processing Tool"})
public class ClParser extends OptionProcessor {

    private Option gptOption = Option.shortDescription(Option.withoutArgument('g', "gpt"), "org.esa.snap.clp.Bundle", "MSG_GPT");
    private Option defaultOptions = Option.defaultArguments();

    @Override
    protected Set<Option> getOptions() {
        HashSet<Option> options = new HashSet<>();
        options.add(gptOption);
        options.add(defaultOptions);
        return options;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        env.usage();
        if (optionValues.containsKey(gptOption)) {
            try {
                Stream<String> argStream = Arrays.stream(optionValues.get(defaultOptions));
                String[] gptArgs = argStream.map(s -> s.replaceFirst("#", "-")).toArray(String[]::new);
                GPT.run(gptArgs);
            } catch (Exception e) {
                throw new CommandException(12345, e.getMessage());
            }
        }
    }

}
