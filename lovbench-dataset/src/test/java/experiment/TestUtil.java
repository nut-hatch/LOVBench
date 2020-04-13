package experiment;

import experiment.configuration.ExperimentConfiguration;
import experiment.repository.triplestore.connector.JenaConnector;

public class TestUtil {

    String nqFile = getClass().getClassLoader().getResource("lov.nq").getFile().toString();

    public void setNqFileConfiguration() {
        ExperimentConfiguration.getInstance().setLovNqFile(nqFile);
        ExperimentConfiguration.getInstance().getRepository().setConnector(new JenaConnector(nqFile));
        ExperimentConfiguration.getInstance().getRepositoryMetadata().setConnector(new JenaConnector(nqFile));
    }

}
