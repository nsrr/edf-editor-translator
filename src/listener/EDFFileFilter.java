package listener;

import java.io.*;

import javax.swing.filechooser.FileFilter;


/**
 * EDF file filters: five types of File filters
 * (1) EDF filter: new EDFFileFilter({"edf"}, "EDF Files");
 * (2) EIA filter: new EDFFileFilter({"eia"}, "EIA Files");
 * (3) ESA filter: new EDFFileFilter({"esa"}, "ESA Files");
 * (4) EIA filter: new EDFFileFilter({"eia", "edf"}, "EIA and EDF Files");
 * (5) ESA filter: new EDFFileFilter({"esa", "edf"}, "ESA and EDF Files")
 */
public class EDFFileFilter extends FileFilter {
    
    private String[] allowedExtensions = null;
    private String description = "";
    
    /**
     * Set default allowed extensions and description
     * @param myAllowedExtensions allowed extension array
     * @param myDescription description
     */
    public EDFFileFilter(String[] myAllowedExtensions, String myDescription) {
        this.allowedExtensions = myAllowedExtensions;
        this.description = myDescription;   
    }

    /**
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
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
    
    /**
     * Return the description of this EDF file filter
     */
    public String getDescription() {
        return description;
    }
}
