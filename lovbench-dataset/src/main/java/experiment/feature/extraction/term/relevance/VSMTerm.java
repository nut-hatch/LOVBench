package experiment.feature.extraction.term.relevance;

import experiment.feature.extraction.term.importance.AbstractTermImportanceFeature;
import experiment.feature.scoring.TFIDFScorer;
import experiment.model.Ontology;
import experiment.model.Term;
import experiment.model.query.AbstractQuery;
import experiment.model.query.TermQuery;
import experiment.repository.triplestore.AbstractOntologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VSMTerm extends AbstractTermRelevanceFeature {

    TFIDFScorer tfidfScorer;

    private static final Logger log = LoggerFactory.getLogger( VSMTerm.class );

    public VSMTerm(AbstractOntologyRepository repository, TFIDFScorer tfidfScorer) {
        super(repository);
        this.tfidfScorer = tfidfScorer;
    }

    @Override
    public double getScore(TermQuery query, Term term) {
//        double vsm = 0.0;
//        Ontology ontology = new Ontology(term.getOntologyUriOfTerm());
//
//        // get maximum frequency of words in searchWords
//        int maxSearchWordFrequency = 0;
//        for(String searchWord: query.getSearchWords()) {
//            int searchWordFrequency = Collections.frequency(query.getSearchWords(), searchWord);
//            if (searchWordFrequency > maxSearchWordFrequency) {
//                maxSearchWordFrequency = searchWordFrequency;
//            }
//        }
//
//        // compute sub scores per query term
//        double queryNorm = 0.0;
//        double tfidfTerm = this.tfidfScorer.tf(term, ontology) * this.tfidfScorer.idf(term);;
//
//        for(String searchWord: query.getSearchWords()) {
//            double tfidfQuery = ( Collections.frequency(query.getSearchWords(), searchWord) / maxSearchWordFrequency ) * Math.log(this.repository.countOntologies() / matchedTermsPerOntology.keySet().size());
//
//            queryNorm += Math.pow(tfidfQuery,2);
//            log.info("tf-idf term: "+ tfidfTerm);
//            log.info("tf-idf query: "+ tfidfQuery);
//            vsm += (tfidfTerm * tfidfQuery);
//
//        }
//        queryNorm = Math.sqrt(queryNorm);
//        log.info("query norm: "+ queryNorm);
//        double ontologyNorm = this.tfidfScorer.getOntologyNorm(ontology);
//        log.info("ontology norm: "+ ontologyNorm);
//
//        // compute final vsm similarity score
//        log.info("vsm sum only: "+ vsm);
//        vsm /= (ontologyNorm * queryNorm);
//        log.info("vsm: "+ vsm);
//
//        if (vsm > 2) {
//            System.out.println("test");
//        }
//        return vsm;
        return 0.0;
    }

    @Override
    public String getFeatureName() {
        return "VSM_Term";
    }
}
