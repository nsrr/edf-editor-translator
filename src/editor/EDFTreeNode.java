package editor;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A tree node representing an EDF file
 */
@SuppressWarnings("serial")
public class EDFTreeNode extends DefaultMutableTreeNode {
	
	// universal id for the tree node
    private long uid;
    
    /*
     * keep the info of host file
     * uid is not a good idea too store the information
     * Fangping, 08/26/2010
     */
    private File hostFile; 
       
    /**
     * Default constructor of EDFTreeNode
     */
    public EDFTreeNode() {
        super();
    }
    
    /**
     * Constructs an EDF tree node using a host file
     * @param file the file this node pointed to 
     */
    public EDFTreeNode(File file){
        super(file.getName());
        setHostFile(file);
    }
    
    /**
     * Constructs an EDF tree node with a name
     * @param name the name of this tree node
     */
    public EDFTreeNode(String name){
        super(name);
    }

    /**
     * Sets the uid for this node
     * @param uid the uid to be set
     */
    public void setUid(long uid) {
        this.uid = uid;
    }

    /**
     * Gets the uid for this node
     * @return the uid of this node
     */
    public long getUid() {
        return uid;
    }

    /**
     * Sets the host file of this tree node
     * @param hostFile the host file
     */
    public void setHostFile(File hostFile) {
        this.hostFile = hostFile;
    }

    /**
     * Returns the host file of this tree node
     * @return the host file
     */
    public File getHostFile() {
        return hostFile;
    }
}
