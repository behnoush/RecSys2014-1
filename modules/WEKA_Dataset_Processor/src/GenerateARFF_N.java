import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class GenerateARFF_N {

	static HashMap<String, HashMap<String, String>> imdb_data = new HashMap<String, HashMap<String, String>>();
	static int N = 10;

	public static void main(String args[]) {
		if (args.length > 0) {
			N = Integer.parseInt(args[0]);
		}
		// generateARFF();

		// Switch the parameters for the favorite count datasets.
		if (args[1].equals("r"))
			processGeneratedARFF("retweet_count", "favorite_count");
		else if (args[1].equals("f"))
			processGeneratedARFF("favorite_count", "retweet_count");
		else if (args[1].equals("e"))
			processGeneratedARFF("engagement", "");
		else
			System.out.println("Say 'r', 'f' or 'e'");
	}

	private static void processGeneratedARFF(String classAttr, String otherAttr) {
		String fname = "/home/gopi/RecSys2014/dataset/full2.arff";
		String features = "/home/gopi/RecSys2014/dataset/features_toStringWordVectors.txt";

		// String fname =
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\full2.arff";
		// String features =
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\features_toStringWordVectors.txt";

		ArrayList<String> featureList = getFeatureNames(features);
		Instances training = null;
		try {
			training = loadTrainingARFF(fname);
			Reorder rd = new Reorder();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (!otherAttr.equals("")){
			training.deleteAttributeAt(training.attribute(otherAttr).index());
			training.deleteAttributeAt(training.attribute("engagement").index());
		}

		Instances trainingLarger = new Instances(training);
		training.setClass(training.attribute(classAttr));

		System.out.println("[" + N + "]Done reading the ARFF file.");
		Instances temp = null;
		for (int i = 0; i < featureList.size(); i++) {
			temp = new Instances(training);
			System.out.println("FEATURE: " + featureList.get(i));
			for (int j = 0; j < temp.numAttributes();) {
				if (temp.attribute(j).name().equals(featureList.get(i))
						|| temp.attribute(j)
								.name()
								.equals(temp.firstInstance().classAttribute()
										.name())) {
					j++;
				} else {
					temp.deleteAttributeAt(j);
				}
			}
			temp.setClass(temp.attribute(classAttr));
			temp = strToWVAndSelect(temp, classAttr, featureList.get(i));
			// temp.setClassIndex(0);
			// temp.deleteAttributeAt(temp.attribute(classAttr).index());
			trainingLarger.deleteAttributeAt(trainingLarger
					.attribute(classAttr).index());
			trainingLarger.deleteAttributeAt(trainingLarger.attribute(
					featureList.get(i)).index());
			trainingLarger = Instances.mergeInstances(trainingLarger, temp);
		}

		System.out.println("Writing to file.");
		try {
			ArffSaver saver = new ArffSaver();
			saver.setInstances(trainingLarger);
			saver.setFile(new File("full2_larger_" + N + ".arff"));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	public static Instances strToWVAndSelect(Instances t, String classAttr,
			String pName) {

		t.setClass(t.attribute(classAttr));
		Attribute cls = t.classAttribute();

		// Nominal to String
		try {
			NominalToString ns = new NominalToString();
			ns.setInputFormat(t);
			ns.setOptions(new String[] { "-C", "first" });
			t = Filter.useFilter(t, ns);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// String to Word Vector
		try {
			StringToWordVector swv = new StringToWordVector();
			swv.setInputFormat(t);
			swv.setUseStoplist(true);
			swv.setOptions(new String[] { "-P", pName + "~" });
			t = Filter.useFilter(t, swv);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			AttributeSelection filter = new AttributeSelection();
			filter.setInputFormat(t);
			GainRatioAttributeEval eval = new GainRatioAttributeEval();
			Ranker search = new Ranker();
			if (t.numAttributes() > N) {
				search.setNumToSelect(N);
			}
			filter.setEvaluator(eval);
			filter.setSearch(search);
			// generate new data
			t = Filter.useFilter(t, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	private static ArrayList<String> getFeatureNames(String features) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(features));
			String line = "";

			while ((line = br.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				list.add(line);
			}
		} catch (IOException e) {
		}
		return list;
	}

	public static void generateARFF() throws Exception {

		// String fname = "/home/gopi/RecSys2014/dataset/full1a.arff";
		String fname = "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\full1a.arff";
		Map<String, Double> newValues = new HashMap<String, Double>();

		Instances training = loadTrainingARFF(fname);
		training.insertAttributeAt(new Attribute("avg_user_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("avg_movie_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("movie_retweet_count"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("user_count"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("movie_count"),
				training.numAttributes());

		training.insertAttributeAt(new Attribute(
				"entities-user_mentions-id_str_count"), training
				.numAttributes());
		training.insertAttributeAt(new Attribute("retweeted_flag",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(new Attribute(
				"retweeted_status-entities-user_mentions-id_count"), training
				.numAttributes());
		training.insertAttributeAt(new Attribute("unix_timestamp",
				(FastVector) null), training.numAttributes());

		training.insertAttributeAt(new Attribute("imdb_genres",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(
				new Attribute("imdb_cast", (FastVector) null),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_release_date",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(new Attribute(
				"imdb_release_date_tweet_diff_minutes"), training
				.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_director",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_languages",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_countries",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(
				new Attribute("imdb_plot", (FastVector) null),
				training.numAttributes());

		// ------------------------------------

		for (int i = 0; i < training.numInstances(); i++) {
			if (training.instance(i).value(
					training.attribute("retweeted_status-id")) > 0) {
				training.instance(i).setValue(
						training.attribute("retweeted_flag"), "1");
			} else {
				training.instance(i).setValue(
						training.attribute("retweeted_flag"), "0");
			}
		}

		newValues = getAverage(training, "twitter_user_id", "rating");
		training = updateInstances(training, newValues, "twitter_user_id",
				"avg_user_rating");

		newValues = getCount(training, "twitter_user_id",
				"entities-user_mentions-id_str");
		training = updateInstances(training, newValues, "twitter_user_id",
				"entities-user_mentions-id_str_count");

		newValues = getCount(training, "twitter_user_id",
				"retweeted_status-entities-user_mentions-id");
		training = updateInstances(training, newValues, "twitter_user_id",
				"retweeted_status-entities-user_mentions-id_count");

		newValues = getAverage(training, "imdb_item_id", "rating");
		training = updateInstances(training, newValues, "imdb_item_id",
				"avg_movie_rating");

		newValues = getCount(training, "imdb_item_id", "retweet_count");
		training = updateInstances(training, newValues, "imdb_item_id",
				"movie_retweet_count");

		newValues = getCount(training, "imdb_item_id");
		training = updateInstances(training, newValues, "imdb_item_id",
				"movie_count");

		newValues = getCount(training, "twitter_user_id");
		training = updateInstances(training, newValues, "twitter_user_id",
				"user_count");

		training = addUNIXTimestamp(training);

		System.out.println("Reading IMDb Data.");
		NumericToNominal nton = new NumericToNominal();
		nton.setInputFormat(training);
		nton.setAttributeIndicesArray(new int[] {
				training.attribute("imdb_item_id").index(),
				training.attribute("retweet_count").index(),
				training.attribute("favorite_count").index(),
				training.attribute("twitter_user_id").index(),
				training.attribute("entities-media-id").index(),
				training.attribute("entities-user_mentions-id_str").index(),
				training.attribute("id").index(),
				training.attribute("in_reply_to_status_id").index(),
				training.attribute("in_reply_to_user_id").index(),
				training.attribute("retweeted_status-entities-media-id")
						.index(),
				training.attribute("retweeted_status-favorite_count").index(),
				training.attribute("retweeted_status-retweet_count").index(),
				training.attribute("retweeted_status-id").index(),
				training.attribute("retweeted_status-in_reply_to_status_id")
						.index(),
				training.attribute("retweeted_status-in_reply_to_user_id")
						.index(),
				training.attribute("retweeted_status-user-id").index() });

		training = Filter.useFilter(training, nton);
		readIMDbData();

		training = updateInstanceIMDb(training, "imdb_genres");
		training = updateInstanceIMDb(training, "imdb_cast");
		training = updateInstanceIMDb(training, "imdb_release_date");
		training = updateInstanceTimeDiff(training, "imdb_release_date",
				"unix_timestamp", "imdb_release_date_tweet_diff_minutes");
		training = updateInstanceIMDb(training, "imdb_director");
		training = updateInstanceIMDb(training, "imdb_languages");
		training = updateInstanceIMDb(training, "imdb_countries");
		training = updateInstanceIMDb(training, "imdb_plot");

		// training.deleteAttributeAt(training.attribute("created_at").index());

		// ----------------------------------------

		StringToNominal stn = new StringToNominal();
		stn.setInputFormat(training);
		stn.setOptions(new String[] { "-R",
				(training.attribute("retweeted_flag").index() + 1) + "" });
		training = Filter.useFilter(training, stn);

		writeInstances(training);
		System.out.println("Done!");
	}

	private static Instances updateInstanceTimeDiff(Instances training,
			String k1, String k2, String diffKey) throws NumberFormatException,
			ParseException {

		Compare differ = new Compare();
		for (int i = 0; i < training.numInstances(); i++) {
			String ts1 = training.instance(i).stringValue(
					training.attribute(k1));
			String ts2 = training.instance(i).stringValue(
					training.attribute(k2));
			if (!ts1.equals("") && !ts1.contains("?") && !ts2.equals("")) {
				training.instance(i).setValue(training.attribute(diffKey),
						differ.timeInMinutes(ts1, Long.parseLong(ts2)));
			}
		}
		return training;
	}

	private static Instances addUNIXTimestamp(Instances training)
			throws ParseException {
		for (int i = 0; i < training.numInstances(); i++) {
			String ts = training.instance(i).stringValue(
					training.attribute("created_at"));
			training.instance(i).setValue(training.attribute("unix_timestamp"),
					getUNIXTimestamp(ts));
		}
		return training;
	}

	private static Instances updateInstanceIMDb(Instances training,
			String imdb_ftr) throws Exception {

		String id;
		for (int i = 0; i < training.numInstances(); i++) {
			id = training.instance(i).stringValue(
					training.attribute("imdb_item_id"));
			try {
				if (imdb_data.keySet().contains(id)) {
					training.instance(i).setValue(training.attribute(imdb_ftr),
							imdb_data.get(id).get(imdb_ftr) + "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return training;
	}

	private static void writeInstances(Instances training) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File("full2.arff"));
		saver.writeBatch();
	}

	private static Instances updateInstances(Instances training,
			Map<String, Double> newValues, String k1, String k2) {

		String id;
		for (int i = 0; i < training.numInstances(); i++) {

			id = (int) training.instance(i).value(training.attribute(k1)) + "";
			try {
				training.instance(i).setValue(training.attribute(k2),
						newValues.get(id));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return training;
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();

		return data;
	}

	public static String getUNIXTimestamp(String date) throws ParseException {
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(true);
		return sf.parse(date).getTime() + "";
	}

	public static Map<String, Double> getAverage(Instances training,
			String key1, String key2) {
		Map<String, Double> avgRating = new HashMap<String, Double>();
		Map<String, Double> appCount = new HashMap<String, Double>();

		String uid = "";
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {
			try {
				uid = (int) training.instance(i)
						.value(training.attribute(key1)) + "";

				rating = (int) training.instance(i).value(
						training.attribute(key2));
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = appCount.containsKey(uid) ? appCount.get(uid) : 0;
			appCount.put(uid, count + 1);

			double r = avgRating.containsKey(uid) ? avgRating.get(uid) : 0;
			avgRating.put(uid, r + rating);
		}

		for (String u : appCount.keySet()) {
			double rt = avgRating.get(u);
			double c = appCount.get(u);

			avgRating.put(u, rt / c);
		}

		return avgRating;
	}

	public static Map<String, Double> getCount(Instances training, String key1,
			String key2) {
		// name says avg but it is only count. I am too lazy.
		Map<String, Double> avgRating = new HashMap<String, Double>();
		Map<String, Double> appCount = new HashMap<String, Double>();

		String uid;
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {

			try {

				uid = (int) training.instance(i)
						.value(training.attribute(key1)) + "";
				rating = (int) training.instance(i).value(
						training.attribute(key2));
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = appCount.containsKey(uid) ? appCount.get(uid) : 0;
			appCount.put(uid, count + 1);

			double r = avgRating.containsKey(uid) ? avgRating.get(uid) : 0;
			avgRating.put(uid, r + rating);
		}

		return avgRating;
	}

	public static Map<String, Double> getCount(Instances training, String key1) {
		// name says avg but it is only count. I am too lazy.
		Map<String, Double> avgRating = new HashMap<String, Double>();
		Map<String, Double> appCount = new HashMap<String, Double>();

		String uid;

		for (int i = 0; i < training.numInstances(); i++) {

			try {
				uid = (int) training.instance(i)
						.value(training.attribute(key1)) + "";
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = appCount.containsKey(uid) ? appCount.get(uid) : 0;
			appCount.put(uid, count + 1);

			double r = avgRating.containsKey(uid) ? avgRating.get(uid) : 0;
			avgRating.put(uid, r + 1);
		}

		return avgRating;
	}

	public static void readIMDbData() throws IOException {
		// C:\Users\WKUUSER\Documents\RecSys2014\dataset
		String imdbFile = "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\imdb_features_training.csv";
		// String imdbFile =
		// "/home/gopi/RecSys2014/dataset/imdb_features_training.csv";
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(imdbFile));

		while ((line = br.readLine()) != null) {
			try {
				String[] tokens = line.split("\t", -1);
				HashMap<String, String> imdbVals = new HashMap<String, String>();

				imdbVals.put("imdb_genres", tokens[1].trim());
				imdbVals.put("imdb_cast", tokens[2].trim());
				imdbVals.put("imdb_release_date", tokens[3].trim());
				imdbVals.put("imdb_director", tokens[4].trim());
				imdbVals.put("imdb_languages", tokens[5].trim());
				imdbVals.put("imdb_countries", tokens[6].trim());
				imdbVals.put("imdb_plot", tokens[7].trim());

				imdb_data
						.put(Integer.parseInt(tokens[0].trim()) + "", imdbVals);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Error--> " + line + "\n");
			}
		}
		br.close();
	}
}
