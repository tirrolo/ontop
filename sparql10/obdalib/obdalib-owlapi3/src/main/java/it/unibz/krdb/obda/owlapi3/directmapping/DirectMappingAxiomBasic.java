package it.unibz.krdb.obda.owlapi3.directmapping;

import java.sql.DatabaseMetaData;

import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.ForeignKey;

public class DirectMappingAxiomBasic extends DirectMappingAxiom{
	
	
	public DirectMappingAxiomBasic(DataDefinition dd, DatabaseMetaData md, DBMetadata obda_md){
		this.table = dd;
		this.SQLString = new String("SELECT * FROM "+dd.getName());
		this.md = md;
		this.obda_md = obda_md;
	}
	
	private String generateURI(DataDefinition dd){
		String pk_uri=new String("<\"&:;" + dd.getName() + "-" + getDataDefinitionPK(dd) + "\">");
		return new String(pk_uri);
	}
	
	private String getDataDefinitionPK(DataDefinition dd){
		String pk = new String();
		int numPK = 1;
		for(int i=0;i<dd.getAttributes().size();i++){
			if(dd.getAttribute(i+1).bPrimaryKey && numPK!=1){
				pk += "."+"{$"+dd.getAttributeName(i+1)+"}";
			}
			if(dd.getAttribute(i+1).bPrimaryKey && numPK==1){
				pk = "{$"+dd.getAttributeName(i+1)+"}";
				numPK++;
			}		
		}
		return new String(pk);
	}
	
	public void generateCQ(){
		CQString=generateURI(this.table);
		CQString+=" a :"+this.table.getName()+" ; ";
		for(int i=0;i<table.getAttributes().size();i++){
			CQString+=":"+table.getName()+"-"+table.getAttributeName(i+1)+" $"+table.getAttributeName(i+1);
			if((i+1)!=table.getAttributes().size()){
				CQString+=" ; ";
			}else{
				CQString+=" . ";
			}
		}
		for(int i=0;i<table.getAttributes().size();i++){
			if(this.table.getAttribute(i+1).bForeignKey){
				String FKName = new String(this.table.getAttributeName(i+1));
				ForeignKey FK = new ForeignKey(this.md, this.table.getName(), FKName);
				String fk_uri = generateURI(this.obda_md.getDefinition(FK.getCoPKTable()));
				CQString+=generateURI(this.table)+" :"+table.getName()+"-ref-"+table.getAttributeName(i+1);
				CQString+=" "+fk_uri+" . ";
			}
		}
	}
	

}
