function StartMe_cmdPars(edfName, edfPath, xmlName, xmlPath)
    
    global needOpenDialog;
    needOpenDialog = logical(0);
    
    global EdfFilePath;
    global EdfFileName;
    global XmlFilePath;
    global XmlFileName;
    EdfFilePath = edfName;
    EdfFileName = edfPath;
    XmlFilePath = xmlName;
    XmlFileName = xmlPath;
    
    EDF_View({})
end