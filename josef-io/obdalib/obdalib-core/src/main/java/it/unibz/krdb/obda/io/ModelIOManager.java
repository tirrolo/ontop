package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.TurtleSyntaxParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages saving and loading an OBDA file.
 */
public class ModelIOManager {

    private enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, targetQuery, sourceQuery
    }

    private static final String PREFIX_DECLARATION_TAG = "[PrefixDeclaration]";
    private static final String CLASS_DECLARATION_TAG = "[ClassDeclaration]";
    private static final String OBJECT_PROPERTY_DECLARATION_TAG = "[ObjectPropertyDeclaration]";
    private static final String DATA_PROPERTY_DECLARATION_TAG = "[DataPropertyDeclaration]";
    private static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";
    private static final String MAPPING_DECLARATION_TAG = "[MappingDeclaration]";

    private static final String START_COLLECTION_SYMBOL = "@collection [[";
    private static final String END_COLLECTION_SYMBOL = "]]";
    private static final String COMMENT_SYMBOL = ";";

    private static final int MAX_ENTITIES_PER_ROW = 10;

    private OBDAModel model;

    private static final Logger log = LoggerFactory.getLogger(ModelIOManager.class);

    /**
     * Create an IO manager for saving/loading the OBDA model.
     * 
     * @param model
     *          The target OBDA model.
     */
    public ModelIOManager(OBDAModel model) {
        this.model = model;
    }

    /**
     * The save/write operation.
     * 
     * @param fileLocation
     *          The target file location to which the model is saved.
     * @throws IOException
     */
    public void save(String fileLocation) throws IOException {
        save(new File(fileLocation));
    }

    /**
     * The save/write operation.
     * 
     * @param file
     *          The target file object to which the model is saved.
     * @throws IOException
     */
    public void save(File file) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writePrefixDeclaration(writer);
            writeClassEntityDeclaration(writer);
            writeObjectPropertyDeclaration(writer);
            writeDataPropertyDeclaration(writer);
            for (OBDADataSource source : model.getSources()) {
                writeSourceDeclaration(source, writer);
                writeMappingDeclaration(source, writer);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

    /**
     * The load/read operation.
     * 
     * @param fileLocation
     *          The target file location from which the model is loaded.
     * @throws IOException
     */
    public void load(String fileLocation) throws IOException {
        load(new File(fileLocation));
    }
    
    /**
     * The load/read operation.
     * 
     * @param fileLocation
     *          The target file object from which the model is loaded.
     * @throws IOException
     */
    public void load(File file) throws IOException {
        if (!file.exists()) {
            // NO-OP: Users may not have the OBDA file
        }
        if (!file.canRead()) {
            throw new IOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
        
        // Clean the model first before loading
        model.reset();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        URI sourceUri = null;
        while ((line = reader.readLine()) != null) {
            if (isCommentLine(line)) {
                continue; // skip comment lines
            }
            if (line.contains(PREFIX_DECLARATION_TAG)) {
                readPrefixDeclaration(reader);
            } else if (line.contains(CLASS_DECLARATION_TAG)) {
                readClassDeclaration(reader);
            } else if (line.contains(OBJECT_PROPERTY_DECLARATION_TAG)) {
                readObjectPropertyDeclaration(reader);
            } else if (line.contains(DATA_PROPERTY_DECLARATION_TAG)) {
                readDataPropertyDeclaration(reader);
            } else if (line.contains(SOURCE_DECLARATION_TAG)) {
                sourceUri = readSourceDeclaration(reader);
            } else if (line.contains(MAPPING_DECLARATION_TAG)) {
                readMappingDeclaration(reader, sourceUri);
            }
        }
    }

    /*
     * Helper methods related to save file.
     */

    private void writePrefixDeclaration(BufferedWriter writer) throws IOException {
        final Map<String, String> prefixMap = model.getPrefixManager().getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(PREFIX_DECLARATION_TAG);
        writer.write("\n");
        for (String prefix : prefixMap.keySet()) {
            String uri = prefixMap.get(prefix);
            writer.write(prefix + (prefix.length() >= 9 ? "\t" : "\t\t") + uri + "\n");
        }
        writer.write("\n");
    }

    private void writeClassEntityDeclaration(BufferedWriter writer) throws IOException {
        writer.write(CLASS_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");
        writeEntities(model.getDeclaredClasses(), writer);
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private void writeObjectPropertyDeclaration(BufferedWriter writer) throws IOException {
        writer.write(OBJECT_PROPERTY_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");
        writeEntities(model.getDeclaredObjectProperties(), writer);
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private void writeDataPropertyDeclaration(BufferedWriter writer) throws IOException {
        writer.write(DATA_PROPERTY_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");
        writeEntities(model.getDeclaredDataProperties(), writer);
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private void writeEntities(Set<? extends Predicate> predicates, BufferedWriter writer) throws IOException {
        int count = 1;
        boolean needComma = false;
        for (Predicate p : predicates) {
            if (count > MAX_ENTITIES_PER_ROW) {
                writer.write("\n");
                count = 1;
                needComma = false;
            }
            if (needComma) {
                writer.write(", ");
            }
            writer.write(p.toString());
            needComma = true;
            count++;
        }
    }
    
    private void writeSourceDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        writer.write(SOURCE_DECLARATION_TAG);
        writer.write("\n");
        writer.write(Label.sourceUri.name() + "\t" + source.getSourceID() + "\n");
        writer.write(Label.connectionUrl.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_URL) + "\n");
        writer.write(Label.username.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME) + "\n");
        writer.write(Label.password.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD) + "\n");
        writer.write(Label.driverClass.name() + "\t" + source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER) + "\n");
        writer.write("\n");
    }

    private void writeMappingDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        final URI sourceUri = source.getSourceID();
        CQFormatter formatter = new TurtleFormatter(model.getPrefixManager());

        writer.write(MAPPING_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");
        
        boolean needLineBreak = false;
        for (OBDAMappingAxiom mapping : model.getMappings(sourceUri)) {
            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(Label.mappingId.name() + "\t" + mapping.getId() + "\n");
            writer.write(Label.targetQuery.name() + "\t" + formatter.print((CQIE) mapping.getTargetQuery()) + "\n");
            writer.write(Label.sourceQuery.name() + "\t" + mapping.getSourceQuery() + "\n");
            needLineBreak = true;
        }
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    /*
     * Helper methods related to load file.
     */

    private void readPrefixDeclaration(BufferedReader reader) throws IOException {
        final PrefixManager pm = model.getPrefixManager();

        String line = "";
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("\t+");
            pm.addPrefix(tokens[0], tokens[1]);
        }
    }

    private void readClassDeclaration(BufferedReader reader) throws IOException {
        // NO-OP
    }
    
    private void readObjectPropertyDeclaration(BufferedReader reader) throws IOException {
        // NO-OP
    }
    
    private void readDataPropertyDeclaration(BufferedReader reader) throws IOException {
        // NO-OP
    }

    private URI readSourceDeclaration(BufferedReader reader) throws IOException {
        String line = "";
        URI sourceUri = null;
        OBDADataSource datasource = null;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("\t");
            if (tokens[0].equals(Label.sourceUri.name())) {
                sourceUri = URI.create(tokens[1]);
                // TODO: BAD CODE! The data source id should be part of the
                // parameters!
                datasource = model.getDataFactory().getDataSource(sourceUri);
            } else if (tokens[0].equals(Label.connectionUrl.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, tokens[1]);
            } else if (tokens[0].equals(Label.username.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, tokens[1]);
            } else if (tokens[0].equals(Label.password.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, tokens[1]);
            } else if (tokens[0].equals(Label.driverClass.name())) {
                datasource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, tokens[1]);
            }
        }
        // Save the source to the model.
        model.addSource(datasource);
        return sourceUri;
    }

    private void readMappingDeclaration(BufferedReader reader, URI sourceUri) throws IOException {
        OBDADataFactory dfac = model.getDataFactory();
        TurtleSyntaxParser parser = new TurtleSyntaxParser(model.getPrefixManager());

        String line = "";
        String mappingId = "";
        String sourceString = "";
        CQIE targetQuery = null;
        while (!(line = reader.readLine()).equals(END_COLLECTION_SYMBOL)) {
            String[] tokens = line.split("\t");
            if (tokens[0].equals(Label.mappingId.name())) {
                mappingId = tokens[1];
            } else if (tokens[0].equals(Label.targetQuery.name())) {
                try {
                    targetQuery = parser.parse(tokens[1]);
                } catch (Exception e) {
                    log.error("Error on parsing the target query (Mapping ID=" + mappingId + ")");
                }
            } else if (tokens[0].equals(Label.sourceQuery.name())) {
                sourceString = tokens[1];

                // Add the mapping to the model.
                try {
                    OBDAMappingAxiom axiom = dfac.getRDBMSMappingAxiom(mappingId, sourceString, targetQuery);
                    model.addMapping(sourceUri, axiom);
                } catch (DuplicateMappingException e) {
                    log.warn("A mapping duplication was found: " + mappingId);
                }
            }
        }
    }

    private boolean isCommentLine(String line) {
        // A comment line is always started by semi-colon
        return line.contains(COMMENT_SYMBOL) && line.trim().indexOf(COMMENT_SYMBOL) == 0;
    }
}
