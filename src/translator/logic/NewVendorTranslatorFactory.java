package translator.logic;

import java.util.HashMap;

import org.w3c.dom.Element;

/**
 * This class is used for demo
 * @author wei wang, 2014-10-15
 */
public class NewVendorTranslatorFactory extends AbstractTranslatorFactory {
	
	public NewVendorTranslatorFactory() {
		super();
		System.out.println("=================================================================");
		System.out.println("NewVendor Factory:");
	}

	@Override
	public boolean read(String edfFile, String annotationFile,
			String mappingFile) {
		System.out.println("   >>> Inside NewVendorTranslatorFactory read");
		return false;
	}

	@Override
	public boolean translate() {
		System.out.println("   >>> Inside NewVendorTranslatorFactory translate");
		return false;
	}

	@Override
	public boolean write(String outputFile) {
		System.out.println("   >>> Inside NewVendorTranslatorFactory write");
		System.out.println("=================================================================");
		return false;
	}

	@Override
	public boolean write2JSON(String outputFile) {
		System.out.println("   >>> Inside NewVendorTranslatorFactory writeToJSON");
		return false;
	}

	@Override
	public HashMap<String, Object>[] readMapFile(String mapFile) {
		return null;
	}

  @Override
  public String getSignalLocationFromEvent(Element scoredEvent,
      String annLocation) {
    // TODO Auto-generated method stub
    return null;
  }

}
