package cl.uchile.dcc.query.cardinality;

public class Join extends Operator {

	private Operator child1;
	private Operator child2;
	private String joinVar;
	public Join(TableStats stats) {
		super(stats);
		// TODO Auto-generated constructor stub
	}
	@Override
	public int getCardinality() {
		// TODO Auto-generated method stub
		return 0;
	}

}
