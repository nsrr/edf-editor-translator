package listener;

import java.io.*;

import javax.swing.filechooser.FileFilter;


/*
 * five types of File filters
 * (1) EDF filter: new EDFFileFilter({"edf"}, "EDF Files");
 * (2) EIA filter: new EDFFileFilter({"eia"}, "EIA Files");
 * (3) ESA filter: new EDFFileFilter({"esa"}, "ESA Files");
 * (4) EIA filter: new EDFFileFilter({"eia", "edf"}, "EIA and EDF Files");
 * (5) ESA filter: new EDFFileFilter({"esa", "edf"}, "ESA and EDF Files")
 */
public class EDFFileFilter extends FileFilter {
    
    private String[] allowedExtensions = null;
    private String description = "";
    
    public EDFFileFilter(String[] myAllowedExtensions, String myDescription) {
        this.allowedExtensions = myAllowedExtensions;
        this.description = myDescription;   
    }

    public boolean accept(File pathname) {
        String fileName = pathname.getName().toLowerCase();
        if (pathname.isDirectory())
            return true;
        for (int i = 0; i < allowedExtensions.length; i++) {
            if (fileName.endsWith("." + allowedExtensions[i].toLowerCase()))
                return true;                
        }
        
        return false;  
    }
    
    public String getDescription() {
        return description;
    }
}
