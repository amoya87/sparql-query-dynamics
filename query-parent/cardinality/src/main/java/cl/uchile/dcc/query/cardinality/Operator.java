package cl.uchile.dcc.query.cardinality;

public abstract class Operator implements IOperator{

	protected TableStats stats;
	
	protected Operator(TableStats stats) {
		super();
		this.stats = stats;
	}
	
	protected Operator() {
		super();
	}
	
	public abstract double getCardinality();

	public TableStats getStats() {
		return stats;
	}

	public void setStats(TableStats stats) {
		this.stats = stats;
	}

}
