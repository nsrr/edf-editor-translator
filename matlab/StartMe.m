function varargout = EDF_View(edfName, edfPath, xmlName, xmlPath)

    global EdfFilePath;
    global EdfFileName;
    global XmlFilePath;
    global XmlFileName;
    %EdfFilePath = 'D:\ABC\';
    %EdfFileName = 'ABC_012345.edf';
    %XmlFilePath = 'D:\ABC\';
    %XmlFileName = 'ABC_012345_MIMI.xml';
    EdfFilePath = edfName;
    EdfFileName = edfPath;
    XmlFilePath = xmlName;
    XmlFileName = xmlPath;
    
    EDF_View({})