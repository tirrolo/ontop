package sesameWrapper;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;

public class SesameRepositoryFactory implements RepositoryFactory{

	public String getRepositoryType() {
		return "obda:QuestRepository";
	}

	public RepositoryImplConfig getConfig() {
		return new SesameRepositoryConfig();
	}
	
	public RepositoryImplConfig getConfig(String type) {
		return new SesameRepositoryConfig(type);
	}
	
	public RepositoryImplConfig getConfig(String type, String name, String owlfile) {
		return new SesameRepositoryConfig(type, name, owlfile);
	}
	
	public RepositoryImplConfig getConfig(String type, String name, String owlfile, String obdafile) {
		return new SesameRepositoryConfig(type, name, owlfile, obdafile);
	}

	public Repository getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		// TODO Auto-generated method stub
		
		if (config instanceof SesameRepositoryConfig)
		{
			try{
			if (!config.getType().isEmpty())
			{
					String name = ((SesameRepositoryConfig) config).getName();
					String owlfile = ((SesameRepositoryConfig) config).getOwlFile();

					if (config.getType().equals("quest-inmemory"))
						return new SesameClassicInMemoryRepo(name, owlfile);
					
					
					else if (config.getType().equals("quest-remote"))
						return new SesameClassicJDBCRepo(name, owlfile);
					
					
					else if (config.getType().equals("quest-virtual")) 
					{
						String obdafile = ((SesameRepositoryConfig) config).getObdaFile();
						return new SesameVirtualRepo(name, owlfile, obdafile);
					}
			}}
			catch(Exception e)
			{e.printStackTrace();
			throw new RepositoryConfigException("Could not create Sesame Repo!");
			}
		}
		else 
		{
	        throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
		}
		return null;
	}

}
