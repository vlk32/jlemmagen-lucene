/*
 * Copyright 2013 Michal Hlavac <hlavki@hlavki.eu>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.lemmagen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michal Hlavac <hlavki@hlavki.eu>
 */
public class SlovakFilterTest extends BaseTokenStreamTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(SlovakFilterTest.class);


    @Test
    public void doFilter() throws IOException {
        assertTrue(analyze(getAnalyzer(), "/adam-sangala.txt") < 8000);
    }


    private Analyzer getAnalyzer() {
        return new Analyzer() {

            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                StandardTokenizer source = new StandardTokenizer();

                TokenStream filter = new ASCIIFoldingFilter(new LemmagenFilter(
                    new LowerCaseFilter(new StandardFilter(source)), "lemmagen/lang/sk"));
                return new Analyzer.TokenStreamComponents(source, filter);
            }
        };
    }


    private int analyze(Analyzer analyzer, String resource) {
        TokenStream stream = null;
        InputStream in = null;
        Set<Integer> words = new HashSet<>();
        try {
            long start = System.currentTimeMillis();
            in = SlovakFilterTest.class.getResourceAsStream(resource);
            stream = analyzer.tokenStream(null, new InputStreamReader(in));
            stream.reset();
            int count = 0;
            while (stream.incrementToken()) {
                String word = stream.getAttribute(CharTermAttribute.class).toString();
                words.add(word.hashCode());
                count++;
            }
            stream.end();
            long end = System.currentTimeMillis();
            LOG.info("All words count: {}", count);
            LOG.info("Words set count: {}", words.size());
            LOG.info("Commpression: {}", ((double) words.size() / count));
            LOG.info("TIME: {} ms", (end - start));
            return words.size();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
            throw new RuntimeException(e);
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                LOG.error("Can't close stream", e);
            }
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                LOG.error("Can't close stream", e);
            }
        }

    }
}
