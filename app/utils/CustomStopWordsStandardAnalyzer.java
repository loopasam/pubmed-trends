package utils;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class CustomStopWordsStandardAnalyzer extends StopwordAnalyzerBase {

    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    public CustomStopWordsStandardAnalyzer(Version matchVersion) {
        this(matchVersion, STOP_WORDS_SET);
    }

    public CustomStopWordsStandardAnalyzer(Version matchVersion, CharArraySet stopWords) {
        super(matchVersion, stopWords);
    }

    //No stop word removing - just convert into lower case and tokenise
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        //http://lucene.apache.org/core/4_0_0/analyzers-common/org/apache/lucene/analysis/standard/StandardTokenizer.html
        StandardTokenizer tokenizer = new StandardTokenizer(matchVersion, reader);

        TokenStream tok = new StandardFilter(matchVersion, tokenizer);

        tok = new StopFilter(Version.LUCENE_47, tok, StandardAnalyzer.STOP_WORDS_SET);

        //Custom list of stop words
        List<String> stopWords = Arrays.asList("we", "pmid!",
                "were", "from", "reply", "can", "between", "using", "used", "however", "which", "our", "among", "while");
        tok = new StopFilter(Version.LUCENE_47, tok, StopFilter.makeStopSet(Version.LUCENE_47, stopWords));

        tok = new LowerCaseFilter(matchVersion, tok);

        return new TokenStreamComponents(tokenizer, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }

}
