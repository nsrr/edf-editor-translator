package translator.logic.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import translator.logic.AbstractTranslatorFactory;
import translator.logic.EmblaTranslatorFactory;

public class TestTranslateUtility {
  
  private AbstractTranslatorFactory translator;

  @Before
  public void setUp() throws Exception {
//    translator = new EmblaTranslatorFactory();
  }

  @After
  public void tearDown() throws Exception {
//    translator = null;
  }

  @Test
  public void testGetUntranslatedEmblaEventSignalLocation() {
    String s = "Resp.Flow-Cannula.Nasal";
    String[] s1 = {"Resp", "Flow", "Cannula", "Nasal"};
    System.out.println(Arrays.deepToString(s.split("[.-]")));
    assertArrayEquals(s1, s.split("[.-]"));
  }

}
