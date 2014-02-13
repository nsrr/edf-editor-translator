package editor;

import java.net.URL;

import javax.help.*;

public class EDFEditorHelp {

    private HelpSet hs;

    public EDFEditorHelp() {
        String helpHS = "/helpfiles/mhHelpSet.hs";
        ClassLoader cl = Main.class.getClassLoader();
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            hs = new HelpSet(null, hsURL);
        } catch (Exception e) {
            System.out.println("HelpSet " + e.getMessage());
            System.out.println("HelpSet " + helpHS + " not found");
        }
    }
}
