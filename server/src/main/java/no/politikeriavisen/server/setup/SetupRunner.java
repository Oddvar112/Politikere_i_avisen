package no.politikeriavisen.server.setup;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SetupRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupRunner.class);

    private static final Path CACHE = Path.of(System.getProperty("user.home"), ".cache", "news-analyzer");
    private static final Path MODEL_ONNX = CACHE.resolve("sentiment-model/model.onnx");
    private static final Path MALTPARSER_MODEL = CACHE.resolve("maltparser/norsk-bokmaal.mco");
    private static final String SENTIMENT_MODEL = "cardiffnlp/twitter-xlm-roberta-base-sentiment-multilingual";

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        kjørSetupHvisMangler("MaltParser", MALTPARSER_MODEL, "setup/setup_maltparser.py");
        kjørSetupHvisMangler("Sentimentmodell", MODEL_ONNX, "setup/export_model.py", SENTIMENT_MODEL);
    }

    private void kjørSetupHvisMangler(final String navn, final Path sjekk,
            final String... kommando) throws Exception {
        if (Files.exists(sjekk)) {
            LOGGER.info("{} funnet, hopper over setup.", navn);
            return;
        }

        Path script = Path.of(kommando[0]);
        if (!Files.exists(script)) {
            LOGGER.error("Finner ikke {}, sentimentanalyse vil ikke fungere.", script.toAbsolutePath());
            return;
        }

        LOGGER.info("{} ikke funnet, kjører {}...", navn, script);
        String[] fullKommando = new String[kommando.length + 1];
        fullKommando[0] = "python3";
        System.arraycopy(kommando, 0, fullKommando, 1, kommando.length);

        Process process = new ProcessBuilder(fullKommando)
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            LOGGER.error("{} setup feilet med exit-kode {}.", navn, exitCode);
        } else {
            LOGGER.info("{} klar.", navn);
        }
    }
}
