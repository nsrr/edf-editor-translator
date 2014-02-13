package editor;

import java.awt.Color;
import java.awt.Font;

import java.awt.Graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.io.File;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.text.SimpleDateFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;

/**
 * this class is to customize the layout of info panes
 * including the file information pane, the log pane, and the message pane
 * To be customized items: foreground/background color/font, keyword color/font,
 * plain text color/font, and so on.
 */


/* EDFInfoPane should extend JTextPane, instead of JEditorPane which is better for 
 * displaying already formatted data but less fantastic if things whant to be
 * manipulated by oneself.
 */

public class EDFInfoPane extends JTextPane{
    
    protected static String fontName = Font.MONOSPACED;
    protected static Color foregroundColor = new Color(0, 255, 0);
    protected static Color backgroundColor = new Color(0, 55, 0);
    //theme, like new task, add files, apply template, save
    protected static MutableAttributeSet theme = new SimpleAttributeSet();
    //content
    protected static MutableAttributeSet content = new SimpleAttributeSet();
    //actions registered in log, such as open, save, export, add, remove, delete
    protected static MutableAttributeSet logaction = new SimpleAttributeSet();
    //commented by fangping, 10/07/2010
    //protected static final String finfoTitle = "======== Source File Info ========\n";
    protected static final String finfoTitle = "";
    protected static final int finfoTitleLength = finfoTitle.length();
    protected static final String logTitle = "";
    protected static final int logTitleLength = logTitle.length();
    
    public static final int FINFO = 0;
    public static final int LOG = 1;
    
    public static final String file_name = "Name: ";
    public static final String file_path = "Path: ";
    public static final String file_size = "Size: ";
    public static final String file_date = "Date: ";
    
    public static final String write_mode = "Write Mode: ";
    public static final String write_mode_overwrite = "Overwrite";
    public static final String mode_mode_nonoverwrite = "Duplicate";
    public static final String selection_mode = "Selection Mode: ";
    public static final String selection_mode_by_dir = "By Directory";
    public static final String selection_mode_by_file = "By File";
    public static final String source_dir = "Source Path: ";
    public static final String output_dir = "Output Path: ";
    public static final String task_files = "Files: ";    

    public static final String html_table_beg= "<tr><th>";
    public static final String html_table_mid = "</th><td>";
    public static final String html_table_end = "</td></tr>";
    public static final String tale_tag_start =  "<table width = 250>";
    public static final String table_tag_end = "</table>";
   
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem clearMenu = new JMenuItem("Clear");
   // private JMenuItem copyMenu = new JMenuItem("Copy");
   // private JMenuItem selectallMenu = new JMenuItem("Select All");
   // private JMenuItem findMenu = new JMenuItem("Find");
   // private JMenuItem saveasMenu = new JMenuItem("Save As");
    
    static{
        StyleConstants.setForeground(theme, foregroundColor);
        StyleConstants.setForeground(content, foregroundColor);
        StyleConstants.setForeground(logaction, foregroundColor);
    }
    
    private StyledDocument doc;
    
    public EDFInfoPane(int paneType) {       
        super();
        this.setOpaque(false);
        
        //this is needed if using Nimbus laf
        //see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6687960
        this.setBackground(new Color(0, 0, 0, 0));
        this.setForeground(foregroundColor);
        
        setupDoc();
        setPopupMenu();
        addMouseListener(new InfoPaneMouseAdapter());
        
        //obsolete
/*         if (paneType == FINFO)
            this.setText("");
        else
            this.setText(logTitle); */
    }
    
    private void setupDoc(){
        doc = this.getStyledDocument();        
    }
    
    public void outputMessage(String message){
        try {
            doc.insertString(doc.getLength(), message, theme);
        } catch (BadLocationException e) {;}
    }
    
    
    @Override
    protected void paintComponent(Graphics g){
        g.setColor(backgroundColor);
        g.fillRect(0,0, getWidth(), getHeight());
        
        super.paintComponent(g);
    }
    
    //print the message header for each type of events
    public static void printMessageHeader(String headerText){
        StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);
        Style labelStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        
        JLabel label = new JLabel(MainWindow.messageIcon);
        StyleConstants.setComponent(labelStyle, label);
        
        headerText = Utility.currentTimeToString() + ": " + headerText;
        try {
            //document.insertString(document.getLength(), "ignored", labelStyle);
            document.insertString(document.getLength(), headerText, theme);
            System.out.println("been here");
            System.out.println(document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public void setPopupMenu(){
        popup.add(clearMenu);      
        popup.setLightWeightPopupEnabled(true);

        
        clearMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    clearMessage();
                }
            });
    }
    
    //erase all content in the console window    
    private void clearMessage(){
        this.setText("");
    }

    public static MutableAttributeSet getTheme() {
        return theme;
    }
    
    /*
     * output file information in table to editorPane
     */
    public void outputFileInfoWithHtml(File file) {
        this.setContentType("text/html");
        String name = "", path = "", size = "", date= "";
        if (file != null) {
            name = file.getName();
            path = file.getParent();
            size = formatFileSize(file.length());
            date = formatFileDate(file.lastModified());
        }
        
        String list = tabulateFileInfo(name, path, size, date);      
        this.setText(EDFInfoPane.finfoTitle + list);
    }

    /*
     * output task information in table to editorPane
     */
    public void outputTaskInfoWithHtml() {
        this.setContentType("text/html");
        String srcDir, outputDir, taskFiles, writeMode, selectionMode;
        
        //1.
        srcDir = MainWindow.getSourceDirectoryText();
        //2.4.
        if (MainWindow.getWriteMode() == MainWindow.overwrite_mode){
            outputDir = srcDir;
            writeMode = "OVERWRITE to source files";
        }
        else{
            outputDir = MainWindow.getWorkingDirectory().getPath();
            writeMode = "DUPLICATE source files";
        }
        //3.
        int count = MainWindow.srcEdfFiles.size();
        taskFiles = count + ((count > 0)? " files" :" file ");
        //5.
        if (MainWindow.getSelectionMode() == MainWindow.selection_mode_by_dir)
            selectionMode = selection_mode_by_dir;
        else
            selectionMode = selection_mode_by_file;
        

        String list = tabulateTaskInfo(srcDir, outputDir, taskFiles, writeMode, selectionMode);
        this.setText(EDFInfoPane.finfoTitle + list);
    }


    private String formatFileSize(long size){
        String lenstr = "";
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(',');
        
        df.setDecimalFormatSymbols(dfs);
        lenstr = df.format(toMegaBytes(size)) + "M bytes";
        
        return lenstr;
    }
    
    private long toMegaBytes(long size){
        int mb = 1000*1000;
        return (size < 1)? 1: (size/mb);
    }
    
    private String formatFileDate(long date){        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");      
        String datestr = sdf.format(date);       
        
        return datestr;
    }

    
    private String tabulateFileInfo(String name, String path,  String size, String date){
        name = appendformattedHeader(file_name, name);
        path = appendformattedHeader(file_path, path);
        size = appendformattedHeader(file_size, size);
        date = appendformattedHeader(file_date, date);
        
        String content = name + path + size + date;
        
        return tale_tag_start  + content + table_tag_end;
    }
    
    private String appendformattedHeader(String header, String content){
        return html_table_beg + header + html_table_mid + content + html_table_end;
    }
    
    
    private String tabulateTaskInfo(String srcDir, String outputDir, String taskFilesCount, String writeMode, String selectionMode){
        srcDir = appendformattedHeader(source_dir, srcDir);
        outputDir = appendformattedHeader(output_dir, outputDir);
        taskFilesCount = appendformattedHeader(task_files, taskFilesCount);
        writeMode = appendformattedHeader(write_mode, writeMode);
        selectionMode = appendformattedHeader(selection_mode, selectionMode);
        
        String content = srcDir + outputDir + taskFilesCount + writeMode + selectionMode;
        
        return  tale_tag_start  + content + table_tag_end;
    }

    class InfoPaneMouseAdapter extends MouseAdapter{
        
        public void mousePressed(MouseEvent e){
            if (e.getButton() == MouseEvent.BUTTON3){
                popup.show((JComponent)e.getSource(), e.getX(), e.getY());
            }
        }        
    }   
     
}
