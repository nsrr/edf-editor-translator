package editor;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

public class EDFTreeNode extends DefaultMutableTreeNode{
    
    private long uid; // universal id for the tree node
    /*
     * keep the info of host file
     * uid is not a good idea too store the information
     * /Fangping, 08/26/2010
     */
    private File hostFile; 
       
    public EDFTreeNode() {
        super();
    }
    
    public EDFTreeNode(File file){
        super(file.getName());
        setHostFile(file);
    }
    
    public EDFTreeNode(String name){
        super(name);
    }


    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getUid() {
        return uid;
    }

    public void setHostFile(File hostFile) {
        this.hostFile = hostFile;
    }

    public File getHostFile() {
        return hostFile;
    }
}
