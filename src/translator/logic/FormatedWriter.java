package translator.logic;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

public class FormatedWriter {
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
    
    /**
     * Translates the XML format to json file format
     * @param doc translates the Document to json file format
     * @return the json output
     */
    public static String xml2json(Document doc) {
    	TransformerFactory tf = TransformerFactory.newInstance();
    	Transformer transformer;
    	String output = "";
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();	
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
	    	output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		try {
            JSONObject xmlJSONObj = XML.toJSONObject(output);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonPrettyPrintString);
            return jsonPrettyPrintString;
        } catch (JSONException je) {
            System.out.println(je.toString());
            return "";
        }
    }
    
    /**
     * TODO: to be implemented
     * @param doc
     * @return
     */
    public static String xml2xml(Document doc) {
    	return "";
    }
}