/* Copyright © 2016 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.exigen.eis.sonar.checkstyle;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Base class for regular unit tests. Code borrowed from checkstyle test infrastructure.
 * 
 * @author anorkus
 * @since 1.0
 * @see <a href="https://github.com/checkstyle/checkstyle">BaseCheckTestSupport.java on checkstyle repostory.</a>
 */
public abstract class BaseCheckTestSupport {
    /**
     * A brief logger that only display info about errors
     */
    protected static class BriefLogger
            extends DefaultLogger {
        public BriefLogger(OutputStream out) throws UnsupportedEncodingException {
            super(out, true);
        }

        @Override
        public void auditStarted(AuditEvent evt) {
        }

    }

    protected final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    protected final PrintStream stream = new PrintStream(baos);
    protected final Properties props = new Properties();

    public static DefaultConfiguration createCheckConfig(Class<?> clazz) {
        return new DefaultConfiguration(clazz.getName());
    }

    protected Checker createChecker(Configuration checkConfig)
            throws Exception {
        final DefaultConfiguration dc = createCheckerConfig(checkConfig);
        final Checker checker = new Checker();
        // make sure the tests always run with default error messages (language-invariant)
        // so the tests don't fail in supported locales like German
        final Locale locale = Locale.ROOT;
        checker.setLocaleCountry(locale.getCountry());
        checker.setLocaleLanguage(locale.getLanguage());
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(dc);
        checker.addListener(new BriefLogger(stream));
        return checker;
    }

    protected DefaultConfiguration createCheckerConfig(Configuration config) {
        final DefaultConfiguration dc =
                new DefaultConfiguration("configuration");
        final DefaultConfiguration twConf = createCheckConfig(TreeWalker.class);
        // make sure that the tests always run with this charset
        dc.addAttribute("charset", "UTF-8");
        dc.addChild(twConf);
        twConf.addChild(config);
        return dc;
    }

    protected static String getPath(String filename)
            throws IOException {
        return new File("src/test/resources/com/exigen/eis/sonar/checkstyle/" + filename).getCanonicalPath();
    }

    protected static String getSrcPath(String filename) throws IOException {
        return new File("src/test/java/com/exigen/eis/sonar/checkstyle/" + filename).getCanonicalPath();
    }

    protected void verify(Configuration aConfig, String fileName, String... expected)
            throws Exception {
        verify(createChecker(aConfig), fileName, fileName, expected);
    }

    protected void verify(Checker checker, String fileName, String... expected)
            throws Exception {
        verify(checker, fileName, fileName, expected);
    }

    protected void verify(Checker checker,
                          String processedFilename,
                          String messageFileName,
                          String... expected)
            throws Exception {
        verify(checker,
                new File[]{new File(processedFilename)},
                messageFileName, expected);
    }

    protected void verify(Checker checker,
                          File[] processedFiles,
                          String messageFileName,
                          String... expected)
            throws Exception {
        stream.flush();
        final List<File> theFiles = Lists.newArrayList();
        Collections.addAll(theFiles, processedFiles);
        final int errs = checker.process(theFiles);

        // process each of the lines
        final ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        final LineNumberReader lnr =
                new LineNumberReader(new InputStreamReader(bais));

        for (int i = 0; i < expected.length; i++) {
            final String expectedResult = messageFileName + ":" + expected[i];
            final String actual = lnr.readLine();
            assertEquals("error message " + i, expectedResult, actual);
        }

        assertEquals("unexpected output: " + lnr.readLine(),
                expected.length, errs);
        checker.destroy();
    }

    /**
     * Gets the check message 'as is' from appropriate 'messages.properties'
     * file.
     *
     * @param messageKey the key of message in 'messages.properties' file.
     * @param arguments  the arguments of message in 'messages.properties' file.
     */
    public String getCheckMessage(String messageKey, Object... arguments) {
        Properties pr = new Properties();
        try {
            pr.load(getClass().getResourceAsStream("messages.properties"));
        } catch (IOException e) {
            return null;
        }
        return format(pr.getProperty(messageKey), arguments);
    }
}
