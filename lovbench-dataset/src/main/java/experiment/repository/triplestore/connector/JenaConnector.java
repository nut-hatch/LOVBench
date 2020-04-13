package experiment.repository.triplestore.connector;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import experiment.repository.file.LOVPrefixes;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JenaConnector extends AbstractFileConnector {

    private Dataset dataset;
    
    private static final Logger log = LoggerFactory.getLogger( JenaConnector.class );

    public JenaConnector(String filename) {
        super(filename);
        this.dataset = RDFDataMgr.loadDataset(filename, Lang.NQUADS);
    }

    @Override
    public List<BindingSet> selectQuery(String sparql, boolean appendPrefix) {
        log.debug(sparql);
        if (appendPrefix) {
            sparql = this.prepandPrefixStatements(sparql);
        }
        Query query = QueryFactory.create(sparql);
        QueryExecution qe = QueryExecutionFactory.create(query, dataset);
        ResultSet rs = qe.execSelect();
        return this.resultSetToBindingSetList(rs);
    }

    public List<BindingSet> resultSetToBindingSetList(ResultSet rs) {
        List list = new ArrayList();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            MapBindingSet bs = new MapBindingSet();
            Iterator<String> varNames = qs.varNames();
            while (varNames.hasNext()) {
                String varname = varNames.next();
                RDFNode node = qs.get(varname);
                Value value = this.getValueForRDFNode(node);
                bs.addBinding(varname, value);
            }
            list.add(bs);
        }
        return list;
    }

    public Value getValueForRDFNode(RDFNode node) {
        return (Value)node.visitWith(new RDFVisitor() {
            @Override
            public Object visitBlank(Resource r, AnonId id) {
                return SimpleValueFactory.getInstance().createBNode(id.getLabelString());
            }

            @Override
            public Object visitURI(Resource r, String uri) {
                return SimpleValueFactory.getInstance().createIRI(uri);
            }

            @Override
            public Object visitLiteral(Literal l) {

//                return SimpleValueFactory.getInstance().createLiteral(l.getValue().toString(), SimpleValueFactory.getInstance().createIRI(l.getDatatype().getURI()));
                return SimpleValueFactory.getInstance().createLiteral(l.getValue().toString());
            }
        });
    }

//    public String handleNamedGraph(String query) {
//        if (query.toLowerCase().contains("graph <") || query.toLowerCase().contains("graph ?g")) {
//            return query;
//        } else {
//            String allGraphQuery =
//        }
//    }

    public String prepandPrefixStatements(String query) {
        return LOVPrefixes.getInstance().getSparqlPrefixString() + query;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

//    String prefixes = null;
//    "PREFIX SAN: <http://www.irit.fr/recherches/MELODI/ontologies/SAN>\n" +
//            "PREFIX acco: <http://purl.org/acco/ns>\n" +
//            "PREFIX acl: <http://www.w3.org/ns/auth/acl>\n" +
//            "PREFIX acm: <http://www.rkbexplorer.com/ontologies/acm>\n" +
//            "PREFIX acrt: <http://privatealpha.com/ontology/certification/1#>\n" +
//            "PREFIX adms: <http://www.w3.org/ns/adms>\n" +
//            "PREFIX af: <http://purl.org/ontology/af/>\n" +
//            "PREFIX agls: <http://www.agls.gov.au/agls/terms>\n" +
//            "PREFIX agr: <http://promsns.org/def/agr>\n" +
//            "PREFIX agrelon: <http://d-nb.info/standards/elementset/agrelon>\n" +
//            "PREFIX aiiso: <http://purl.org/vocab/aiiso/schema>\n" +
//            "PREFIX airs: <https://raw.githubusercontent.com/airs-linked-data/lov/latest/src/airs_vocabulary.ttl#>\n" +
//            "PREFIX akt: <http://www.aktors.org/ontology/portal>\n" +
//            "PREFIX akts: <http://www.aktors.org/ontology/support>\n" +
//            "PREFIX algo: <http://securitytoolbox.appspot.com/securityAlgorithms#>\n" +
//            "PREFIX am: <http://open-services.net/ns/asset#>\n" +
//            "PREFIX aml: <https://w3id.org/i40/aml>\n" +
//            "PREFIX ao: <http://purl.org/ontology/ao/core#>\n" +
//            "PREFIX aos: <http://rdf.muninn-project.org/ontologies/appearances>\n" +
//            "PREFIX api: <http://purl.org/linked-data/api/vocab#>\n" +
//            "PREFIX apps4X: <http://semweb.mmlab.be/ns/apps4X>\n" +
//            "PREFIX arch: <http://purl.org/archival/vocab/arch>\n" +
//            "PREFIX arco: <https://w3id.org/arco/ontology/core>\n" +
//            "PREFIX arp: <http://www.arpenteur.org/ontology/Arpenteur.owl>\n" +
//            "PREFIX atd: <https://data.nasa.gov/ontologies/atmonto/data#>\n" +
//            "PREFIX atm: <https://data.nasa.gov/ontologies/atmonto/ATM#>\n" +
//            "PREFIX atts: <https://data.nasa.gov/ontologies/atmonto/general#>\n" +
//            "PREFIX awol: <http://bblfish.net/work/atom-owl/2006-06-06/>\n" +
//            "PREFIX aws: <http://purl.oclc.org/NET/ssnx/meteo/aws>\n" +
//            "PREFIX bag: <http://bag.basisregistraties.overheid.nl/def/bag>\n" +
//            "PREFIX basic: <http://def.seegrid.csiro.au/isotc211/iso19103/2005/basic>\n" +
//            "PREFIX bbc: <http://www.bbc.co.uk/ontologies/bbc>\n" +
//            "PREFIX bbccms: <http://www.bbc.co.uk/ontologies/cms>\n" +
//            "PREFIX bbccore: <http://www.bbc.co.uk/ontologies/coreconcepts>\n" +
//            "PREFIX bbcprov: <http://www.bbc.co.uk/ontologies/provenance>\n" +
//            "PREFIX bci: <https://w3id.org/BCI-ontology>\n" +
//            "PREFIX being: <http://contextus.net/ontology/ontomedia/ext/common/being#>\n" +
//            "PREFIX bevon: <http://rdfs.co/bevon/>\n" +
//            "PREFIX bf: <http://id.loc.gov/ontologies/bibframe/>\n" +
//            "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
//            "PREFIX bibtex: <http://purl.org/net/nknouf/ns/bibtex>\n" +
//            "PREFIX bio: <http://purl.org/vocab/bio/0.1/>\n" +
//            "PREFIX biol: <http://purl.org/NET/biol/ns#>\n" +
//            "PREFIX biopax: <http://www.biopax.org/release/biopax-level3.owl>\n" +
//            "PREFIX biotop: <http://purl.org/biotop/biotop.owl>\n" +
//            "PREFIX biro: <http://purl.org/spar/biro>\n" +
//            "PREFIX blt: <http://www.bl.uk/schemas/bibliographic/blterms>\n" +
//            "PREFIX bot: <http://swa.cefriel.it/ontologies/botdcat-ap>\n" +
//            "PREFIX botany: <http://purl.org/NET/biol/botany#>\n" +
//            "PREFIX bperson: <http://data.vlaanderen.be/ns/persoon>\n" +
//            "PREFIX br: <http://vocab.deri.ie/br>\n" +
//            "PREFIX brk: <http://brk.basisregistraties.overheid.nl/def/brk>\n" +
//            "PREFIX brt: <http://brt.basisregistraties.overheid.nl/def/top10nl>\n" +
//            "PREFIX bto: <https://w3id.org/bot>\n" +
//            "PREFIX c4n: <http://vocab.deri.ie/c4n>\n" +
//            "PREFIX c4o: <http://purl.org/spar/c4o>\n" +
//            "PREFIX cal: <http://www.w3.org/2002/12/cal/ical>\n" +
//            "PREFIX cart: <http://purl.org/net/cartCoord#>\n" +
//            "PREFIX cbo: <http://comicmeta.org/cbo/>\n" +
//            "PREFIX cbs: <http://betalinkeddata.cbs.nl/def/cbs>\n" +
//            "PREFIX cc: <http://creativecommons.org/ns>\n" +
//            "PREFIX cci: <http://cookingbigdata.com/linkeddata/ccinstances>\n" +
//            "PREFIX cco: <http://purl.org/ontology/cco/core#>\n" +
//            "PREFIX ccp: <http://cookingbigdata.com/linkeddata/ccpricing>\n" +
//            "PREFIX ccr: <http://cookingbigdata.com/linkeddata/ccregions>\n" +
//            "PREFIX ccsla: <http://cookingbigdata.com/linkeddata/ccsla>\n" +
//            "PREFIX cdm: <http://purl.org/twc/ontology/cdm.owl#>\n" +
//            "PREFIX cdtype: <http://purl.org/cld/cdtype/>\n" +
//            "PREFIX ceo: <http://www.ebusiness-unibw.org/ontologies/consumerelectronics/v1>\n" +
//            "PREFIX cerif: <http://www.eurocris.org/ontologies/cerif/1.3>\n" +
//            "PREFIX cert: <http://www.w3.org/ns/auth/cert#>\n" +
//            "PREFIX cff: <http://purl.oclc.org/NET/ssnx/cf/cf-feature>\n" +
//            "PREFIX cfp: <http://purl.oclc.org/NET/ssnx/cf/cf-property>\n" +
//            "PREFIX cfrl: <http://linkeddata.finki.ukim.mk/lod/ontology/cfrl#>\n" +
//            "PREFIX cgov: <http://reference.data.gov.uk/def/central-government>\n" +
//            "PREFIX chord: <http://purl.org/ontology/chord/>\n" +
//            "PREFIX ci: <https://privatealpha.com/ontology/content-inventory/1#>\n" +
//            "PREFIX cis: <http://dati.beniculturali.it/cultural-ON/cultural-ON.owl>\n" +
//            "PREFIX cito: <http://purl.org/spar/cito>\n" +
//            "PREFIX citof: <http://www.essepuntato.it/2013/03/cito-functions>\n" +
//            "PREFIX cl: <http://advene.org/ns/cinelab/ld>\n" +
//            "PREFIX cld: <http://purl.org/cld/terms/>\n" +
//            "PREFIX cmo: <http://purl.org/twc/ontologies/cmo.owl>\n" +
//            "PREFIX cnt: <http://www.w3.org/2011/content>\n" +
//            "PREFIX co: <http://purl.org/ontology/co/core#>\n" +
//            "PREFIX cocoon: <https://w3id.org/cocoon/v1.0>\n" +
//            "PREFIX cogs: <http://vocab.deri.ie/cogs>\n" +
//            "PREFIX cold: <http://purl.org/configurationontology>\n" +
//            "PREFIX coll: <http://purl.org/co>\n" +
//            "PREFIX comm: <http://vocab.resc.info/communication>\n" +
//            "PREFIX con: <http://www.w3.org/2000/10/swap/pim/contact>\n" +
//            "PREFIX conversion: <http://purl.org/twc/vocab/conversion/>\n" +
//            "PREFIX coo: <http://purl.org/coo/ns#>\n" +
//            "PREFIX coun: <http://www.daml.org/2001/09/countries/iso-3166-ont>\n" +
//            "PREFIX cpa: <http://www.ontologydesignpatterns.org/schemas/cpannotationschema.owl>\n" +
//            "PREFIX crm: <http://www.cidoc-crm.org/cidoc-crm/>\n" +
//            "PREFIX cro: <http://rhizomik.net/ontologies/copyrightonto.owl>\n" +
//            "PREFIX crsw: <http://courseware.rkbexplorer.com/ontologies/courseware>\n" +
//            "PREFIX cs: <http://purl.org/vocab/changeset/schema>\n" +
//            "PREFIX csp: <http://vocab.deri.ie/csp>\n" +
//            "PREFIX csvw: <http://www.w3.org/ns/csvw#>\n" +
//            "PREFIX ct: <http://www.tele.pw.edu.pl/~sims-onto/ConnectivityType.owl>\n" +
//            "PREFIX ctag: <http://commontag.org/ns#>\n" +
//            "PREFIX ctorg: <http://purl.org/ctic/infraestructuras/organizacion>\n" +
//            "PREFIX ctrl: <https://w3id.org/ibp/CTRLont>\n" +
//            "PREFIX ctxdesc: <http://www.demcare.eu/ontologies/contextdescriptor.owl>\n" +
//            "PREFIX cwmo: <http://purl.org/cwmo/#>\n" +
//            "PREFIX cwork: <http://www.bbc.co.uk/ontologies/creativework>\n" +
//            "PREFIX cwrc: <http://sparql.cwrc.ca/ontologies/cwrc>\n" +
//            "PREFIX d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1>\n" +
//            "PREFIX dady: <http://vocab.deri.ie/dady>\n" +
//            "PREFIX daia: <http://purl.org/ontology/daia>\n" +
//            "PREFIX daq: <http://purl.org/eis/vocab/daq#>\n" +
//            "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
//            "PREFIX date: <http://contextus.net/ontology/ontomedia/misc/date#>\n" +
//            "PREFIX datex: <http://vocab.datex.org/terms#>\n" +
//            "PREFIX dave: <http://theme-e.adaptcentre.ie/dave/dave.ttl>\n" +
//            "PREFIX dbm: <http://purl.org/net/dbm/ontology#>\n" +
//            "PREFIX dbowl: <http://ontology.cybershare.utep.edu/dbowl>\n" +
//            "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n" +
//            "PREFIX dbug: <http://ontologi.es/doap-bugs#>\n" +
//            "PREFIX dcam: <http://purl.org/dc/dcam/>\n" +
//            "PREFIX dcat: <http://www.w3.org/ns/dcat>\n" +
//            "PREFIX dce: <http://purl.org/dc/elements/1.1/>\n" +
//            "PREFIX dcite: <http://purl.org/spar/datacite>\n" +
//            "PREFIX dcndl: <http://ndl.go.jp/dcndl/terms/>\n" +
//            "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
//            "PREFIX dctype: <http://purl.org/dc/dcmitype/>\n" +
//            "PREFIX decision: <https://decision-ontology.googlecode.com/svn/trunk/decision.owl>\n" +
//            "PREFIX demlab: <http://www.demcare.eu/ontologies/demlab.owl>\n" +
//            "PREFIX deo: <http://purl.org/spar/deo>\n" +
//            "PREFIX deps: <http://ontologi.es/doap-deps#>\n" +
//            "PREFIX dicom: <http://purl.org/healthcarevocab/v1>\n" +
//            "PREFIX dio: <https://w3id.org/dio>\n" +
//            "PREFIX disco: <http://rdf-vocabulary.ddialliance.org/discovery>\n" +
//            "PREFIX dita: <http://purl.org/dita/ns#>\n" +
//            "PREFIX dk: <http://www.data-knowledge.org/dk/>\n" +
//            "PREFIX dm2e: <http://onto.dm2e.eu/schemas/dm2e>\n" +
//            "PREFIX dnbt: <http://d-nb.info/standards/elementset/dnb>\n" +
//            "PREFIX doap: <http://usefulinc.com/ns/doap#>\n" +
//            "PREFIX doc: <http://www.w3.org/2000/10/swap/pim/doc>\n" +
//            "PREFIX doco: <http://purl.org/spar/doco>\n" +
//            "PREFIX docso: <http://purl.org/ontology/dso>\n" +
//            "PREFIX dogont: <http://elite.polito.it/ontologies/dogont.owl>\n" +
//            "PREFIX donto: <http://reference.data.gov.au/def/ont/dataset>\n" +
//            "PREFIX dpn: <http://purl.org/dpn>\n" +
//            "PREFIX dprov: <http://promsns.org/def/decprov>\n" +
//            "PREFIX dq: <http://def.seegrid.csiro.au/isotc211/iso19115/2003/dataquality>\n" +
//            "PREFIX dqc: <http://semwebquality.org/ontologies/dq-constraints>\n" +
//            "PREFIX dqm: <http://purl.org/dqm-vocabulary/v1/dqm>\n" +
//            "PREFIX dqv: <http://www.w3.org/ns/dqv>\n" +
//            "PREFIX dr: <http://purl.org/swan/2.0/discourse-relationships/>\n" +
//            "PREFIX drm: <http://vocab.data.gov/def/drm>\n" +
//            "PREFIX ds: <http://purl.org/ctic/dcat#>\n" +
//            "PREFIX dsn: <http://purl.org/dsnotify/vocab/eventset/>\n" +
//            "PREFIX dso: <http://inference-web.org/2.0/ds.owl>\n" +
//            "PREFIX dtype: <http://www.linkedmodel.org/schema/dtype>\n" +
//            "PREFIX dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl>\n" +
//            "PREFIX duv: <http://www.w3.org/ns/duv>\n" +
//            "PREFIX dvia: <http://purl.org/ontology/dvia>\n" +
//            "PREFIX eac-cpf: <http://archivi.ibc.regione.emilia-romagna.it/ontology/eac-cpf/>\n" +
//            "PREFIX earl: <http://www.w3.org/ns/earl>\n" +
//            "PREFIX earth: <http://linked.earth/ontology#>\n" +
//            "PREFIX ebucore: <http://www.ebu.ch/metadata/ontologies/ebucore/ebucore>\n" +
//            "PREFIX eccrev: <https://vocab.eccenca.com/revision/>\n" +
//            "PREFIX eclap: <http://www.eclap.eu/schema/eclap/>\n" +
//            "PREFIX ecpo: <http://purl.org/ontology/ecpo>\n" +
//            "PREFIX ecrm: <http://erlangen-crm.org/current/>\n" +
//            "PREFIX edac: <http://ontology.cybershare.utep.edu/ELSEWeb/elseweb-edac.owl>\n" +
//            "PREFIX edm: <http://www.europeana.eu/schemas/edm/>\n" +
//            "PREFIX edupro: <http://ns.inria.fr/semed/eduprogression/>\n" +
//            "PREFIX eem: <http://purl.org/eem>\n" +
//            "PREFIX eepsa: <https://w3id.org/eepsa>\n" +
//            "PREFIX ei2a: <https://opendata.aragon.es/def/ei2a/ei2a.owl>\n" +
//            "PREFIX elec: <http://purl.org/ctic/sector-publico/elecciones>\n" +
//            "PREFIX eli: <http://data.europa.eu/eli/ontology>\n" +
//            "PREFIX emotion: <http://ns.inria.fr/emoca>\n" +
//            "PREFIX emp: <http://purl.org/ctic/empleo/oferta>\n" +
//            "PREFIX ends: <http://labs.mondeca.com/vocab/endpointStatus>\n" +
//            "PREFIX ep: <http://eprints.org/ontology/>\n" +
//            "PREFIX eppl: <https://w3id.org/ep-plan>\n" +
//            "PREFIX eqp: <https://data.nasa.gov/ontologies/atmonto/equipment#>\n" +
//            "PREFIX essglobal: <http://purl.org/essglobal/vocab/>\n" +
//            "PREFIX eupont: <http://elite.polito.it/ontologies/eupont.owl>\n" +
//            "PREFIX event: <http://purl.org/NET/c4dm/event.owl>\n" +
//            "PREFIX ex: <http://purl.org/net/ns/ex>\n" +
//            "PREFIX exif: <http://www.w3.org/2003/12/exif/ns>\n" +
//            "PREFIX ext: <http://def.seegrid.csiro.au/isotc211/iso19115/2003/extent>\n" +
//            "PREFIX fabio: <http://purl.org/spar/fabio>\n" +
//            "PREFIX faldo: <http://biohackathon.org/resource/faldo>\n" +
//            "PREFIX fea: <http://vocab.data.gov/def/fea>\n" +
//            "PREFIX fiesta-iot: <http://purl.org/iot/ontology/fiesta-iot>\n" +
//            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
//            "PREFIX foio: <https://w3id.org/seas/FeatureOfInterestOntology>\n" +
//            "PREFIX food: <http://data.lirmm.fr/ontologies/food>\n" +
//            "PREFIX fowl: <http://www.w3.org/TR/2003/PR-owl-guide-20031215/food>\n" +
//            "PREFIX frad: <http://iflastandards.info/ns/fr/frad/>\n" +
//            "PREFIX frapo: <http://purl.org/cerif/frapo/>\n" +
//            "PREFIX frappe: <http://streamreasoning.org/ontologies/frappe#>\n" +
//            "PREFIX frbr: <http://purl.org/vocab/frbr/core>\n" +
//            "PREFIX frbre: <http://purl.org/vocab/frbr/extended>\n" +
//            "PREFIX frbrer: <http://iflastandards.info/ns/fr/frbr/frbrer/>\n" +
//            "PREFIX fresnel: <http://www.w3.org/2004/09/fresnel>\n" +
//            "PREFIX g50k: <http://data.ordnancesurvey.co.uk/ontology/50kGazetteer/>\n" +
//            "PREFIX game: <http://data.totl.net/game/>\n" +
//            "PREFIX gc: <http://www.oegov.org/core/owl/gc>\n" +
//            "PREFIX gci: <http://ontology.eil.utoronto.ca/GCI/Foundation/GCI-Foundation.owl>\n" +
//            "PREFIX gcon: <https://w3id.org/GConsent>\n" +
//            "PREFIX gd: <http://vocab.data.gov/gd>\n" +
//            "PREFIX gdprov: <https://w3id.org/GDPRov>\n" +
//            "PREFIX gdprt: <https://w3id.org/GDPRtEXT>\n" +
//            "PREFIX gen: <http://purl.org/gen/0.1#>\n" +
//            "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos>\n" +
//            "PREFIX geod: <http://vocab.lenka.no/geo-deling>\n" +
//            "PREFIX geof: <http://www.mindswap.org/2003/owl/geo/geoFeatures20040307.owl>\n" +
//            "PREFIX geofla: <http://data.ign.fr/def/geofla>\n" +
//            "PREFIX geom: <http://data.ign.fr/def/geometrie>\n" +
//            "PREFIX geop: <http://aims.fao.org/aos/geopolitical.owl>\n" +
//            "PREFIX geosp: <http://rdf.geospecies.org/ont/geospecies>\n" +
//            "PREFIX gf: <http://def.seegrid.csiro.au/isotc211/iso19109/2005/feature>\n" +
//            "PREFIX gm: <http://def.seegrid.csiro.au/isotc211/iso19107/2003/geometry>\n" +
//            "PREFIX gml: <http://www.opengis.net/ont/gml>\n" +
//            "PREFIX gn: <http://www.geonames.org/ontology>\n" +
//            "PREFIX gndo: <http://d-nb.info/standards/elementset/gnd#>\n" +
//            "PREFIX gold: <http://purl.org/linguistics/gold>\n" +
//            "PREFIX gov: <http://gov.genealogy.net/ontology.owl>\n" +
//            "PREFIX gr: <http://purl.org/goodrelations/v1>\n" +
//            "PREFIX grddl: <http://www.w3.org/2003/g/data-view>\n" +
//            "PREFIX gso: <http://www.w3.org/2006/gen/ont>\n" +
//            "PREFIX gsp: <http://www.opengis.net/ont/geosparql>\n" +
//            "PREFIX gtfs: <http://vocab.gtfs.org/terms#>\n" +
//            "PREFIX gts: <http://resource.geosciml.org/ontology/timescale/gts>\n" +
//            "PREFIX gvp: <http://vocab.getty.edu/ontology>\n" +
//            "PREFIX h2o: <http://def.seegrid.csiro.au/isotc211/iso19150/-2/2012/basic>\n" +
//            "PREFIX ha: <http://sensormeasurement.appspot.com/ont/home/homeActivity#>\n" +
//            "PREFIX hdo: <http://www.samos.gr/ontologies/helpdeskOnto.owl>\n" +
//            "PREFIX hifm: <http://purl.org/net/hifm/ontology#>\n" +
//            "PREFIX holding: <http://purl.org/ontology/holding>\n" +
//            "PREFIX hosp: <http://vocab.data.gov/hosp>\n" +
//            "PREFIX hr: <http://iserve.kmi.open.ac.uk/ns/hrests>\n" +
//            "PREFIX hto: <http://vcharpenay.github.io/hto/hto.xml>\n" +
//            "PREFIX http: <http://www.w3.org/2011/http>\n" +
//            "PREFIX hw: <https://www.auto.tuwien.ac.at/downloads/thinkhome/ontology/WeatherOntology.owl>\n" +
//            "PREFIX hydra: <http://www.w3.org/ns/hydra/core>\n" +
//            "PREFIX ibis: <https://privatealpha.com/ontology/ibis/1#>\n" +
//            "PREFIX ic: <http://ontology.eil.utoronto.ca/icontact.owl>\n" +
//            "PREFIX idemo: <http://rdf.insee.fr/def/demo>\n" +
//            "PREFIX identity: <http://www.identity.org/ontologies/identity.owl>\n" +
//            "PREFIX igeo: <http://rdf.insee.fr/def/geo>\n" +
//            "PREFIX ignf: <http://data.ign.fr/def/ignf>\n" +
//            "PREFIX imo: <http://imgpedia.dcc.uchile.cl/ontology>\n" +
//            "PREFIX incident: <http://vocab.resc.info/incident>\n" +
//            "PREFIX infor: <http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl>\n" +
//            "PREFIX inno: <http://purl.org/innovation/ns>\n" +
//            "PREFIX interval: <http://reference.data.gov.uk/def/intervals>\n" +
//            "PREFIX iol: <http://www.ontologydesignpatterns.org/ont/dul/IOLite.owl>\n" +
//            "PREFIX iot-lite: <http://purl.oclc.org/NET/UNIS/fiware/iot-lite#>\n" +
//            "PREFIX ioto: <http://www.irit.fr/recherches/MELODI/ontologies/IoT-O>\n" +
//            "PREFIX ipo: <http://purl.org/ipo/core>\n" +
//            "PREFIX irw: <http://www.ontologydesignpatterns.org/ont/web/irw.owl>\n" +
//            "PREFIX is: <http://purl.org/ontology/is/core#>\n" +
//            "PREFIX isbd: <http://iflastandards.info/ns/isbd/elements/>\n" +
//            "PREFIX iso-thes: <http://purl.org/iso25964/skos-thes>\n" +
//            "PREFIX iso37120: <http://ontology.eil.utoronto.ca/ISO37120.owl>\n" +
//            "PREFIX isoadr: <http://reference.data.gov.au/def/ont/iso19160-1-address>\n" +
//            "PREFIX ispra: <http://dati.isprambiente.it/ontology/core#>\n" +
//            "PREFIX istex: <https://data.istex.fr/ontology/istex#>\n" +
//            "PREFIX itm: <http://spi-fm.uca.es/spdef/models/genericTools/itm/1.0>\n" +
//            "PREFIX itsmo: <http://ontology.it/itsmo/v1>\n" +
//            "PREFIX jup: <http://w3id.org/charta77/jup>\n" +
//            "PREFIX juso: <http://rdfs.co/juso/>\n" +
//            "PREFIX juso.kr: <http://rdfs.co/juso/kr/>\n" +
//            "PREFIX kdo: <http://kdo.render-project.eu/kdo#>\n" +
//            "PREFIX kees: <http://linkeddata.center/kees/v1>\n" +
//            "PREFIX keys: <http://purl.org/NET/c4dm/keys.owl>\n" +
//            "PREFIX km4c: <http://www.disit.org/km4city/schema>\n" +
//            "PREFIX label: <http://purl.org/net/vocab/2004/03/label>\n" +
//            "PREFIX lawd: <http://lawd.info/ontology/>\n" +
//            "PREFIX lcy: <http://purl.org/vocab/lifecycle/schema>\n" +
//            "PREFIX ldp: <http://www.w3.org/ns/ldp#>\n" +
//            "PREFIX ldr: <http://purl.oclc.org/NET/ldr/ns#>\n" +
//            "PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>\n" +
//            "PREFIX lemon: <http://lemon-model.net/lemon>\n" +
//            "PREFIX lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo>\n" +
//            "PREFIX lgdo: <http://linkedgeodata.org/ontology>\n" +
//            "PREFIX li: <http://def.seegrid.csiro.au/isotc211/iso19115/2003/lineage>\n" +
//            "PREFIX lib: <http://purl.org/library/>\n" +
//            "PREFIX lifecycle: <http://www.irit.fr/recherches/MELODI/ontologies/IoT-Lifecycle>\n" +
//            "PREFIX limo: <http://purl.org/limo-ontology/limo/>\n" +
//            "PREFIX limoo: <http://purl.org/LiMo/0.1#>\n" +
//            "PREFIX lingvo: <http://www.lingvoj.org/ontology>\n" +
//            "PREFIX lio: <http://purl.org/net/lio>\n" +
//            "PREFIX llont: <http://www.linklion.org/ontology>\n" +
//            "PREFIX lmm1: <http://www.ontologydesignpatterns.org/ont/lmm/LMM_L1.owl>\n" +
//            "PREFIX lmm2: <http://www.ontologydesignpatterns.org/ont/lmm/LMM_L2.owl>\n" +
//            "PREFIX loc: <http://purl.org/ctic/infraestructuras/localizacion>\n" +
//            "PREFIX locah: <http://data.archiveshub.ac.uk/def/>\n" +
//            "PREFIX locn: <http://www.w3.org/ns/locn>\n" +
//            "PREFIX lode: <http://linkedevents.org/ontology/>\n" +
//            "PREFIX log: <http://www.w3.org/2000/10/swap/log>\n" +
//            "PREFIX lom: <http://data.opendiscoveryspace.eu/lom_ontology_ods.owl>\n" +
//            "PREFIX losp: <http://sparql.sstu.ru:3030/speciality/>\n" +
//            "PREFIX loted: <http://loted.eu/ontology>\n" +
//            "PREFIX lsc: <http://linkedscience.org/lsc/ns#>\n" +
//            "PREFIX lslife: <http://ontology.cybershare.utep.edu/ELSEWeb/elseweb-lifemapper.owl>\n" +
//            "PREFIX lsmap: <http://ontology.cybershare.utep.edu/ELSEWeb/mappings/elseweb-mappings.owl>\n" +
//            "PREFIX lsq: <http://lsq.aksw.org/vocab>\n" +
//            "PREFIX lsweb: <http://ontology.cybershare.utep.edu/ELSEWeb/elseweb-data.owl>\n" +
//            "PREFIX lswmo: <http://ontology.cybershare.utep.edu/ELSEWeb/elseweb-modelling.owl>\n" +
//            "PREFIX lswpm: <http://ontology.cybershare.utep.edu/ELSEWeb/elseweb-lifemapper-parameters.owl>\n" +
//            "PREFIX ludo: <http://ns.inria.fr/ludo>\n" +
//            "PREFIX ludo-gm: <http://ns.inria.fr/ludo/v1/gamemodel#>\n" +
//            "PREFIX ludo-gp: <http://ns.inria.fr/ludo/v1/gamepresentation#>\n" +
//            "PREFIX ludo-vc: <http://ns.inria.fr/ludo/v1/virtualcontext#>\n" +
//            "PREFIX ludo-xapi: <http://ns.inria.fr/ludo/v1/xapi>\n" +
//            "PREFIX lv: <http://purl.org/lobid/lv>\n" +
//            "PREFIX lvont: <http://lexvo.org/ontology>\n" +
//            "PREFIX lyou: <http://purl.org/linkingyou/>\n" +
//            "PREFIX m3lite: <http://purl.org/iot/vocab/m3-lite#>\n" +
//            "PREFIX ma-ont: <http://www.w3.org/ns/ma-ont>\n" +
//            "PREFIX mads: <http://www.loc.gov/mads/rdf/v1>\n" +
//            "PREFIX marl: <http://www.gsi.dit.upm.es/ontologies/marl/ns>\n" +
//            "PREFIX maso: <http://securitytoolbox.appspot.com/MASO>\n" +
//            "PREFIX md: <http://def.seegrid.csiro.au/isotc211/iso19115/2003/metadata>\n" +
//            "PREFIX mdi: <https://w3id.org/multidimensional-interface/ontology>\n" +
//            "PREFIX meb: <http://rdf.myexperiment.org/ontologies/base/>\n" +
//            "PREFIX media: <http://purl.org/media>\n" +
//            "PREFIX medred: <http://w3id.org/medred/medred#>\n" +
//            "PREFIX mexalgo: <http://mex.aksw.org/mex-algo>\n" +
//            "PREFIX mexcore: <http://mex.aksw.org/mex-core>\n" +
//            "PREFIX mexperf: <http://mex.aksw.org/mex-perf>\n" +
//            "PREFIX mil: <http://rdf.muninn-project.org/ontologies/military>\n" +
//            "PREFIX mls: <http://www.w3.org/ns/mls>\n" +
//            "PREFIX mo: <http://purl.org/ontology/mo/>\n" +
//            "PREFIX moac: <http://www.observedchange.com/moac/ns#>\n" +
//            "PREFIX moat: <http://moat-project.org/ns#>\n" +
//            "PREFIX mod: <https://www.isibang.ac.in/ns/mod/1.0/mod.owl>\n" +
//            "PREFIX mrel: <http://id.loc.gov/vocabulary/relators>\n" +
//            "PREFIX msm: <http://iserve.kmi.open.ac.uk/ns/msm>\n" +
//            "PREFIX msr: <http://www.telegraphis.net/ontology/measurement/measurement#>\n" +
//            "PREFIX mtlo: <http://www.ics.forth.gr/isl/MarineTLO/v4/marinetlo.owl>\n" +
//            "PREFIX munc: <http://ns.inria.fr/munc/>\n" +
//            "PREFIX mus: <http://data.doremus.org/ontology#>\n" +
//            "PREFIX music: <http://www.kanzaki.com/ns/music>\n" +
//            "PREFIX muto: <http://purl.org/muto/core>\n" +
//            "PREFIX mv: <http://schema.mobivoc.org/>\n" +
//            "PREFIX mvco: <http://purl.oclc.org/NET/mvco.owl>\n" +
//            "PREFIX nao: <http://www.semanticdesktop.org/ontologies/2007/08/15/nao>\n" +
//            "PREFIX nas: <https://data.nasa.gov/ontologies/atmonto/NAS#>\n" +
//            "PREFIX ncal: <http://www.semanticdesktop.org/ontologies/2007/04/02/ncal>\n" +
//            "PREFIX nco: <http://www.semanticdesktop.org/ontologies/2007/03/22/nco>\n" +
//            "PREFIX nfo: <http://www.semanticdesktop.org/ontologies/2007/03/22/nfo>\n" +
//            "PREFIX ngeo: <http://geovocab.org/geometry>\n" +
//            "PREFIX nie: <http://www.semanticdesktop.org/ontologies/2007/01/19/nie>\n" +
//            "PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>\n" +
//            "PREFIX nlon: <http://lod.nl.go.kr/ontology/>\n" +
//            "PREFIX nno: <https://w3id.org/nno/ontology>\n" +
//            "PREFIX npg: <http://ns.nature.com/terms/>\n" +
//            "PREFIX nrl: <http://www.semanticdesktop.org/ontologies/2007/08/15/nrl>\n" +
//            "PREFIX nrv: <http://ns.inria.fr/nrv>\n" +
//            "PREFIX nsl: <http://purl.org/ontology/storyline>\n" +
//            "PREFIX ntag: <http://ns.inria.fr/nicetag/2010/09/09/voc>\n" +
//            "PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
//            "PREFIX oad: <http://culturalis.org/oad#>\n" +
//            "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core>\n" +
//            "PREFIX oan: <http://data.lirmm.fr/ontologies/oan>\n" +
//            "PREFIX obo: <http://purl.obolibrary.org/obo/obi.owl>\n" +
//            "PREFIX obsm: <http://rdf.geospecies.org/methods/observationMethod.rdf>\n" +
//            "PREFIX obws: <http://delicias.dia.fi.upm.es/ontologies/ObjectWithStates.owl>\n" +
//            "PREFIX oc: <http://contextus.net/ontology/ontomedia/core/expression#>\n" +
//            "PREFIX ocd: <http://dati.camera.it/ocd/>\n" +
//            "PREFIX ocds: <http://purl.org/onto-ocds/ocds>\n" +
//            "PREFIX odapp: <http://vocab.deri.ie/odapp>\n" +
//            "PREFIX odapps: <http://semweb.mmlab.be/ns/odapps>\n" +
//            "PREFIX odpart: <http://www.ontologydesignpatterns.org/cp/owl/participation.owl>\n" +
//            "PREFIX odrl: <http://www.w3.org/ns/odrl/2/>\n" +
//            "PREFIX odrs: <http://schema.theodi.org/odrs>\n" +
//            "PREFIX odv: <http://reference.data.gov.uk/def/organogram>\n" +
//            "PREFIX oecc: <http://www.oegov.org/core/owl/cc>\n" +
//            "PREFIX of: <http://owlrep.eu01.aws.af.cm/fridge>\n" +
//            "PREFIX ofrd: <http://purl.org/opdm/refrigerator#>\n" +
//            "PREFIX og: <http://ogp.me/ns>\n" +
//            "PREFIX oh: <http://semweb.mmlab.be/ns/oh>\n" +
//            "PREFIX olca: <http://www.lingvoj.org/olca>\n" +
//            "PREFIX olo: <http://purl.org/ontology/olo/core#>\n" +
//            "PREFIX om: <http://def.seegrid.csiro.au/isotc211/iso19156/2011/observation>\n" +
//            "PREFIX oml: <http://def.seegrid.csiro.au/ontology/om/om-lite>\n" +
//            "PREFIX omn: <http://open-multinet.info/ontology/omn>\n" +
//            "PREFIX omnfed: <http://open-multinet.info/ontology/omn-federation>\n" +
//            "PREFIX omnlc: <http://open-multinet.info/ontology/omn-lifecycle>\n" +
//            "PREFIX onc: <http://www.ics.forth.gr/isl/oncm/core>\n" +
//            "PREFIX ont: <http://purl.org/net/ns/ontology-annot>\n" +
//            "PREFIX ontopic: <http://www.ontologydesignpatterns.org/ont/dul/ontopic.owl>\n" +
//            "PREFIX ontosec: <http://www.semanticweb.org/ontologies/2008/11/OntologySecurity.owl>\n" +
//            "PREFIX onyx: <http://www.gsi.dit.upm.es/ontologies/onyx/ns>\n" +
//            "PREFIX oo: <http://purl.org/openorg/>\n" +
//            "PREFIX op: <http://environment.data.gov.au/def/op>\n" +
//            "PREFIX open311: <http://ontology.eil.utoronto.ca/open311.owl>\n" +
//            "PREFIX opmo: <http://openprovenance.org/model/opmo>\n" +
//            "PREFIX opmv: <http://purl.org/net/opmv/ns#>\n" +
//            "PREFIX opmw: <http://www.opmw.org/ontology/>\n" +
//            "PREFIX opo: <http://online-presence.net/opo/ns#>\n" +
//            "PREFIX opus: <http://lsdis.cs.uga.edu/projects/semdis/opus#>\n" +
//            "PREFIX orca: <http://vocab.deri.ie/orca>\n" +
//            "PREFIX ore: <http://www.openarchives.org/ore/terms/>\n" +
//            "PREFIX org: <http://www.w3.org/ns/org#>\n" +
//            "PREFIX orges: <http://datos.gob.es/def/sector-publico/organizacion#>\n" +
//            "PREFIX osadm: <http://data.ordnancesurvey.co.uk/ontology/admingeo/>\n" +
//            "PREFIX osgeom: <http://data.ordnancesurvey.co.uk/ontology/geometry/>\n" +
//            "PREFIX oslc: <http://open-services.net/ns/core#>\n" +
//            "PREFIX oslo: <http://purl.org/oslo/ns/localgov>\n" +
//            "PREFIX osp: <http://data.lirmm.fr/ontologies/osp>\n" +
//            "PREFIX osr: <http://contextus.net/ontology/ontomedia/core/space#>\n" +
//            "PREFIX osspr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/>\n" +
//            "PREFIX ostop: <http://www.ordnancesurvey.co.uk/ontology/Topography/v0.1/Topography.owl>\n" +
//            "PREFIX otl: <https://w3id.org/opentrafficlights>\n" +
//            "PREFIX ov: <http://open.vocab.org/terms>\n" +
//            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
//            "PREFIX p-plan: <http://purl.org/net/p-plan#>\n" +
//            "PREFIX parl: <http://reference.data.gov.uk/def/parliament>\n" +
//            "PREFIX part: <http://purl.org/vocab/participation/schema>\n" +
//            "PREFIX passim: <http://data.lirmm.fr/ontologies/passim>\n" +
//            "PREFIX pat: <http://purl.org/hpi/patchr#>\n" +
//            "PREFIX pattern: <http://www.essepuntato.it/2008/12/pattern>\n" +
//            "PREFIX pav: <http://purl.org/pav/>\n" +
//            "PREFIX pay: <http://reference.data.gov.uk/def/payment#>\n" +
//            "PREFIX pbo: <http://purl.org/ontology/pbo/core#>\n" +
//            "PREFIX pc: <http://purl.org/procurement/public-contracts>\n" +
//            "PREFIX pdo: <http://vocab.deri.ie/pdo>\n" +
//            "PREFIX pep: <https://w3id.org/pep/>\n" +
//            "PREFIX person: <http://www.w3.org/ns/person>\n" +
//            "PREFIX pext: <http://www.ontotext.com/proton/protonext>\n" +
//            "PREFIX phdd: <http://rdf-vocabulary.ddialliance.org/phdd>\n" +
//            "PREFIX place: <http://purl.org/ontology/places>\n" +
//            "PREFIX plink: <http://cedric.cnam.fr/isid/ontologies/PersonLink.owl>\n" +
//            "PREFIX plo: <http://purl.org/net/po#>\n" +
//            "PREFIX pmlp: <http://inference-web.org/2.0/pml-provenance.owl>\n" +
//            "PREFIX pmofn: <http://premon.fbk.eu/ontology/fn>\n" +
//            "PREFIX pmonb: <http://premon.fbk.eu/ontology/nb>\n" +
//            "PREFIX pmopb: <http://premon.fbk.eu/ontology/pb>\n" +
//            "PREFIX pmovn: <http://premon.fbk.eu/ontology/vn>\n" +
//            "PREFIX pna: <http://data.press.net/ontology/asset/>\n" +
//            "PREFIX pnc: <http://data.press.net/ontology/classification/>\n" +
//            "PREFIX pne: <http://data.press.net/ontology/event/>\n" +
//            "PREFIX pni: <http://data.press.net/ontology/identifier/>\n" +
//            "PREFIX pns: <http://data.press.net/ontology/stuff/>\n" +
//            "PREFIX pnt: <http://data.press.net/ontology/tag/>\n" +
//            "PREFIX po: <http://purl.org/ontology/po/>\n" +
//            "PREFIX poder: <http://dev.poderopedia.com/vocab/schema>\n" +
//            "PREFIX postcode: <http://data.ordnancesurvey.co.uk/ontology/postcode/>\n" +
//            "PREFIX poste: <http://data.lirmm.fr/ontologies/poste>\n" +
//            "PREFIX ppo: <http://vocab.deri.ie/ppo>\n" +
//            "PREFIX pproc: <http://contsem.unizar.es/def/sector-publico/pproc>\n" +
//            "PREFIX pr: <http://purl.org/ontology/prv/core#>\n" +
//            "PREFIX premis: <http://www.loc.gov/premis/rdf/v1>\n" +
//            "PREFIX prissma: <http://ns.inria.fr/prissma/v2#>\n" +
//            "PREFIX pro: <http://purl.org/spar/pro>\n" +
//            "PREFIX prog: <http://purl.org/prog/>\n" +
//            "PREFIX prov: <http://www.w3.org/ns/prov#>\n" +
//            "PREFIX provoc: <http://ns.inria.fr/provoc>\n" +
//            "PREFIX prv: <http://purl.org/net/provenance/ns#>\n" +
//            "PREFIX prvt: <http://purl.org/net/provenance/types#>\n" +
//            "PREFIX pso: <http://purl.org/spar/pso>\n" +
//            "PREFIX ptop: <http://www.ontotext.com/proton/protontop>\n" +
//            "PREFIX pubsub: <https://vocab.eccenca.com/pubsub/>\n" +
//            "PREFIX pwo: <http://purl.org/spar/pwo>\n" +
//            "PREFIX qb: <http://purl.org/linked-data/cube>\n" +
//            "PREFIX qb4o: <http://purl.org/qb4olap/cubes>\n" +
//            "PREFIX qu: <http://purl.oclc.org/NET/ssnx/qu/qu>\n" +
//            "PREFIX qudt: <http://qudt.org/schema/qudt>\n" +
//            "PREFIX r4r: <http://guava.iis.sinica.edu.tw/r4r>\n" +
//            "PREFIX radion: <http://www.w3.org/ns/radion#>\n" +
//            "PREFIX rami: <https://w3id.org/i40/rami/>\n" +
//            "PREFIX raul: <http://vocab.deri.ie/raul>\n" +
//            "PREFIX rdaa: <http://rdaregistry.info/Elements/a>\n" +
//            "PREFIX rdac: <http://rdaregistry.info/Elements/c>\n" +
//            "PREFIX rdae: <http://rdaregistry.info/Elements/e>\n" +
//            "PREFIX rdafrbr: <http://rdvocab.info/uri/schema/FRBRentitiesRDA>\n" +
//            "PREFIX rdag1: <http://rdvocab.info/Elements>\n" +
//            "PREFIX rdag2: <http://rdvocab.info/ElementsGr2>\n" +
//            "PREFIX rdag3: <http://rdvocab.info/ElementsGr3>\n" +
//            "PREFIX rdai: <http://rdaregistry.info/Elements/i>\n" +
//            "PREFIX rdam: <http://rdaregistry.info/Elements/m>\n" +
//            "PREFIX rdarel: <http://rdvocab.info/RDARelationshipsWEMI>\n" +
//            "PREFIX rdarel2: <http://metadataregistry.org/uri/schema/RDARelationshipsGR2>\n" +
//            "PREFIX rdarole: <http://rdvocab.info/roles>\n" +
//            "PREFIX rdau: <http://rdaregistry.info/Elements/u>\n" +
//            "PREFIX rdaw: <http://rdaregistry.info/Elements/w>\n" +
//            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//            "PREFIX rdfa: <http://www.w3.org/ns/rdfa#>\n" +
//            "PREFIX rdfg: <http://www.w3.org/2004/03/trix/rdfg-1/>\n" +
//            "PREFIX rdfp: <https://w3id.org/rdfp/>\n" +
//            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//            "PREFIX rec: <http://purl.org/ontology/rec/core#>\n" +
//            "PREFIX rec54: <http://www.w3.org/2001/02pd/rec54#>\n" +
//            "PREFIX reco: <http://purl.org/reco#>\n" +
//            "PREFIX reegle: <http://reegle.info/schema>\n" +
//            "PREFIX rel: <http://purl.org/vocab/relationship/>\n" +
//            "PREFIX remetca: <http://www.purl.org/net/remetca#>\n" +
//            "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
//            "PREFIX rlog: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/rlog#>\n" +
//            "PREFIX ro: <http://purl.org/wf4ever/ro>\n" +
//            "PREFIX rooms: <http://vocab.deri.ie/rooms>\n" +
//            "PREFIX rov: <http://www.w3.org/ns/regorg>\n" +
//            "PREFIX rr: <http://www.w3.org/ns/r2rml#>\n" +
//            "PREFIX rsctx: <http://softeng.polito.it/rsctx>\n" +
//            "PREFIX rss: <http://purl.org/rss/1.0>\n" +
//            "PREFIX ru: <http://purl.org/imbi/ru-meta.owl>\n" +
//            "PREFIX ruto: <http://rdfunit.aksw.org/ns/core#>\n" +
//            "PREFIX s4ac: <http://ns.inria.fr/s4ac/v2>\n" +
//            "PREFIX s4ee: <https://w3id.org/saref4ee>\n" +
//            "PREFIX sam: <http://def.seegrid.csiro.au/isotc211/iso19156/2011/sampling>\n" +
//            "PREFIX samfl: <http://def.seegrid.csiro.au/ontology/om/sam-lite>\n" +
//            "PREFIX san-lod: <http://dati.san.beniculturali.it/SAN/>\n" +
//            "PREFIX sao: <http://salt.semanticauthoring.org/ontologies/sao>\n" +
//            "PREFIX saref: <https://w3id.org/saref>\n" +
//            "PREFIX saws: <http://purl.org/saws/ontology>\n" +
//            "PREFIX schema: <http://schema.org/>\n" +
//            "PREFIX scip: <http://lod.taxonconcept.org/ontology/sci_people.owl>\n" +
//            "PREFIX scoro: <http://purl.org/spar/scoro/>\n" +
//            "PREFIX scot: <http://rdfs.org/scot/ns#>\n" +
//            "PREFIX scovo: <http://vocab.deri.ie/scovo>\n" +
//            "PREFIX scsv: <http://vocab.deri.ie/scsv>\n" +
//            "PREFIX sd: <http://www.w3.org/ns/sparql-service-description>\n" +
//            "PREFIX sdmx: <http://purl.org/linked-data/sdmx>\n" +
//            "PREFIX sdmx-code: <http://purl.org/linked-data/sdmx/2009/code>\n" +
//            "PREFIX sdmx-dimension: <http://purl.org/linked-data/sdmx/2009/dimension>\n" +
//            "PREFIX sdo: <http://salt.semanticauthoring.org/ontologies/sdo>\n" +
//            "PREFIX search: <http://vocab.deri.ie/search>\n" +
//            "PREFIX seas: <https://w3id.org/seas/>\n" +
//            "PREFIX seas-eval: <https://w3id.org/seas/EvaluationOntology>\n" +
//            "PREFIX seas-op: <https://w3id.org/seas/OperatingOntology>\n" +
//            "PREFIX seas-qudt: <https://w3id.org/seas/QUDTAlignment>\n" +
//            "PREFIX seas-stats: <https://w3id.org/seas/StatisticsOntology>\n" +
//            "PREFIX seas-sys: <https://w3id.org/seas/SystemOntology>\n" +
//            "PREFIX seasb: <https://w3id.org/seas/BatteryOntology>\n" +
//            "PREFIX seasbo: <https://w3id.org/seas/BuildingOntology>\n" +
//            "PREFIX seasd: <https://w3id.org/seas/DeviceOntology>\n" +
//            "PREFIX seasfo: <https://w3id.org/seas/ForecastingOntology>\n" +
//            "PREFIX seast: <https://w3id.org/seas/TimeOntology>\n" +
//            "PREFIX seasto: <https://w3id.org/seas/TradingOntology>\n" +
//            "PREFIX security: <http://securitytoolbox.appspot.com/securityMain>\n" +
//            "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/>\n" +
//            "PREFIX semio: <http://www.lingvoj.org/semio.rdf>\n" +
//            "PREFIX semsur: <http://purl.org/SemSur/>\n" +
//            "PREFIX seo: <http://purl.org/seo/>\n" +
//            "PREFIX seq: <http://www.ontologydesignpatterns.org/cp/owl/sequence.owl>\n" +
//            "PREFIX service: <http://purl.org/ontology/service>\n" +
//            "PREFIX sf: <http://www.opengis.net/ont/sf>\n" +
//            "PREFIX sh: <http://www.w3.org/ns/shacl#>\n" +
//            "PREFIX shoah: <http://dati.cdec.it/lod/shoah/>\n" +
//            "PREFIX shw: <http://paul.staroch.name/thesis/SmartHomeWeather.owl#>\n" +
//            "PREFIX sim: <http://purl.org/ontology/similarity/>\n" +
//            "PREFIX sio: <http://semanticscience.org/ontology/sio.owl>\n" +
//            "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
//            "PREFIX situ: <http://www.ontologydesignpatterns.org/cp/owl/situation.owl>\n" +
//            "PREFIX skos: <http://www.w3.org/2004/02/skos/core>\n" +
//            "PREFIX skosxl: <http://www.w3.org/2008/05/skos-xl>\n" +
//            "PREFIX smg: <http://ns.cerise-project.nl/energy/def/cim-smartgrid>\n" +
//            "PREFIX snarm: <http://rdf.myexperiment.org/ontologies/snarm/>\n" +
//            "PREFIX solid: <http://www.w3.org/ns/solid/terms>\n" +
//            "PREFIX sor: <http://purl.org/net/soron>\n" +
//            "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
//            "PREFIX sp: <http://spinrdf.org/sp>\n" +
//            "PREFIX spatial: <http://geovocab.org/spatial>\n" +
//            "PREFIX spcm: <http://spi-fm.uca.es/spdef/models/deployment/spcm/1.0>\n" +
//            "PREFIX spfood: <http://kmi.open.ac.uk/projects/smartproducts/ontologies/food.owl>\n" +
//            "PREFIX spin: <http://spinrdf.org/spin>\n" +
//            "PREFIX sport: <http://www.bbc.co.uk/ontologies/sport>\n" +
//            "PREFIX spt: <http://spitfire-project.eu/ontology/ns>\n" +
//            "PREFIX spvqa: <https://bmake.th-brandenburg.de/spv>\n" +
//            "PREFIX sql: <http://ns.inria.fr/ast/sql#>\n" +
//            "PREFIX sro: <http://salt.semanticauthoring.org/ontologies/sro>\n" +
//            "PREFIX ssn: <http://www.w3.org/2005/Incubator/ssn/ssnx/ssn>\n" +
//            "PREFIX ssno: <http://www.w3.org/ns/ssn/>\n" +
//            "PREFIX ssso: <http://purl.org/ontology/ssso>\n" +
//            "PREFIX st: <http://semweb.mmlab.be/ns/stoptimes#Ontology>\n" +
//            "PREFIX stac: <http://securitytoolbox.appspot.com/stac>\n" +
//            "PREFIX step: <http://purl.org/net/step>\n" +
//            "PREFIX sto: <https://w3id.org/i40/sto#>\n" +
//            "PREFIX stories: <http://purl.org/ontology/stories/>\n" +
//            "PREFIX summa: <http://purl.org/voc/summa/>\n" +
//            "PREFIX swc: <http://data.semanticweb.org/ns/swc/ontology>\n" +
//            "PREFIX swp: <http://www.w3.org/2004/03/trix/swp-1>\n" +
//            "PREFIX swpm: <http://spi-fm.uca.es/spdef/models/deployment/swpm/1.0>\n" +
//            "PREFIX swpo: <http://sw-portal.deri.org/ontologies/swportal>\n" +
//            "PREFIX swrc: <http://swrc.ontoware.org/ontology-07>\n" +
//            "PREFIX swrl: <http://www.w3.org/2003/11/swrl>\n" +
//            "PREFIX tac: <http://ns.bergnet.org/tac/0.1/triple-access-control>\n" +
//            "PREFIX tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>\n" +
//            "PREFIX tao: <http://vocab.deri.ie/tao>\n" +
//            "PREFIX taxon: <http://purl.org/biodiversity/taxon/>\n" +
//            "PREFIX te: <http://www.w3.org/2006/time-entry>\n" +
//            "PREFIX teach: <http://linkedscience.org/teach/ns#>\n" +
//            "PREFIX test: <http://www.w3.org/2006/03/test-description>\n" +
//            "PREFIX theatre: <http://purl.org/theatre#>\n" +
//            "PREFIX thors: <http://resource.geosciml.org/ontology/timescale/thors>\n" +
//            "PREFIX ti: <http://www.ontologydesignpatterns.org/cp/owl/timeinterval.owl>\n" +
//            "PREFIX time: <http://www.w3.org/2006/time>\n" +
//            "PREFIX tio: <http://purl.org/tio/ns#>\n" +
//            "PREFIX tis: <http://www.ontologydesignpatterns.org/cp/owl/timeindexedsituation.owl>\n" +
//            "PREFIX tisc: <http://www.observedchange.com/tisc/ns#>\n" +
//            "PREFIX tl: <http://purl.org/NET/c4dm/timeline.owl>\n" +
//            "PREFIX tm: <http://def.seegrid.csiro.au/isotc211/iso19108/2002/temporal>\n" +
//            "PREFIX tmo: <http://www.w3.org/2001/sw/hcls/ns/transmed/>\n" +
//            "PREFIX topo: <http://data.ign.fr/def/topo>\n" +
//            "PREFIX tp: <http://tour-pedia.org/download/tp.owl>\n" +
//            "PREFIX traffic: <http://www.sensormeasurement.appspot.com/ont/transport/traffic>\n" +
//            "PREFIX trait: <http://contextus.net/ontology/ontomedia/ext/common/trait#>\n" +
//            "PREFIX transit: <http://vocab.org/transit/terms/>\n" +
//            "PREFIX trao: <http://linkeddata.finki.ukim.mk/lod/ontology/tao#>\n" +
//            "PREFIX tsioc: <http://rdfs.org/sioc/types#>\n" +
//            "PREFIX tsn: <http://purl.org/net/tsn#>\n" +
//            "PREFIX tsnc: <http://purl.org/net/tsnchange#>\n" +
//            "PREFIX turismo: <http://idi.fundacionctic.org/cruzar/turismo>\n" +
//            "PREFIX tvc: <http://www.essepuntato.it/2012/04/tvc>\n" +
//            "PREFIX txn: <http://lod.taxonconcept.org/ontology/txn.owl>\n" +
//            "PREFIX tzont: <http://www.w3.org/2006/timezone>\n" +
//            "PREFIX uby: <http://purl.org/olia/ubyCat.owl>\n" +
//            "PREFIX uco: <http://purl.org/uco/ns#>\n" +
//            "PREFIX ucum: <http://purl.oclc.org/NET/muo/ucum/>\n" +
//            "PREFIX ui: <http://www.w3.org/ns/ui>\n" +
//            "PREFIX umbel: <http://umbel.org/umbel>\n" +
//            "PREFIX uneskos: <http://purl.org/umu/uneskos>\n" +
//            "PREFIX uniprot: <http://purl.uniprot.org/core/>\n" +
//            "PREFIX uri4uri: <http://uri4uri.net/vocab>\n" +
//            "PREFIX usability: <https://w3id.org/usability>\n" +
//            "PREFIX va: <http://code-research.eu/ontology/visual-analytics>\n" +
//            "PREFIX vaem: <http://www.linkedmodel.org/schema/vaem>\n" +
//            "PREFIX vag: <http://www.essepuntato.it/2013/10/vagueness>\n" +
//            "PREFIX vann: <http://purl.org/vocab/vann/>\n" +
//            "PREFIX vcard: <http://www.w3.org/2006/vcard/ns>\n" +
//            "PREFIX vdpp: <http://data.lirmm.fr/ontologies/vdpp>\n" +
//            "PREFIX veo: <http://linkeddata.finki.ukim.mk/lod/ontology/veo#>\n" +
//            "PREFIX vgo: <http://purl.org/net/VideoGameOntology>\n" +
//            "PREFIX vin: <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine>\n" +
//            "PREFIX vir: <http://w3id.org/vir#>\n" +
//            "PREFIX vivo: <http://vivoweb.org/ontology/core>\n" +
//            "PREFIX vmm: <http://spi-fm.uca.es/spdef/models/genericTools/vmm/1.0>\n" +
//            "PREFIX voaf: <http://purl.org/vocommons/voaf>\n" +
//            "PREFIX voag: <http://voag.linkedmodel.org/schema/voag>\n" +
//            "PREFIX vocals: <http://w3id.org/rsp/vocals#>\n" +
//            "PREFIX void: <http://vocab.deri.ie/void>\n" +
//            "PREFIX voidwh: <http://www.ics.forth.gr/isl/VoIDWarehouse/VoID_Extension_Schema.owl>\n" +
//            "PREFIX vra: <http://simile.mit.edu/2003/10/ontologies/vraCore3#>\n" +
//            "PREFIX vrank: <http://vocab.sti2.at/vrank>\n" +
//            "PREFIX vs: <http://www.w3.org/2003/06/sw-vocab-status/ns>\n" +
//            "PREFIX vsearch: <http://purl.org/vsearch/>\n" +
//            "PREFIX vso: <http://purl.org/vso/ns>\n" +
//            "PREFIX vvo: <http://purl.org/vvo/ns#>\n" +
//            "PREFIX w3c-ssn: <https://www.w3.org/ns/ssn>\n" +
//            "PREFIX wai: <http://purl.org/wai#>\n" +
//            "PREFIX wdrs: <http://www.w3.org/2007/05/powder-s>\n" +
//            "PREFIX wf-invoc: <http://purl.org/net/wf-invocation>\n" +
//            "PREFIX wfdesc: <http://purl.org/wf4ever/wfdesc>\n" +
//            "PREFIX wfm: <http://purl.org/net/wf-motifs>\n" +
//            "PREFIX wfprov: <http://purl.org/wf4ever/wfprov>\n" +
//            "PREFIX whisky: <http://vocab.org/whisky/terms>\n" +
//            "PREFIX whois: <http://www.kanzaki.com/ns/whois>\n" +
//            "PREFIX wi: <http://purl.org/ontology/wi/core#>\n" +
//            "PREFIX wikim: <http://spi-fm.uca.es/spdef/models/genericTools/wikim/1.0>\n" +
//            "PREFIX wl: <http://www.wsmo.org/ns/wsmo-lite#>\n" +
//            "PREFIX wlo: <http://purl.org/ontology/wo/>\n" +
//            "PREFIX wo: <http://purl.org/ontology/wo/core#>\n" +
//            "PREFIX wot: <http://xmlns.com/wot/0.1/>\n" +
//            "PREFIX xapi: <http://purl.org/xapi/ontology#>\n" +
//            "PREFIX xbrll: <https://w3id.org/vocab/xbrll>\n" +
//            "PREFIX xhv: <http://www.w3.org/1999/xhtml/vocab>\n" +
//            "PREFIX xkos: <http://rdf-vocabulary.ddialliance.org/xkos>\n" +
//            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema>\n" +
//            "PREFIX zbwext: <http://zbw.eu/namespaces/zbw-extensions>\n" +
//            "PREFIX caresses: <http://caressesrobot.org/ontology>\n" +
//            "PREFIX fog: <https://w3id.org/fog>\n" +
//            "PREFIX gleif-L1: <https://www.gleif.org/ontology/L1/>\n" +
//            "PREFIX gleif-L2: <https://www.gleif.org/ontology/L2/>\n" +
//            "PREFIX gleif-base: <https://www.gleif.org/ontology/Base/>\n" +
//            "PREFIX gleif-elf: <https://www.gleif.org/ontology/EntityLegalForm/>\n" +
//            "PREFIX gleif-geo: <https://www.gleif.org/ontology/Geocoding/>\n" +
//            "PREFIX gleif-ra: <https://www.gleif.org/ontology/RegistrationAuthority/>\n" +
//            "PREFIX gleif-repex: <https://www.gleif.org/ontology/ReportingException/>\n" +
//            "PREFIX sw-quality: <https://w3id.org/squap/>\n" +
//            "PREFIX toco: <http://purl.org/toco/>\n";

}
