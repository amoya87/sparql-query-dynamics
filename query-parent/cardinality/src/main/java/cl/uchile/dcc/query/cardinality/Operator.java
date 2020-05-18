package cl.uchile.dcc.query.cardinality;

public abstract class Operator {

	private TableStats stats;
	
	public Operator(TableStats stats) {
		super();
		this.stats = stats;
	}
	
	public abstract int getCardinality();

	
	
	

}
