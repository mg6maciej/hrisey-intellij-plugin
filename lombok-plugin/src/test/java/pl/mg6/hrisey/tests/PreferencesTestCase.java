package pl.mg6.hrisey.tests;

import de.plushnikov.lombok.LombokParsingTestCase;

import java.io.IOException;

public class PreferencesTestCase extends LombokParsingTestCase {

  @Override
  protected boolean shouldCompareCodeBlocks() {
    return false;
  }

  public void testPreferencesSimple() throws IOException {
    doTest();
  }

  public void testPreferencesObject() throws IOException {
    doTest();
  }
}
