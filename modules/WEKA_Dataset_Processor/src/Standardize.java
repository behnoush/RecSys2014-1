import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;

public class Standardize {

	public static Instances trainingSet;
	public static Instances testingSet;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// String training =
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\training_2.arff";
		// String testing =
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\test.arff";

		String training = "/home/gopi/RecSys2014/dataset/training_2.arff";
		String testing = "/home/gopi/RecSys2014/dataset/test.arff";

		trainingSet = loadARFF(training);
		testingSet = loadARFF(testing);
		testingSet = addClassCategory(testingSet);

		// writeInstances(testingSet);
		ArrayList<String> trainingAttributes = getAttributeNames(trainingSet);
		ArrayList<String> testingAttributes = getAttributeNames(testingSet);

		// ArrayList<String> notInTraining = new ArrayList<String>(
		// testingAttributes);
		// notInTraining.removeAll(trainingAttributes);

		Instances testingSetStrWV = new Instances(testingSet);
		for (int i = 0; i < testingSetStrWV.numAttributes();) {
			if (trainingAttributes
					.contains(testingSetStrWV.attribute(i).name())) {
				testingSetStrWV.deleteAttributeAt(i);
			} else {
				i++;
			}
		}

		// for (int i = 0; i < testingSetStrWV.numAttributes(); i++) {
		// System.out.println(testingSetStrWV.attribute(i).name());
		// }

		Instances testingSetStrWVTemp = new Instances(testingSet);
		for (int i = 0; i < testingSetStrWV.numAttributes(); i++) {
			Instances testingTemp = new Instances(testingSetStrWV);
			if (testingSetStrWV.attribute(i).name()
					.equals("class_retweet_count_cats")) {
				continue;
			}
			for (int j = 0; j < testingTemp.numAttributes();) {
				if (!testingTemp.attribute(j).name()
						.equals(testingSetStrWV.attribute(i).name())
						&& !testingTemp.attribute(j).name()
								.equals("class_retweet_count_cats")) {
					testingTemp.deleteAttributeAt(j);
				} else
					j++;
			}

			// String attributes.
			testingTemp = strToWVAndSelect(testingTemp, testingSetStrWV
					.attribute(i).name(), false);
			for (int k = 0; k < testingTemp.numAttributes(); k++) {
				if (testingTemp.attribute(k).name()
						.equals("class_retweet_count_cats"))
					continue;

				try {
					if (trainingAttributes.contains(testingTemp.attribute(k)
							.name())) {
						testingSetStrWVTemp.insertAttributeAt(
								testingTemp.attribute(k),
								testingSetStrWVTemp.numAttributes());
					}
				} catch (Exception e) {
					continue;
				}
			}

			System.out.println(testingTemp.attribute(0).name() + " "
					+ testingTemp.numAttributes() + " --> "
					+ testingSetStrWVTemp.numAttributes());
		}

		for (int i = 0; i < testingSetStrWV.numAttributes(); i++) {
			Instances testingTemp = new Instances(testingSetStrWV);
			if (testingSetStrWV.attribute(i).name()
					.equals("class_retweet_count_cats")) {
				continue;
			}
			for (int j = 0; j < testingTemp.numAttributes();) {
				if (!testingTemp.attribute(j).name()
						.equals(testingSetStrWV.attribute(i).name())
						&& !testingTemp.attribute(j).name()
								.equals("class_retweet_count_cats")) {
					testingTemp.deleteAttributeAt(j);
				} else
					j++;
			}

			testingTemp = strToWVAndSelect(testingTemp, testingSetStrWV
					.attribute(i).name(), true);
			for (int k = 0; k < testingTemp.numAttributes(); k++) {
				if (testingTemp.attribute(k).name()
						.equals("class_retweet_count_cats"))
					continue;

				try {
					if (trainingAttributes.contains(testingTemp.attribute(k)
							.name())) {
						testingSetStrWVTemp.insertAttributeAt(
								testingTemp.attribute(k),
								testingSetStrWVTemp.numAttributes());
					}
				} catch (Exception e) {
					continue;
				}
			}

			System.out.println(testingTemp.attribute(0).name() + " "
					+ testingTemp.numAttributes() + " ++> "
					+ testingSetStrWVTemp.numAttributes());
		}

		for (int i = 0; i < testingSetStrWV.numAttributes(); i++) {
			Instances testingTemp = new Instances(testingSetStrWV);
			if (testingSetStrWV.attribute(i).name()
					.equals("class_retweet_count_cats")) {
				continue;
			}
			for (int j = 0; j < testingTemp.numAttributes();) {
				if (!testingTemp.attribute(j).name()
						.equals(testingSetStrWV.attribute(i).name())
						&& !testingTemp.attribute(j).name()
								.equals("class_retweet_count_cats")) {
					testingTemp.deleteAttributeAt(j);
				} else
					j++;
			}

			testingTemp = strToWVAndSelect_(testingTemp, testingSetStrWV
					.attribute(i).name(), true);
			for (int k = 0; k < testingTemp.numAttributes(); k++) {
				if (testingTemp.attribute(k).name()
						.equals("class_retweet_count_cats"))
					continue;

				try {
					if (trainingAttributes.contains(testingTemp.attribute(k)
							.name())) {
						testingSetStrWVTemp.insertAttributeAt(
								testingTemp.attribute(k),
								testingSetStrWVTemp.numAttributes());
					}
				} catch (Exception e) {
					continue;
				}
			}

			System.out.println(testingTemp.attribute(0).name() + " "
					+ testingTemp.numAttributes() + " ++__> "
					+ testingSetStrWVTemp.numAttributes());
		}

		
		for (int i = 0; i < testingSetStrWVTemp.numAttributes();) {
			if (trainingAttributes
					.contains(testingSetStrWVTemp.attribute(i).name())) {
				testingSetStrWVTemp.deleteAttributeAt(i);
			} else {
				i++;
			}
		}
		
		testingSetStrWVTemp.setClass(testingSetStrWVTemp
				.attribute("class_retweet_count_cats"));
		writeInstances(testingSetStrWVTemp);
		System.out.println("Done");
		printDifference(trainingSet, testingSetStrWVTemp);
		System.out.println("----------------------");
		printDifference(testingSetStrWVTemp, trainingSet);
	}

	private static void printDifference(Instances tr1,
			Instances tr2) {
		
		ArrayList<String> ta1 = getAttributeNames(tr1);
		ArrayList<String> ta2 = getAttributeNames(tr2);
		
		ta1.removeAll(ta2);
		for(String t : ta1){
			System.out.println("DIFF: " + t);
		}
	}

	private static Instances addClassCategory(Instances t) {
		FastVector categs = new FastVector();
		categs.addElement("0_1");
		categs.addElement("2_3_4_5_7_8_9");
		categs.addElement("15_20_51_185");
		t.insertAttributeAt(new Attribute("class_retweet_count_cats", categs),
				t.numAttributes());

		for (int i = 0; i < t.numInstances(); i++) {
			if (t.instance(i).value(t.attribute("class_retweet_count")) < 2) {
				t.instance(i).setValue(t.attribute("class_retweet_count_cats"),
						"0_1");
			} else if (t.instance(i).value(t.attribute("class_retweet_count")) < 10) {
				t.instance(i).setValue(t.attribute("class_retweet_count_cats"),
						"2_3_4_5_7_8_9");
			} else
				t.instance(i).setValue(t.attribute("class_retweet_count_cats"),
						"15_20_51_185");
		}
		return t;
	}

	public static Instances loadARFF(String fname) throws IOException {
		ArffLoader t = new ArffLoader();
		t.setSource(new File(fname));
		Instances data = t.getDataSet();
		return data;
	}

	public static ArrayList<String> getAttributeNames(Instances t) {

		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < t.numAttributes(); i++) {
			names.add(t.attribute(i).name());
		}
		return names;
	}

	public static Instances strToWVAndSelect(Instances t, String pName,
			boolean prefix) throws Exception {

		// t.insertAttributeAt(testingSet.attribute("class_retweet_count_cats"),
		// t.numAttributes());
		t.setClass(t.attribute("class_retweet_count_cats"));

		// Nominal to String
		NominalToString ns = new NominalToString();
		ns.setInputFormat(t);
		// ns.setAttributeIndexes("1");
		ns.setOptions(new String[] { "-C", "first" });
		t = Filter.useFilter(t, ns);

		// String to Word Vector
		StringToWordVector swv = new StringToWordVector();
		swv.setInputFormat(t);
		swv.setUseStoplist(true);
		if (prefix) {
			// swv.setAttributeNamePrefix(cpName);
			swv.setOptions(new String[] { "-P", pName });
		}
		t = Filter.useFilter(t, swv);

		// Attribute evaluation
		AttributeSelection filter = new AttributeSelection();
		GainRatioAttributeEval eval = new GainRatioAttributeEval();
		Ranker search = new Ranker();
		if (t.numAttributes() > 5000) {
			search.setNumToSelect(5000);
		}
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(t);

		// generate new data
		t = Filter.useFilter(t, filter);
		return t;
	}

	public static Instances strToWVAndSelect_(Instances t, String pName,
			boolean prefix) throws Exception {

		// t.insertAttributeAt(testingSet.attribute("class_retweet_count_cats"),
		// t.numAttributes());
		t.setClass(t.attribute("class_retweet_count_cats"));

		// Nominal to String
		NominalToString ns = new NominalToString();
		ns.setInputFormat(t);
		// ns.setAttributeIndexes("1");
		ns.setOptions(new String[] { "-C", "first" });
		t = Filter.useFilter(t, ns);

		// String to Word Vector
		StringToWordVector swv = new StringToWordVector();
		swv.setInputFormat(t);
		swv.setUseStoplist(true);
		if (prefix) {
			// swv.setAttributeNamePrefix(cpName);
			swv.setOptions(new String[] { "-P", pName + "_" });
		}
		t = Filter.useFilter(t, swv);

		// Attribute evaluation
		AttributeSelection filter = new AttributeSelection();
		GainRatioAttributeEval eval = new GainRatioAttributeEval();
		Ranker search = new Ranker();
		if (t.numAttributes() > 5000) {
			search.setNumToSelect(5000);
		}
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(t);

		// generate new data
		t = Filter.useFilter(t, filter);
		return t;
	}

	private static void writeInstances(Instances training) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File("test_output.arff"));
		saver.writeBatch();
	}

	private static Instances NomToStr(Instances t) throws Exception {
		NominalToString ns = new NominalToString();
		ns.setInputFormat(t);
		t = Filter.useFilter(t, ns);
		return t;
	}
}
