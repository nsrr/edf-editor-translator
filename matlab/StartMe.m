function EDF_View(edfName, edfPath, xmlName, xmlPath)

    global EdfFilePath;
    global EdfFileName;
    global XmlFilePath;
    global XmlFileName;
    EdfFilePath = edfName;
    EdfFileName = edfPath;
    XmlFilePath = xmlName;
    XmlFileName = xmlPath;
    
    %EdfFilePath = 'D:\ABC\';
    %EdfFileName = 'ABC_012345.edf';
    %XmlFilePath = 'D:\ABC\';
    %XmlFileName = 'ABC_012345_MIMI.xml';
    
    EDF_View({})
end