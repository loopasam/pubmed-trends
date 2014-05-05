/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import models.Citation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Logger;
import play.jobs.Job;
import play.vfs.VirtualFile;
import utils.CustomStandardAnalyzer;

/**
 *
 * @author loopasam
 */
public class SimpleIndexJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("Starting index...");

        //Needs to take care of dashes and commas - as now removes everything
        Analyzer analyzer = new CustomStandardAnalyzer(Version.LUCENE_47);
        ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(analyzer, 2, 3);
//        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

        Directory directory = FSDirectory.open(VirtualFile.fromRelativePath("/lucene").getRealFile());
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, shingleAnalyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        String pmid = "23667399";
        String title = "eQTL Mapping Using RNA-seq Data.";
        String abstractText = "pouet As RNA-seq is replacing gene expression microarrays "
                + "to assess genome-wide transcription abundance, gene "
                + "expression Quantitative Trait Locus (eQTL) studies using "
                + "RNA-seq have emerged. RNA-seq delivers two novel features "
                + "that are important for eQTL studies. First, it provides information "
                + "on allele-specific expression (ASE), which is not available from gene "
                + "expression microarrays. Second, it generates unprecedentedly "
                + "rich data to study RNA-isoform expression. In this paper, "
                + "we review current methods for eQTL mapping using ASE and "
                + "discuss some future directions. We also review existing "
                + "works that use RNA-seq data to study RNA-isoform expression "
                + "and we discuss the gaps between these works and isoform-specific eQTL mapping.";

        String created = "2013-01-13 00:00:00.0";

        Citation citation = new Citation(pmid, title, abstractText, created);

        Document doc = new Document();

        doc.add(new Field("title", citation.title, TextField.TYPE_STORED));
        doc.add(new Field("date", DateTools.dateToString(citation.created, DateTools.Resolution.MINUTE), TextField.TYPE_STORED));
        doc.add(new Field("abstract", citation.abstractText, TextField.TYPE_STORED));

        iwriter.addDocument(doc);

        String title1 = "foo bar RNA-pvr Datas interactions.";
        Citation citation1 = new Citation("123456", title1, abstractText, "2012-01-13 00:00:00.0");
        Document doc1 = new Document();
        doc1.add(new Field("title", citation1.title, TextField.TYPE_STORED));
        doc1.add(new Field("date", DateTools.dateToString(citation1.created, DateTools.Resolution.MINUTE), TextField.TYPE_STORED));
        doc.add(new Field("abstract", "foo barr", TextField.TYPE_STORED));

        iwriter.addDocument(doc1);

        iwriter.close();

        Logger.info("index done.");
    }

}
