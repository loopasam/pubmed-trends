package jobs;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;

public class DumpIndex extends Job {

	public void doJob() throws Exception {
		
		Logger.info("Dumping index...");
		Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
		DirectoryReader ireader = DirectoryReader.open(directory);

		Terms terms = SlowCompositeReaderWrapper.wrap(ireader).terms("title"); 
		TermsEnum iterator = terms.iterator(null);
		BytesRef byteRef = null;

		HashMap<String,Integer> map = new HashMap<String,Integer>();
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);

		while ((byteRef = iterator.next()) != null) {
			String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
			int freq = iterator.docFreq();

			if(freq > 5){
				map.put(term, freq);
			}
		}
		sorted_map.putAll(map);
		System.out.println("results: "+sorted_map);

	}

	class ValueComparator implements Comparator<String> {
		Map<String, Integer> base;
		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
