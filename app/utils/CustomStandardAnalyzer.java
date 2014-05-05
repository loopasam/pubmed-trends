package utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class CustomStandardAnalyzer extends StopwordAnalyzerBase {

    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    public CustomStandardAnalyzer(Version matchVersion) {
        this(matchVersion, STOP_WORDS_SET);
    }

    public CustomStandardAnalyzer(Version matchVersion, CharArraySet stopWords) {
        super(matchVersion, stopWords);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        //http://lucene.apache.org/core/4_0_0/analyzers-common/org/apache/lucene/analysis/standard/StandardTokenizer.html
        StandardTokenizer tokenizer = new StandardTokenizer(matchVersion, reader);

        TokenStream tok = new StandardFilter(matchVersion, tokenizer);
        tok = new LowerCaseFilter(matchVersion, tok);

        return new TokenStreamComponents(tokenizer, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }

}
