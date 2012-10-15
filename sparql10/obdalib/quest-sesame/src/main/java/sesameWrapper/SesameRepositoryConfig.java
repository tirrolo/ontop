package sesameWrapper;
/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */


import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.config.RepositoryConfig;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * @author Herko ter Horst
 */
public class SesameRepositoryConfig extends RepositoryImplConfigBase {

	private String type;
    private String name;
    private String owlfile;
    private String obdafile;

    /**
     * Create a new RepositoryConfigImpl.
     */
    public SesameRepositoryConfig() {
    }

    /**
     * Create a new RepositoryConfigImpl.
     */
    public SesameRepositoryConfig(String type) {
        this();
        setType(type);
    }
    
    public SesameRepositoryConfig(String type, String name, String owlfile) {
        this();
        setType(type);
        setName(name);
        setOwlFile(owlfile);
    }
    
    public SesameRepositoryConfig(String type, String name, String owlfile, String obdafile) {
        this();
        setType(type);
        setName(name);
        setOwlFile(owlfile);
        setObdaFile(obdafile);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
    public String getName()
    {
    	return name;
    }
    
    public void setName(String name)
    {
    	this.name = name;
    }
    
    public String getOwlFile()
    {
    	return owlfile;
    }

    public void setOwlFile(String file)
    {
    	this.owlfile = file;
    }
    
    public String getObdaFile()
    {
    	return obdafile;
    }
    
    public void setObdaFile(String file)
    {
    	this.obdafile = file;
    }


    @Override
    public void validate()
        throws RepositoryConfigException
    {
        if (type == null) {
            throw new RepositoryConfigException("No type specified for repository implementation");
        }
    }

    @Override
    public Resource export(Graph graph) {
        BNode implNode = graph.getValueFactory().createBNode();

        if (type != null) {
            graph.add(implNode, REPOSITORYTYPE, graph.getValueFactory().createLiteral(type));
        }
        if (name != null) {
            graph.add(implNode, REPOSITORYTYPE, graph.getValueFactory().createLiteral(name));
        }
        if (owlfile != null) {
            graph.add(implNode, REPOSITORYTYPE, graph.getValueFactory().createLiteral(owlfile));
        }
        if (obdafile != null) {
            graph.add(implNode, REPOSITORYTYPE, graph.getValueFactory().createLiteral(obdafile));
        }
        return implNode;
    }

    @Override
    public void parse(Graph graph, Resource implNode)
        throws RepositoryConfigException
    {
        try {
            Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);
            if (typeLit != null) {
                setType(typeLit.getLabel());
            }
          
        }
        catch (GraphUtilException e) {
            throw new RepositoryConfigException(e.getMessage(), e);
        }
    }


}