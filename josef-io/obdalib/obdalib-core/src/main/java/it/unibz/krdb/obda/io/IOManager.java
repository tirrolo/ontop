package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages saving and loading an OBDA file.
 */
public class IOManager {

    private enum Label {
        /* Entity decl.: */concept, objectProperty, dataProperty,
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, targetQuery, sourceQuery
    }

    private static final String PREFIX_DECLARATION = "[PrefixDeclaration]";
    private static final String ENTITY_DECLARATION = "[EntityDeclaration]";
    private static final String SOURCE_DECLARATION = "[SourceDeclaration]";
    private static final String MAPPING_DECLARATION = "[MappingDeclaration]";

    private static final String START_COLLECTION_SYMBOL = "@collection {";
    private static final String END_COLLECTION_SYMBOL = "}";
    private static final String COMMENT_SYMBOL = ";";

    private OBDAModel model;

    private static final Logger log = LoggerFactory.getLogger(IOManager.class);

    public IOManager(OBDAModel model) {
        this.model = model;
    }

    public void save(String fileLocation) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
            writePrefixDeclaration(writer);
            writeEntityDeclaration(writer);
            for (OBDADataSource source : model.getSources()) {
                writeSourceDeclaration(source, writer);
                writeMappingDeclaration(source, writer);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IOException("Error when writing the OBDA file: " + e.getMessage());
        }
    }

    public void load(String fileLocation) throws IOException {
        File target = new File(fileLocation);
        if (!target.exists()) {
            throw new IOException("File not found: " + fileLocation);
        }
        if (!target.canRead()) {
            throw new IOException("Error while reading the file " + fileLocation);
        }

        // Clean the model first before loading
        model.reset();

        BufferedReader reader = new BufferedReader(new FileReader(target));
        String line = "";
        URI sourceUri = null; //
        while ((line = reader.readLine()) != null) {
            if (isCommentLine(line)) {
                continue; // skip comment lines
            }

            if (line.contains(PREFIX_DECLARATION)) {
                readPrefixDeclaration(reader);
            } else if (line.contains(ENTITY_DECLARATION)) {
                readEntityDeclaration(reader);
            } else if (line.contains(SOURCE_DECLARATION)) {
                sourceUri = readSourceDeclaration(reader);
            } else if (line.contains(MAPPING_DECLARATION)) {
                readMappingDeclaration(reader, sourceUri);
            }
        }
    }

    /*
     * Methods related to save file.
     */

    private void writePrefixDeclaration(BufferedWriter writer) throws IOException {
        final Map<String, String> prefixMap = model.getPrefixManager().getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(PREFIX_DECLARATION);
        writer.write("\n");
        for (String prefix : prefixMap.keySet()) {
            String uri = prefixMap.get(prefix);
            writer.write(prefix + "\t\t" + uri + "\n");
        }
        writer.write("\n");
    }

    private void writeEntityDeclaration(BufferedWriter writer) throws IOException {
        // NO-OP
    }

    private void writeSourceDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        writer.write(SOURCE_DECLARATION);
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

        writer.write(MAPPING_DECLARATION + " " + START_COLLECTION_SYMBOL);
        for (OBDAMappingAxiom mapping : model.getMappings(sourceUri)) {
            writer.write("\n");
            writer.write(Label.mappingId.name() + "\t" + mapping.getId() + "\n");
            writer.write(Label.targetQuery.name() + "\t" + formatter.print((CQIE) mapping.getTargetQuery()) + "\n");
            writer.write(Label.sourceQuery.name() + "\t" + mapping.getSourceQuery() + "\n");
        }
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    /*
     * Methods related to load file.
     */

    private void readPrefixDeclaration(BufferedReader reader) throws IOException {
        final PrefixManager pm = model.getPrefixManager();

        String line = "";
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("\t+");
            pm.addPrefix(tokens[0], tokens[1]);
        }
    }

    private void readEntityDeclaration(BufferedReader reader) throws IOException {
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
