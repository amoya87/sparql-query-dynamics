package cl.uchile.dcc.query.cardinality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

import cl.uchile.dcc.dynamics.utils.MapUtils;
import cl.uchile.dcc.query.operator.IOperator;
import cl.uchile.dcc.query.operator.TableStats;

public class QueryCardEstimator {

	public QueryCardEstimator() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		ARQ.init();
		File path = new File(args[0]);
		File datapath = new File(args[1]);
		int mcvLenght = Integer.parseInt(args[2]);
		Map<String, TableStats> dbs = new HashMap<>();

		int tp = -1;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(datapath));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			String line;

			while ((line = br.readLine()) != null) {
				String[] vals = line.split("\t");
				long tripleNum = Long.parseLong(vals[1]);
				int subjectsNum = Integer.parseInt(vals[2]);
				int objectsNum = Integer.parseInt(vals[3]);

				Map<Integer, Integer> subjMCV = new HashMap<Integer, Integer>();
				Map<Integer, Integer> objMCV = new HashMap<Integer, Integer>();

				if (vals.length > 4) {
					String smcv = vals[4];
					if (!smcv.equals("")) {
						Map<String, String> subjMCVStr = MapUtils.string2Map(smcv, mcvLenght);
						subjMCVStr.forEach((key, value) -> subjMCV.put(Integer.parseInt(key), Integer.parseInt(value)));
					}
				}

				if (vals.length > 5) {
					String omcv = vals[5];
					Map<String, String> objMCVStr = MapUtils.string2Map(omcv, mcvLenght);
					objMCVStr.forEach((key, value) -> {
						objMCV.put(Integer.parseInt(key), Integer.parseInt(value));
					});
				}

				TableStats predicateStat = new TableStats(tripleNum);

				predicateStat.addVariable("s", new ImmutablePair<>(subjectsNum, subjMCV));
				predicateStat.addVariable("o", new ImmutablePair<>(objectsNum, objMCV));

				dbs.put(vals[0], predicateStat);
				++tp;
			}

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		File[] files = path.listFiles();
		Arrays.sort(files);

		for (File file : files) {
			System.out.printf(file.getName() + ",");
//			System.out.println("******************************************");
			
//			OutputStream os = new FileOutputStream(file.getName());
//			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), "utf-8"));
			
			Query query;
			try {
				query = QueryFactory.read(file.getAbsolutePath());
			} catch (Exception e1) {
				System.err.println(file.getAbsolutePath());
				e1.printStackTrace();
				continue;
			}
			
			Op op1 = Algebra.compile(query);
			
			OpToCardOperator cardOp = new OpToCardOperator(dbs, tp, mcvLenght);
			OpWalker.walk(op1, cardOp);
			IOperator treeStats = cardOp.getStats();
			if (treeStats != null) {
				System.out.println(treeStats.getCardinality());
			}
			
		}

	}

}
