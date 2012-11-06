package sesameWrapper;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;

public class SesameRepositoryFactory implements RepositoryFactory{

	public static final String REPOSITORY_TYPE = "obda:QuestRepository";
	private SesameClassicInMemoryRepo mrepo = null;
	private SesameClassicJDBCRepo rrepo = null;
	private SesameVirtualRepo vrepo = null;
	   
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public SesameRepositoryConfig getConfig() {
		return new SesameRepositoryConfig();
	}
	
	
	public SesameAbstractRepo getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		// TODO Auto-generated method stub
		
		if (config instanceof SesameRepositoryConfig)
		{
			try{
			if (!config.getType().isEmpty())
			{
				config.validate();
					String name = ((SesameRepositoryConfig) config).getName();
					String owlfile = ((SesameRepositoryConfig) config).getOwlFile();
					
					if (((SesameRepositoryConfig) config).getQuestType().equals("quest-inmemory"))
					{
						if (mrepo == null)
							mrepo = new SesameClassicInMemoryRepo(name, owlfile);
						return mrepo;
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals("quest-remote"))
					{
						if (rrepo == null)
							rrepo = new SesameClassicJDBCRepo(name, owlfile);
						return rrepo;
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals("quest-virtual")) 
					{
						String obdafile = ((SesameRepositoryConfig) config).getObdaFile();
						if (vrepo == null)
						{
							//System.out.println("Repo created");
							vrepo =  new SesameVirtualRepo(name, owlfile, obdafile);
						}
						return vrepo;
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
