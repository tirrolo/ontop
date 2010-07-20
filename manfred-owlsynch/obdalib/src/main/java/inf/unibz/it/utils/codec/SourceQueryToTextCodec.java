package inf.unibz.it.utils.codec;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

public class SourceQueryToTextCodec extends ObjectToTextCodec<SourceQuery> {

	public SourceQueryToTextCodec(APIController apic) {
		super(apic);
	}

	@Override
	public SourceQuery decode(String input) {
		RDBMSSQLQuery query;
		try {
			query = new RDBMSSQLQuery(input, apic);
			return query;
		} catch (QueryParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String encode(SourceQuery input) {
		
		return input.getInputQuString();
	}


}
