package inf.unibz.ontp.sesame.repository.test;


import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;

public class QuestRepos {

	//classic
	@Test
	public void test_inmemory() 
	{
		RepositoryConnection con = null;
		try{
		System.out.println("In-memory quest repo.");
		
		String owlfile = "src/test/resources/onto2.owl";
	
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-inmemory");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}}
	
	
	//classic
	@Test
	public void test_remote()
	{
		System.out.println("Remote quest repo.");
		RepositoryConnection con = null;
		try{		
		String owlfile = "src/test/resources/onto2.owl";
	
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-remote");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
	}
	
	
	//virtual
	@Test
	public void test_virtual()
	{
		System.out.println("Virtual quest repo.");
		String owlfile = "src/test/resources/stockexchange-unittest.owl";
		String obdafile = "src/test/resources/stockexchange-unittest.obda";
	
		RepositoryConnection con = null;
		try{		
			
		SesameRepositoryConfig config;
		SesameRepositoryFactory fact = new SesameRepositoryFactory();
		config = (SesameRepositoryConfig) fact.getConfig();
		config.setType("quest-virtual");
		config.setName("my_repo");
		config.setOwlFile(owlfile);
		config.setObdaFile(obdafile);
		
		Repository repo = fact.getRepository(config);		

		repo.initialize();
	
		con = (RepositoryConnection) repo.getConnection();
	
	System.out.println(con.toString());
		
	}catch(Exception e)
	{e.printStackTrace();}
	
	finally{
		try {
			con.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		
	}
	
	
	
	

}


