function varargout = EDF_View(varargin)
%      EDF_VIEW MATLAB code for EDF_View.fig
%      EDF_VIEW, by itself, creates a new EDF_VIEW or raises the existing
%      singleton*.
%
%      H = EDF_VIEW returns the handle to a new EDF_VIEW or the handle tof
%      the existing singleton*.
%
%      EDF_VIEW('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in EDF_VIEW.M with the given input arguments.
%
%      EDF_VIEW('Property','Value',...) creates a new EDF_VIEW or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before EDF_View_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to EDF_View_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help EDF_View

% Last Modified by GUIDE v2.5 23-Aug-2012 00:07:30

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @EDF_View_OpeningFcn, ...
    'gui_OutputFcn',  @EDF_View_OutputFcn, ...
    'gui_LayoutFcn',  [] , ...
    'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT
%------------------------------------------------------ EDF_View_OpeningFcn
% --- Executes just before EDF_View is made visible.
function EDF_View_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to EDF_View (see VARARGIN)

% Choose default command line output for EDF_View
handles.output = hObject;
%------------------------------------------------------- Set Default Values
% Define Window PopUpTime Menu Entries
handles.epoch_menu = {' 5 sec'; '10 sec'; '15 sec'; '20 sec'; '25 sec'; ...
    '30 sec'; ' 1 min'; ' 2 min'; ' 5 min'; '10 min'; '20 min'; ...
    '30 min'; '60 min'};
handles.epoch_menu_values = [5; 10; 15; 20; 25; 30; 60; 120; 300; 600; ...
    1200; 1800; 3600];
handles.epoch_menu_value = 6;
handles.epoch_menu_num_tick_sections = [5; 5; 5; 6; 5; 5;
    6; 6; 5; 5; 5; 6; 6];

set(handles.PopMenuWindowTime,'String',handles.epoch_menu);
set(handles.PopMenuWindowTime,'Value',handles.epoch_menu_value);

% Set epoch count text strings to null
set(handles.textTotalEpochs,'String',' ');
set(handles.textCurrent30secEpochs,'String',' ');


% Set GUI status text to null
set(handles.TextInfo, 'String',' ');
set(handles.TextSignalValue, 'String',' ');
set(handles.EditEpochNumber, 'String',' ');

% Set EDF list font to fixed width
fontName = get(0,'FixedWidthFontName');
set(handles.ListBoxPatientInfo, 'FontName', fontName);

% Constants 
handles.RIGHT_ARROW_KEY = 29;
handles.LEFT_ARROW_KEY = 28;
handles.UP_ARROW_KEY = 30;
handles.DOWN_ARROW_KEY = 31;
handles.CONTROL_MODIFIER = 'control';


% Load and display button 
%Coloca una imagen en cada botón
[a,map]=imread('start.jpg');
[r,c,d]=size(a); 
x=ceil(r/30); 
y=ceil(c/30); 
g=a(1:x:end,1:y:end,:);
g(g==255)=5.5*255;
set(handles.pb_GoToStart,'String','');
set(handles.pb_GoToStart,'CData',g);

[a,map]=imread('left.jpg');
[r,c,d]=size(a); 
x=ceil(r/30); 
y=ceil(c/30); 
g=a(1:x:end,1:y:end,:);
g(g==255)=5.5*255;
set(handles.pbLeftEpochButton,'String','');
set(handles.pbLeftEpochButton,'CData',g);

[a,map]=imread('right.jpg');
[r,c,d]=size(a); 
x=ceil(r/30); 
y=ceil(c/30); 
g=a(1:x:end,1:y:end,:);
g(g==255)=5.5*255;
set(handles.pbRightButton,'String','');
set(handles.pbRightButton,'CData',g);

[a,map]=imread('end.jpg');
[r,c,d]=size(a); 
x=ceil(r/30); 
y=ceil(c/30); 
g=a(1:x:end,1:y:end,:);
g(g==255)=5.5*255;
set(handles.pbGoToEnd,'String','');
set(handles.pbGoToEnd,'CData',g);

%---------------------------------------------------------- Operating Flags
handles.EDF_LOADED = 0;
handles.XML_LOADED = 0;
handles.FlagAnn = 0;
%--------------------------------------------------------------------------


% GUI Status Varaibles
handles.c_axes =[];
handles.ChSelectionH =[];
handles.FilterSettingH =[];
handles.ActiveCh = [];
handles.Axes1OrgPos = get(handles.axes1,'outerposition');
handles.SliderOrgPos = get(handles.SliderTime,'position');
handles.FileInfo = [];
handles.SelectedCh = [];
handles.FilterPara = [];
handles.Sel = 1; % Select first signal

% Subset of channel selection information
handles.ChInfo = [];
handles.FlagSelectedCh = 0;
handles.FlagChInfo = 0;

% Clear hypnogram and signal axes labels
set(handles.axes1,'xTickLabel','','yTickLabel','');
set(handles.axes2,'xTickLabel','','yTickLabel','');
handles.sleep_stage_width = 30;
handles.minimum_cursor_width = 20;
handles.auto_scale_height = 2;
handles.auto_scale_factor = [];

%--------------------------------------------------------------------------
% Clear axes1 (signals) and axes2(hypnogram)
% Clear axes following second call to viewer, may want to allow multiple
% singleton

% Clear signal 
cla(handles.axes1);
set(handles.axes1,'XGrid','off')
set(handles.axes1,'YGrid','off')

% Clear hypnogram text
cla(handles.axes2);
set(handles.axes2,'XGrid','off')
set(handles.axes2,'YGrid','off')

% Clear header list box
set(handles.ListBoxPatientInfo, 'string', '  ');
set(handles.ListBoxComments, 'Value', 1);

% Clear list boz
set(handles.ListBoxComments, 'string', '  ');
set(handles.ListBoxComments, 'Value', 1);

%--------------------------------------------------------------------------
% Adding new variables intended to speed up access time while scrolling
% Will take longer to load.

% Varaibles to change figure title
figureDefaultTitle = 'EDF View';
set(handles.figure1, 'name', figureDefaultTitle);
handles.figureDefaultTitle = figureDefaultTitle;

%--------------------------------------------------------------------------
% Adding varaibles to accessesing multiple edf's in the same folder faster
handles.openStartFolder = cd;
handles.fileSeperator = '\';
handles.openStartFolder = [handles.openStartFolder, handles.fileSeperator];


% Update handles structure
guidata(hObject, handles);

% UIWAIT makes EDF_View wait for user response (see UIRESUME)
% uiwait(handles.figure1);
global EdfFilePath;
global EdfFileName;
global XmlFilePath;
global XmlFileName;

global needOpenDialog;
if (~needOpenDialog)
    % Step#1: Visualize Edf
    if (~(strcmp(EdfFilePath,'') || strcmp(EdfFileName, '')))
        MenuOpenEDF_Callback(hObject, eventdata, handles);
        % Step#2: Visualize Xml
        if (~(strcmp(XmlFilePath,'') || strcmp(XmlFileName, '')))
            MenuOpenXML_Callback(hObject, eventdata, handles);
        end
    end
end
needOpenDialog = logical(1);

 
%------------------------------------------------------- EDF_View_OutputFcn
% --- Outputs from this function are returned to the command line.
function varargout = EDF_View_OutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;
% ---------------------------------------------------- MenuOpenEDF_Callback
function MenuOpenEDF_Callback(hObject, eventdata, handles)
% hObject    handle to MenuOpen (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

handles = guidata(hObject);

global needOpenDialog;
global EdfFilePath;
global EdfFileName;

% Openfile start path
if (needOpenDialog)
    [EdfFileName EdfFilePath] = uigetfile(strcat(handles.openStartFolder, '*.edf'), 'Open EDF File');
end

if ~(length(EdfFilePath)==1)
    % Store file name
    handles.FileName = [EdfFilePath EdfFileName];
    handles.openStartFolder = EdfFilePath;
    
    % Set menu flags on (dad, 2012/07/27)
    set(handles.MenuOpenXML,'enable','on');
    set(handles.MenuChSelection,'enable','on');
    set(handles.MenuFilter,'enable','on');
    
    set(handles.figure1,'pointer', 'watch');

    Temp = EdfInfo(handles.FileName);   
    
    handles.FileInfo   = Temp.FileInfo;
    handles.ChInfo     = Temp.ChInfo;
    handles.FlagChInfo = 1;
    
    guidata(hObject, handles);
    
    Temp = [[1:length(handles.ChInfo.nr)]' zeros(length(handles.ChInfo.nr),1)];
   
    handles.SelectedCh = Temp;
    handles.FlagSelectedCh = 1;
    
    FilterPara = [];
    for i=1:length(handles.ChInfo.nr)
        FilterPara{i}.A              = 1;
        FilterPara{i}.B              = 1;
        FilterPara{i}.HighValue      = 1;
        FilterPara{i}.LowValue       = 1;
        FilterPara{i}.NotchValue     = 1;
        FilterPara{i}.ScalingFactor  = 1;
        Index=findstr(handles.ChInfo.Labels(i,:),'ECG');
        Index = [Index findstr(handles.ChInfo.Labels(i,:),'SaO2')];
        Index = [Index findstr(handles.ChInfo.Labels(i,:),'PLTH')];
        if ~isempty(Index)
            FilterPara{i}.Color      = 'r';
        else
            Index=findstr(handles.ChInfo.Labels(i,:),'Leg');
            if ~isempty(Index)
                FilterPara{i}.Color      = 'g';
            else
                
                FilterPara{i}.Color      = 'k';
            end
        end
    end
    
    handles.FilterPara = FilterPara;
    
    Temp=[];
    
    TempText = handles.FileInfo.LocalPatientID;
    TempText(TempText==32)=[];
    Temp{1}=['Patient Name : ' TempText];
    
    TempText = handles.FileInfo.LocalRecordID;
    TempText(TempText==32)=[];
    Temp{2}=['Patient ID   : ' TempText];
    
    Temp{3}=['Start Date   : ' handles.FileInfo.StartDate];
    Temp1=handles.FileInfo.StartTime;
    Temp1([3 6])='::';
    Temp{4}=['Start Time   : ' Temp1];
    
    
    Counter = 5;
    
    for i=1:length(handles.ChInfo.nr)
        Counter = Counter + 1;
        Temp1 = handles.ChInfo.Labels(i,:);
        if ~isempty(Temp1)
            while Temp1(end)==32 & length(Temp1)>1
                Temp1(end)=[];
            end
        end
        
        Temp2 = handles.ChInfo.PhyDim(i,:);
        if ~isempty(Temp2)
            while Temp2(end)==32 & length(Temp2)>1
                Temp2(end)=[];
            end
        end
        
        SamplingRate = fix(handles.ChInfo.nr(i)/handles.FileInfo.DataRecordDuration);
        
        Temp{Counter} = [Temp1 ' : ' num2str(handles.ChInfo.PhyMin(i)) ' to ' ...
            num2str(handles.ChInfo.PhyMax(i)) ' ' Temp2 ' (' num2str(handles.ChInfo.DiMin(i)) ' to ' ...
            num2str(handles.ChInfo.DiMax(i)) '), SR : ' num2str(SamplingRate)];
        
    end
    
    
    % Next couple of lines buggy for sleep heart health 
    edfHeaderString =  CreateListBoxEdfHeaderString ...
        (hObject, eventdata, handles);
    set(handles.ListBoxPatientInfo,'string',edfHeaderString);
    
    Temp = dir(handles.FileName);
    handles.TotalTime = (Temp.bytes - handles.FileInfo.HeaderNumBytes) ...
        / 2  / sum(handles.ChInfo.nr) * handles.FileInfo.DataRecordDuration ;
    
    
    Temp = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(Temp);
    
    Temp = handles.TotalTime-WindowTime;
    set(handles.SliderTime,'max',Temp,'SliderStep',[0.2 1]*WindowTime/Temp,'value',0)
    
    % Update Flags and Variables before making external calls
    handles.EDF_LOADED = 1;
    handles.FlagAnn = 0 ;
    handles.XML_LOADED = 0;
    
    % Update figure title
    figureTitle = sprintf('%s: %s',handles.figureDefaultTitle, EdfFileName);
    set(handles.figure1, 'Name', figureTitle);
    
    % Clear Hynogram and Annotations
    cla(handles.axes2);
    set(handles.ListBoxComments, 'String', sprintf(' \n'));
    set(handles.ListBoxComments, 'Value', 1);
    
    % Update data and plot
    handles=DataLoad(handles);
    guidata(hObject, handles);
    handles = UpDatePlot(hObject, handles);
    guidata(hObject, handles);
    
    % Let user know load has been completed
    set(handles.figure1,'pointer', 'arrow');
    guidata(hObject, handles);
end



%--------------------------------------------- CreateListBoxEdfHeaderString
function EdfHeaderString = CreateListBoxEdfHeaderString ...
    (hObject, eventdata, handles)

    % Create debug string
    DEBUG = 0;
    
    Temp=[];
    
    TempText = handles.FileInfo.LocalPatientID;
    TempText(TempText==32)=[];
    Temp{1}=['Patient Name : ' TempText];
    
    TempText = handles.FileInfo.LocalRecordID;
    TempText(TempText==32)=[];
    Temp{2}=['Patient ID   : ' TempText];
    
    Temp{3}=['Start Date   : ' handles.FileInfo.StartDate];
    Temp1=handles.FileInfo.StartTime;
    Temp1([3 6])='::';
    Temp{4}=['Start Time   : ' Temp1];
    
    
    Counter = 5;
    
    for i=1:length(handles.ChInfo.nr)
        if DEBUG == 1
           fprintf('--- CreateListBoxEdfHeaderString Loop (%.0f)',i);  
        end
        
        Counter = Counter + 1;
        Temp1 = handles.ChInfo.Labels(i,:);
        if ~isempty(Temp1)
            while Temp1(end)==32 & length(Temp1)>1
                Temp1(end)=[];
            end
        end
        
        Temp2 = handles.ChInfo.PhyDim(i,:);
        if ~isempty(Temp2)
            while Temp2(end)==32 & length(Temp2)>1
                Temp2(end)=[];
            end
        end
        
        SamplingRate = fix(handles.ChInfo.nr(i)/handles.FileInfo.DataRecordDuration);
        
        % Create signal substring
        signalStr = '          ';
        signalStr(1:length(Temp1)+1) = [Temp1 ':'];
        
        % Create physical dimension string
        phyMinValueStr = num2str(handles.ChInfo.PhyMin(i));
        phyMinStr = '    ';
        %phyMinValueStr
        %%%  Double check this value 
        % phyMinStr(end-length(phyMinValueStr)+1:end) = phyMinValueStr;
        phyMinStr = phyMinValueStr;
        % handles.ChInfo.PhyMax(i)
        phyMaxValueStr = num2str(handles.ChInfo.PhyMax(i));
        phyMaxStr = '    ';
        phyMaxStr(1:length(phyMaxValueStr)) = phyMaxValueStr;
        phyStr = sprintf('%s to %s',phyMinStr, phyMaxStr);
        
        % Create physical dimension string
        digMinValueStr = num2str(handles.ChInfo.DiMin(i));
        digMinStr = '      ';
        digMinStr(end-length(phyMinValueStr)+1:end) = phyMinValueStr;
        digMaxValueStr = num2str(handles.ChInfo.DiMax(i));
        digMaxStr = '      ';
        digMaxStr(1:length(digMaxValueStr)) = digMaxValueStr;
        digStr = sprintf('%s to %s',digMinStr, digMaxStr);
        
        % Sampling rate string
        samRateStr = num2str(SamplingRate);
        
        lineStr = sprintf('%s %s (%s), SR:  %s', ...
            signalStr, phyStr, digStr, samRateStr);
        
        Temp{Counter} = lineStr;
        
    end
% Create header string   
EdfHeaderString = Temp;

%----------------------------------------------- PopMenuWindowTime_Callback
% --- Executes on selection change in PopMenuWindowTime.
function PopMenuWindowTime_Callback(hObject, eventdata, handles)
% hObject    handle to PopMenuWindowTime (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = cellstr(get(hObject,'String')) returns PopMenuWindowTime contents as cell array
%        contents{get(hObject,'Value')} returns selected item from PopMenuWindowTime


% Get menu entry
popup_id = get(handles.PopMenuWindowTime,'value');
epoch_length = handles.epoch_menu_values(popup_id);

Temp = handles.TotalTime-epoch_length;
if  Temp < get(handles.SliderTime,'value')
    set(handles.SliderTime,'max',Temp,'SliderStep',[0.2 1]*epoch_length/Temp,'value',Temp)
else
    set(handles.SliderTime,'max',Temp,'SliderStep',[0.2 1]*epoch_length/Temp)
end

handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject,handles);
guidata(hObject, handles);
%------------------------------------------------------ SliderTime_Callback
% --- Executes on slider movement.
function SliderTime_Callback(hObject, eventdata, handles)
% hObject    handle to SliderTime (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% Get slider value
sliderValue = round(get(hObject,'value'));
set(hObject,'value',sliderValue);

% Add annotation if they exist
if handles.FlagAnn
    % find the closest comments
    Temp=[];
    for i=1:length(handles.ScoredEvent)
        Temp(i)=handles.ScoredEvent(i).Start;
    end
    Temp = Temp - get(handles.SliderTime,'value');
    [Temp Index]=min(abs(Temp));
    set(handles.ListBoxComments,'value',Index);
end

% Get, update, and display data
handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject,handles);
guidata(hObject, handles);



%----------------------------------------------------------------- DataLoad
function handles = DataLoad(handles)
% DataLoad rewritten to access data loaded with block load.

% Access epoch
epoch_id = get(handles.PopMenuWindowTime,'value');
WindowTime = handles.epoch_menu_values(epoch_id);
Time = get(handles.SliderTime,'value');

FileName= handles.FileName;

fid=fopen(FileName,'r');

SkipByte=handles.FileInfo.HeaderNumBytes+fix(Time/handles.FileInfo.DataRecordDuration) ...
    *sum(handles.ChInfo.nr)*2;
fseek(fid,SkipByte,-1);

Data=[];
for i=1:handles.FileInfo.SignalNumbers
    Data{i}=[];
end

% Sec/handles.DatarecordDuration is the number of
for i=1 : WindowTime/handles.FileInfo.DataRecordDuration
    for j=1:handles.FileInfo.SignalNumbers
        Data{j}= [Data{j} fread(fid,[1 handles.ChInfo.nr(j)],'int16') ];
    end
end
fclose('all');


handles.Data = Data;
handles=DataNormalize(handles);
Data = handles.Data;

handles.Data=[];

SelectedCh = handles.SelectedCh;

FilterPara = handles.FilterPara;


% construct the selected referential and differential channels
for i=1:size(SelectedCh,1)
    if SelectedCh(i,2)==0
        % referential
        handles.Data{i}=Data{SelectedCh(i,1)};
    else
        % differential
        handles.Data{i}=Data{SelectedCh(i,1)}-Data{SelectedCh(i,2)};
    end
    
    % Filtering
    handles.Data{i} = filter(FilterPara{i}.B,FilterPara{i}.A,handles.Data{i});
    
end



function handles = DataNormalize(handles)

for i=1:length(handles.Data)
    % remove the mean
    handles.Data{i}=handles.Data{i}-(handles.ChInfo.DiMax(i)+handles.ChInfo.DiMin(i))/2;
    handles.Data{i}=handles.Data{i}./(handles.ChInfo.DiMax(i)-handles.ChInfo.DiMin(i));
    if handles.ChInfo.PhyMin(i)>0
        handles.Data{i}=-handles.Data{i};
    end
end





%--------------------------------------------------------------- UpDatePlot
function handles = UpDatePlot(hObject, handles)
% set the epoch number
% each epoch has been considered as 30 sec

% get  current epoch length


% Set epcoh values based on slider time
SliderTime = get(handles.SliderTime,'value');
epochTime30Sec = fix(SliderTime/30)+1;
epochPopupValue = get(handles.PopMenuWindowTime,'value');
epoch_length = handles.epoch_menu_values(epochPopupValue);
epoch = fix(SliderTime/epoch_length)+1; 
SliderMax = get(handles.SliderTime,'Max');
epochMax = fix(SliderMax/epoch_length)+1; 
epochMax30Sec  = fix(SliderMax/30)+1; 

set(handles.EditEpochNumber,'string',num2str(epoch));
totalString = sprintf('of %.0f (%.0f)',epochMax, epochMax30Sec);
set(handles.textTotalEpochs,'String',totalString);
epochsString = sprintf('%.0f (%.0f)',epoch,epochTime30Sec);
set(handles.textCurrent30secEpochs,'String',epochsString);

% Plot the data
axes(handles.axes1);
% c_axes = evalin('base','c_axes');
c_axes = handles.c_axes;

% not sure why this is need, throws an exception when clicking on the 
% histogram
% if ~isempty(c_axes)
%     c_axes
%     delete(c_axes)
% end
cla
hold on

% get the window width
PopUpMenuIndex = get(handles.PopMenuWindowTime,'value');
WindowTime = handles.epoch_menu_values(PopUpMenuIndex);

SelectedCh = handles.SelectedCh;


% Identify channels to plot
SelectedChMap=[];
for i=1:size(SelectedCh,1)
    if SelectedCh(i,2)==0
        SelectedChMap{i,1} = handles.ChInfo.Labels(SelectedCh(i,1),:);
    else
        SelectedChMap{i,1} = [handles.ChInfo.Labels(SelectedCh(i,1),:) '-' handles.ChInfo.Labels(SelectedCh(i,2),:)];
    end
    SelectedChMap{i,1}((SelectedChMap{i,1}==' '))=[];
end

% Process Annotations, if present
if handles.FlagAnn
    % plot sleep stage line
    epoch_width = get(handles.PopMenuWindowTime,'Value');
    epoch_width = handles.epoch_menu_values(epoch_width);
    epoch_width = max(epoch_width, handles.minimum_cursor_width);
    set(handles.LineSleepStage,...=
        'xData',[-1 -1 1 1 -1]*epoch_width/2+get(handles.SliderTime,'value')+epoch_width/2);
    set(handles.LineSleepStage,'FaceAlpha',0.5);
    set(handles.LineSleepStage,'EdgeColor', 'r');
    
    % Get scored event time
    Start = [];
    for i=1:length(handles.ScoredEvent)
        Start(i)=handles.ScoredEvent(i).Start;
    end
    
    CurrentTime = get(handles.SliderTime,'value');
    
    
    if ~handles.PlotType
        
        % Annotation plot
        
        % Forward Plot
        Index = find(Start>CurrentTime & ...
            Start < (CurrentTime+WindowTime));
        
        if ~isempty(Index)
            
            ChNum=3;
            Start =[];
            
            for i=1:length(Index)
                Start(i) = handles.ScoredEvent(Index(i)).Start-CurrentTime;
                Temp=Start(i) + handles.ScoredEvent(Index(i)).Duration;
                
                if Temp>WindowTime
                    Temp=WindowTime;
                end
                
                fill([Start(i)  Temp Temp Start(i)], ...
                    [-ChNum-3/2 -ChNum-3/2 -ChNum-1/2 -ChNum-1/2 ]...
                    ,[190 222 205]/255);
                
                plot([Start(i)  Temp Temp Start(i) Start(i)], ...
                    [-ChNum-3/2 -ChNum-3/2 -ChNum-1/2 -ChNum-1/2 -ChNum-3/2]...
                    ,'Color',[1 1 1]);
            end
        end
        
        % Annotation text
        if ~isempty(Index)
            for i=1:3:length(Index)
                text(Start(i),-ChNum-0.65,handles.ScoredEvent(Index(i)).EventConcept,'FontWeight','bold','FontSize',9)
            end
        end
    
        
        % Reverse plot added --- Jan 22, 2013
        Temp = [];
        Start = [];
        for i=1:length(handles.ScoredEvent)
            Temp(i)=handles.ScoredEvent(i).Start+handles.ScoredEvent(i).Duration;
            Start(i)=handles.ScoredEvent(i).Start;
        end
        IndexReverse = find((Temp)>=CurrentTime & Temp <= (CurrentTime+WindowTime));
        IndexReverse = [IndexReverse find(Start<=CurrentTime & Temp >= (CurrentTime+WindowTime) )];
        
        
        for i=1:length(Index)
            IndexReverse(IndexReverse==Index(i))=[];
        end
        
        
        Start = Start(IndexReverse)-CurrentTime;
        
        if ~isempty(IndexReverse)
            
            ChNum=[];
            for j=IndexReverse
                ChNum = [ChNum 6];
            end
            
            for i=1:length(IndexReverse)
                Temp=Start(i)+handles.ScoredEvent(IndexReverse(i)).Duration;
                
                fill([0  Temp Temp 0], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 ]+2 ...
                    ,[190 222 205]/255);
                
                plot([0  Temp Temp 0 0], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 -ChNum(i)-3/2]+2 ...
                    ,'Color',[1 1 1]);
                text(0,-ChNum(i)-0.65+2,handles.ScoredEvent(IndexReverse(i)).EventConcept,'FontWeight','bold','FontSize',9)
            end
        end
        
        

        
    else
        
        
        % Forward Plot
        Index = find(Start>CurrentTime & ...
            Start < (CurrentTime+WindowTime));
        
        if ~isempty(Index)
            Start=Start(Index)-CurrentTime;
            ChNum=[];
            for j=Index
                for i=1:size(handles.ChInfo.Labels,1)
                    if  strncmp(handles.ChInfo.Labels(i,:),[handles.ScoredEvent(j).InputCh ' '],...
                            length(handles.ScoredEvent(j).InputCh+1))
                        ChNum=[ChNum i];
                    end
                end
            end
            
            for i=1:length(Index)
                Temp=Start(i)+handles.ScoredEvent(Index(i)).Duration;
                
                if Temp>WindowTime
                    Temp=WindowTime;
                end
                
                fill([Start(i)  Temp Temp Start(i)], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 ]+2 ...
                    ,[190 222 205]/255);
                
                plot([Start(i)  Temp Temp Start(i) Start(i)], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 -ChNum(i)-3/2]+2 ...
                    ,'Color',[1 1 1]);
                text(Start(i),-ChNum(i)-0.65+2,handles.ScoredEvent(Index(i)).EventConcept,'FontWeight','bold','FontSize',9)
            end
            
            
        end
        
        
        % Reverse Plot
        Temp = [];
        Start = [];
        for i=1:length(handles.ScoredEvent)
            Temp(i)=handles.ScoredEvent(i).Start+handles.ScoredEvent(i).Duration;
            Start(i)=handles.ScoredEvent(i).Start;
        end
        IndexReverse = find((Temp)>=CurrentTime & Temp <= (CurrentTime+WindowTime));
        IndexReverse = [IndexReverse find(Start<=CurrentTime & Temp >= (CurrentTime+WindowTime) )];
        
        
        for i=1:length(Index)
            IndexReverse(IndexReverse==Index(i))=[];
        end
        
        Start = Start(IndexReverse)-CurrentTime;
        if ~isempty(IndexReverse)
            
            ChNum=[];
            for j=IndexReverse
                for i=1:size(handles.ChInfo.Labels,1)
                    if  strncmp(handles.ChInfo.Labels(i,:),[handles.ScoredEvent(j).InputCh ' '],...
                            length(handles.ScoredEvent(j).InputCh+1))
                        ChNum=[ChNum i];
                    end
                end
            end
            
            for i=1:length(IndexReverse)
                Temp=Start(i)+handles.ScoredEvent(IndexReverse(i)).Duration;
                
                fill([0  Temp Temp 0], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 ]+2 ...
                    ,[190 222 205]/255);
                
                plot([0  Temp Temp 0 0], ...
                    [-ChNum(i)-3/2 -ChNum(i)-3/2 -ChNum(i)-1/2 -ChNum(i)-1/2 -ChNum(i)-3/2]+2 ...
                    ,'Color',[1 1 1]);
                text(0,-ChNum(i)-0.65+2,handles.ScoredEvent(IndexReverse(i)).EventConcept,'FontWeight','bold','FontSize',9)
            end  
        end
    end
    
% Set epoch values based on slider time
SliderTime = get(handles.SliderTime,'value');
epochTime30Sec = fix(SliderTime/30)+1;
epochPopupValue = get(handles.PopMenuWindowTime,'value');
epoch_length = handles.epoch_menu_values(epochPopupValue);
epoch = fix(SliderTime/epoch_length)+1; 
SliderMax = get(handles.SliderTime,'Max');
epochMax = fix(SliderMax/epoch_length)+1; 
epochMax30Sec  = fix(SliderMax/30)+1; 

set(handles.EditEpochNumber,'string',num2str(epoch));
totalString = sprintf('of %.0f (%.0f)',epochMax, epochMax30Sec);
set(handles.textTotalEpochs,'String',totalString);
epochsString = sprintf('%.0f (%.0f)',epoch,epochTime30Sec);
set(handles.textCurrent30secEpochs,'String',epochsString);
end



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Signals Plot
% FilterPara = evalin('base','FilterPara');
FilterPara = handles.FilterPara;

Counter = 0;
scaled_data_range = [];
auto_scale_factor = handles.auto_scale_factor;
for i=1:size(SelectedCh,1)
    % Get data, recenter and plot
    Time = [0:size(handles.Data{i},2)-1]/size(handles.Data{i},2)*WindowTime;
    PlotColor = FilterPara{i}.Color;
    Y = (handles.Data{i}*FilterPara{i}.ScalingFactor-Counter - ...
        mean(handles.Data{i}*FilterPara{i}.ScalingFactor-Counter))+(1-i);
    plot(Time,Y,'LineWidth',0.01,'color',PlotColor);
    
    % Not sure is this is still being used, 1/27/2013, dad
    Counter = Counter + 1 ;
        
    % Store Current Scaling Factor
    auto_scale_factor (i) = FilterPara{i}.ScalingFactor;
    scaled_data_range = [scaled_data_range;...
        [min(handles.Data{i}*FilterPara{i}.ScalingFactor),...
         max(handles.Data{i}*FilterPara{i}.ScalingFactor)]];    
end

% Determine Optimal Range
data_range = scaled_data_range(:,2) - scaled_data_range(:,1);
index = find(data_range ~= 0);
handles.auto_scale_factor(index) = handles.auto_scale_height./data_range(index);
guidata(hObject, handles);

% Stage information
if and(handles.FlagAnn, epoch_length == 30)
    % plot sleep states
    Temp=handles.SleepStages([1:WindowTime]+get(handles.SliderTime,'value'));
    Temp = Temp - min(Temp);
    if max(Temp)>0
        Temp = Temp / max(Temp) - 0.25;
    end
    plot([0:length(Temp)-1],Temp+1,'linewidth',1.5,'color','k')
    
    % comment for sleep stage
    if sum(abs(diff(Temp))>0)
        % there is more than one sleep state
        Index = [1 find(diff(Temp))+1];
        
        for i=1:length(Index)
            TempState = (handles.SleepStages(get(handles.SliderTime,'value')+Index(i)));
            switch TempState
                case 5
                    TempState = 'W';
                case 4
                    TempState = 'N1';
                case 3
                    TempState = 'N2';
                case 2
                    TempState = 'N3';
                case 1
                    TempState = 'N4';
                case 0
                    TempState = 'N5';
            end
            
            if Temp(Index(i))>0
                text(Index(i),Temp(Index(i))+0.75,['State: ' TempState],'fontweight','bold');
            else
                text(Index(i),Temp(Index(i))+1.25,['State: ' TempState],'fontweight','bold');
            end
        end
        
    else
        TempState = handles.SleepStages(get(handles.SliderTime,'value')+1);
        switch TempState
            case 5
                TempState = 'W';
            case 4
                TempState = 'N1';
            case 3
                TempState = 'N2';
            case 2
                TempState = 'N3';
            case 1
                TempState = 'N4';
            case 0
                TempState = 'N5';
        end
        
        text(WindowTime/2,1.5,['State ' TempState],'fontweight','bold')
    end
    
end

% Set the yTick
YTick=[(-length(handles.Data)+1):0];
set(handles.axes1,'YTick',YTick);

% Set the ylim

ylim([-length(handles.Data) 2]);
set(handles.axes1,'YTickLabel',SelectedChMap([length(SelectedChMap):-1:1]))


% Set the xTick based on the window size
epoch_menu_value = get(handles.PopMenuWindowTime,'value');
epoch_length = handles.epoch_menu_values(epoch_menu_value);
epoch_menu_num_tick_sections = ...
    handles.epoch_menu_num_tick_sections(epoch_menu_value);
increment = 1/epoch_menu_num_tick_sections;

XTick=[0:increment:1]*epoch_length;
Temp = XTick + get(handles.SliderTime,'value');
Temp = datestr(Temp/86400,'HH:MM:SS');
set(handles.axes1,'XTick',XTick,'xTickLabel',Temp,'xlim',[0 epoch_length]);

hold off
grid on

% change the color of xtick and ytick
xtick = get(handles.axes1,'XTick');
ytick = get(handles.axes1,'YTick');
xlim = get(handles.axes1,'XLim');
ylim1 = get(handles.axes1,'YLim');

% Copy the existing axis along with children
set(handles.axes1,'TickLength',[1e-100 1])
c_axes = copyobj(handles.axes1,handles.figure1);
% assignin('base','c_axes',c_axes);
handles.c_axes = c_axes;

% Remove copy of objects
delete(get(c_axes,'Children'))

% Set color XColor to red and only show the grid
set(c_axes, 'Color', 'none', 'XColor', [192 192 1]/255, 'XGrid', 'on', ...
    'YColor',[192 192 1]/255, 'YGrid','on','XTickLabel',[], ...
    'YTickLabel',[],'XTick',xtick,'YTick',ytick,'XLim',xlim,'YLim',ylim1);



% Set epoch numbers

% Update handles
% (list handle updates here)
guidata(hObject, handles);

% ------------------------------------------------ MenuChSelection_Callback
function MenuChSelection_Callback(hObject, eventdata, handles)
% hObject    handle to MenuChSelection (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Create Channel Selection Structure
channelSelectionStruct.ChSelectionH = handles.ChSelectionH;
channelSelectionStruct.SelectedCh = handles.SelectedCh;
channelSelectionStruct.ChInfo = handles.ChInfo;
channelSelectionStruct.FilterPara = handles.FilterPara;

channelSelectionStruct.FlagSelectedCh = handles.FlagSelectedCh;
channelSelectionStruct.FlagChInfo = handles.FlagChInfo;

% Open Channel Selection Dialog Box
channelSelectionStruct = ChSelection(channelSelectionStruct);


% Update local handle 
handles.ChSelectionH = channelSelectionStruct.ChSelectionH;
handles.SelectedCh = channelSelectionStruct.SelectedCh;
handles.ChInfo = channelSelectionStruct.ChInfo;
handles.FilterPara = channelSelectionStruct.FilterPara;
% 
handles.FlagSelectedCh = channelSelectionStruct.FlagSelectedCh;
handles.FlagChInfo = channelSelectionStruct.FlagChInfo;
% 
handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject, handles);

% ----------------------------------------------------- MenuFilter_Callback
function MenuFilter_Callback(hObject, eventdata, handles)
% hObject    handle to MenuFilter (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% Create structure containing filter settings
filterSettingsStruct.FilterSettingH = handles.figure1;
filterSettingsStruct.SelectedCh = handles.SelectedCh;
filterSettingsStruct.ChInfo = handles.ChInfo;
filterSettingsStruct.FileInfo = handles.FileInfo;
filterSettingsStruct.FilterPara = handles.FilterPara;
filterSettingsStruct.Sel = handles.Sel;

% Open Filter Setting GUI
filterSettingsStruct = FilterSettings(filterSettingsStruct);

% Get filter setting information
% Create structure containing filter settings
handles.FilterSettingH = filterSettingsStruct.FilterSettingH;
handles.SelectedCh = filterSettingsStruct.SelectedCh;
handles.ChInfo = filterSettingsStruct.ChInfo;
handles.FileInfo = filterSettingsStruct.FileInfo;
handles.FilterPara = filterSettingsStruct.FilterPara;
handles.Sel = filterSettingsStruct.Sel;

% Load and plot data
handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject, handles);
% assignin('base','FilterSettingH',[]);

% turn this off; since UpDatePlot updates handles, may have to create a
% return structure
% handles.FilterSettingH = handles.FilterSettingH;
guidata(hObject, handles);

%------------------------------------------------- ListBoxComments_Callback
% --- Executes on selection change in ListBoxComments.
function ListBoxComments_Callback(hObject, eventdata, handles)
% hObject    handle to ListBoxComments (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = cellstr(get(hObject,'String')) returns ListBoxComments contents as cell array
%        contents{get(hObject,'Value')} returns selected item from ListBoxComments

Sel = get(hObject,'value');

Temp = get(handles.PopMenuWindowTime,'value');
Temp1 = get(handles.PopMenuWindowTime,'string');
Temp = Temp1{Temp};
WindowTime = str2num(Temp(1:end-3));

Temp = WindowTime - handles.ScoredEvent(Sel).Duration;

if Temp>0
    Time = handles.ScoredEvent(Sel).Start-Temp/2;
else
    Time = handles.ScoredEvent(Sel).Start;
end
set(handles.SliderTime,'value',fix(Time));

handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject,handles);

guidata(hObject, handles);

%-------------------------------------------------- figure1_CloseRequestFcn
% --- Executes when user attempts to close figure1.
function figure1_CloseRequestFcn(hObject, eventdata, handles)
% hObject    handle to figure1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: delete(hObject) closes the figure


% Original program used the workspace to pass variable information. Which 
% can cause the program to hang.  Added Try- catch to by pass hanging
% code.  A good fix is to save infomration in handles.

try
%     Temp=evalin('base','ChSelectionH');
%     if ~isempty(Temp)
%         delete(Temp);
%     end
%     
%     Temp=evalin('base','FilterSettingH');
%     if ~isempty(Temp)
%         delete(Temp);
%     end
    
    close('ChSelection.fig');
    
    % Note: Not sure what these values are
    % Saved to handles just in case
    ChSelectionH = handles.ChSelectionH;
    if ~isempty(ChSelectionH)
        delete(ChSelectionH);
    end

    FilterSettingH = handles.FilterSettingH;
    if ~isempty(FilterSettingH)
        delete(FilterSettingH);
    end    
    
catch err
    % just close
    
end

try
%     Temp=evalin('base','ChSelectionH');
%     if ~isempty(Temp)
%         delete(Temp);
%     end
%     
%     Temp=evalin('base','FilterSettingH');
%     if ~isempty(Temp)
%         delete(Temp);
%     end
    
    % Note: Not sure what these values are
    % Saved to handles just in case
    ChSelectionH = handles.ChSelectionH;
    if ~isempty(ChSelectionH)
        delete(ChSelectionH);
    end

    FilterSettingH = handles.FilterSettingH;
    if ~isempty(FilterSettingH)
        delete(FilterSettingH);
    end    
    
catch err
    % just close

end

try 
    delete(hObject);
catch err
    % just close
end

%------------------------------------------------- EditEpochNumber_Callback
function EditEpochNumber_Callback(hObject, eventdata, handles)
% hObject    handle to EditEpochNumber (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of EditEpochNumber as text
%        str2double(get(hObject,'String')) returns contents of EditEpochNumber as a double



Temp = get(handles.PopMenuWindowTime,'value');
Temp1 = get(handles.PopMenuWindowTime,'string');
Temp = Temp1{Temp};
WindowTime = str2num(Temp(1:end-3));

if WindowTime<30
    WindowTime = 30;
end

EpochNumber = str2num(get(hObject,'string'));
MaxEpoch = (handles.TotalTime - WindowTime)/30+1;

if EpochNumber>MaxEpoch
    EpochNumber = MaxEpoch;
    set(hObject,'string',num2str(EpochNumber));
end


set(handles.SliderTime,'value',(EpochNumber-1)*30);

if handles.FlagAnn
    % find the closest comments
    Temp=[];
    for i=1:length(handles.ScoredEvent)
        Temp(i)=handles.ScoredEvent(i).Start;
    end
    Temp = Temp - get(handles.SliderTime,'value');
    [Temp Index]=min(abs(Temp));
    set(handles.ListBoxComments,'value',Index);
end



handles=DataLoad(handles);
guidata(hObject,handles);
handles = UpDatePlot(hObject,handles);

guidata(hObject, handles);

% ---------------------------------------------------- MenuOpenXML_Callback
function MenuOpenXML_Callback(hObject, eventdata, handles)
% hObject    handle to MenuOpenXML (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

handles = guidata(hObject);

global needOpenDialog;
global XmlFilePath;
global XmlFileName;
    
Temp = handles.FileName;
Temp([-3:0]+end) = [];

if (needOpenDialog)
    [XmlFileName XmlFilePath] = uigetfile([Temp '*.xml'], 'Open XML File');
end

handles.FlagAnn=1;
% if there is ann file
if ~(sum(XmlFileName==0))
    % check for the version of xml
    Fid = fopen([XmlFilePath XmlFileName],'r');
    Temp = fread(Fid,[1 inf],'uint8');
    fclose(Fid);
    Temp = strfind(Temp,'Compumedics');
    
    % Determine file type
    if isempty(Temp)
        % it is compumedics ann file
        handles.FlagAnnType = 1;
        [handles.ScoredEvent, handles.SleepStages, handles.EpochLength]=...
            readXML_Com([XmlFilePath XmlFileName]);
        handles.PlotType = 1;
        Temp = [];
        
        for i=1:length(handles.SleepStages)
            Temp = [Temp ones(1,30)*handles.SleepStages(i)];
        end
        handles.SleepStages = Temp;
        
    else
        % it is PhysiMIMI file
        handles.FlagAnnType = 0;
        [handles.ScoredEvent, handles.SleepStages, handles.EpochLength,...
            annotation] = readXML([XmlFilePath XmlFileName]);
        handles.PlotType = 0;
    end
    
    % Create list box contents
    % ListBox Comments annotation
    Temp = [];
    for i=1:length(handles.ScoredEvent)
        Temp1 = fix(handles.ScoredEvent(i).Start/30)+1;
        Temp{i}= [num2str(Temp1) ' - ' datestr(handles.ScoredEvent(i).Start/86400,'HH:MM:SS - ') handles.ScoredEvent(i).EventConcept];
    end
    set(handles.ListBoxComments,'string',Temp);
    
    
    % Plot historgram
    axes(handles.axes2)
    cla
    hold off    
    plot(handles.SleepStages,'LineWidth',1.5,'color','k');
    hold on
    set(handles.axes2,'xTick',[0 length(handles.SleepStages)],'xlim',[0 length(handles.SleepStages)],'xticklabel',''...
        ,'fontweight','bold','yTick',[0:5],'ylim',[-0.5 5.5],'color',[205 224 247]/255,'yTickLabel',{'R','','N3','','N1','W'})
    hold on
    
    % Higlight current window in historgram
    epoch_width = get(handles.PopMenuWindowTime,'Value');
    epoch_width = handles.epoch_menu_values(epoch_width);
    epoch_width= max(epoch_width,handles.minimum_cursor_width);
    x = [-1 -1 1 1 -1]*epoch_width/2+2+epoch_width/2;
    y = [0 5 5 0 0];
    
    handles.LineSleepStage =  fill(x,y,'r','EdgeColor', 'r','FaceAlpha',0.5);
    hold off
    
    % Record xml load
    handles.XML_LOADED = 1;
    guidata(hObject, handles);
    
    handles=DataLoad(handles);
    guidata(hObject,handles);
    handles = UpDatePlot(hObject, handles);
    guidata(hObject, handles);
else
    if handles.XML_LOADED == 0
%         axes(handles.axes2)
%         cla
%         hold off
%         set(handles.ListBoxComments,'string','');
%         handles.FlagAnn=0;
    end
end
guidata(hObject, handles);



%------------------------------------------------ figure1_WindowKeyPressFcn
% --- Executes on key press with focus on figure1 or any of its controls.
function figure1_WindowKeyPressFcn(hObject, eventdata, handles)
% hObject    handle to figure1 (see GCBO)
% eventdata  structure with the following fields (see FIGURE)
%	Key: name of the key that was pressed, in lower case
%	Character: character interpretation of the key(s) that was pressed
%	Modifier: name(s) of the modifier key(s) (i.e., control, shift) pressed
% handles    structure with handles and user data (see GUIDATA)


% Select the signal to present
Loc=get(handles.axes1,'CurrentPoint');
Sel = round(Loc(1,2));
if Sel>0
    Sel = 0;
end
Sel = abs(Sel)+1;

% Get current character
currentCharacter = get(hObject,'CurrentCharacter')+0;

% Process current character
if currentCharacter == handles.UP_ARROW_KEY
    % Process up arrow key, increase signal scaling factor
    RESCALE_ALL_SIGNALS = 0;
    
    % check if ctrl key is set
    if ~isempty(eventdata.Modifier)
       % Modifier key is set, check if control key is set
       if strcmp(eventdata.Modifier{1},'control') == 1
           RESCALE_ALL_SIGNALS = 1;
       end    
    end
  
    % Get filter parameters
    FilterPara = handles.FilterPara;
    
    % Reset parametrs based on key combinations
    if RESCALE_ALL_SIGNALS == 0
        FilterPara{Sel}.ScalingFactor = ...
            fix(FilterPara{Sel}.ScalingFactor * 115)/100;
    else
        
        for s = 1:length(FilterPara)
            FilterPara{s}.ScalingFactor = ...
                fix(FilterPara{s}.ScalingFactor * 115)/100;
        end
    end
    
    % Save new filter paramters and update plot
    handles.FilterPara = FilterPara;
    guidata(hObject,handles);
    handles = UpDatePlot(hObject,handles);
    guidata(hObject,handles);
elseif currentCharacter == handles.DOWN_ARROW_KEY
    % Process down arrow key, decrease signal scale
    
    % Set signals to rescale
    RESCALE_ALL_SIGNALS = 0;
    
    % check if ctrl key is set
    if ~isempty(eventdata.Modifier)
       % Modifier key is set, check if control key is set
       if strcmp(eventdata.Modifier{1},'control') == 1
           RESCALE_ALL_SIGNALS = 1;
       end    
    end
    
    % FilterPara = evalin('base','FilterPara');
    FilterPara = handles.FilterPara;
    
    % Reset parametrs based on key combinations
    if RESCALE_ALL_SIGNALS == 0
        FilterPara{Sel}.ScalingFactor = ...
            fix(FilterPara{Sel}.ScalingFactor * 85)/100;
    else
        
        for s = 1:length(FilterPara)
            FilterPara{s}.ScalingFactor = ...
                fix(FilterPara{s}.ScalingFactor * 85)/100;
        end
    end    
    
    
    % Save new scaling factors and update plot
    handles.FilterPara = FilterPara;
    guidata(hObject,handles);
    handles = UpDatePlot(hObject,handles);
    guidata(hObject,handles);
elseif currentCharacter == handles.RIGHT_ARROW_KEY
    % Process right arrow
    
    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    Value = get(handles.SliderTime,'value');
    Value = fix(Value/WindowTime)*WindowTime;
    maxSliderValue = get(handles.SliderTime,'max'); 
    
    % If slife value is in a valid range
    if Value<=(maxSliderValue-WindowTime)
        
        set(handles.SliderTime,'value',fix(Value+WindowTime));
        
        if handles.FlagAnn
            % find the closest comments
            Temp=[];
            for i=1:length(handles.ScoredEvent)
                Temp(i)=handles.ScoredEvent(i).Start;
            end
            Temp = Temp - get(handles.SliderTime,'value');
            [Temp Index]=min(abs(Temp));
            set(handles.ListBoxComments,'value',Index);
        end
        
        handles=DataLoad(handles);
        guidata(hObject,handles);
        handles = UpDatePlot(hObject,handles);
        guidata(hObject,handles);
    end
elseif currentCharacter == handles.LEFT_ARROW_KEY
    % Process left arrow key

    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    Value = get(handles.SliderTime,'value');
    Value = fix(Value/WindowTime)*WindowTime;
    maxSliderValue = get(handles.SliderTime,'max'); 
    
    % Process key if new time in a ballid range
    if and(Value>=WindowTime, Value >=0)
        set(handles.SliderTime,'value',fix(Value-WindowTime));
        
        if handles.FlagAnn
            % find the closest comments
            Temp=[];
            for i=1:length(handles.ScoredEvent)
                Temp(i)=handles.ScoredEvent(i).Start;
            end
            Temp = Temp - get(handles.SliderTime,'value');
            [Temp Index]=min(abs(Temp));
            set(handles.ListBoxComments,'value',Index);
        end
        
        handles=DataLoad(handles);
        guidata(hObject,handles);
        handles = UpDatePlot(hObject,handles);
        guidata(hObject,handles);
    end
end

% Update changes to handles
guidata(hObject, handles);


%---------------------------------------------- figure1_WindowButtonDownFcn
% --- Executes on mouse press over figure background, over a disabled or
% --- inactive control, or over an axes background.
function figure1_WindowButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to figure1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


%-----------------------------------------------------------------histogram
% Get location of cursor at Window's button down
Loc=get(handles.axes2,'CurrentPoint');

xLim = get(handles.axes2,'xlim');
yLim = get(handles.axes2,'ylim');

% Check if button down is in histogram window
% Click to go to sleep point of sleep axes
if (Loc(3)>yLim(1) & Loc(3)<yLim(2) & Loc(1)>xLim(1) & Loc(1)<xLim(2))
    
    
    % Get slider limits
    Max = get(handles.SliderTime,'max');
    Time = fix(Loc(1));
    
    % Adjust time accoring to window width
    epoch_popup_menu_value = get(handles.PopMenuWindowTime,'Value');
    epoch_width = handles.epoch_menu_values(epoch_popup_menu_value);
    Time = floor(Time/epoch_width)*epoch_width;
    if Time<0
        Time = 0;
    elseif Time> Max
        Time = Max;
    end
    set(handles.SliderTime,'value',Time);
    
    if handles.FlagAnn
        % find the closest comments
        Temp=[];
        for i=1:length(handles.ScoredEvent)
            Temp(i)=handles.ScoredEvent(i).Start;
        end
        Temp = Temp - get(handles.SliderTime,'value');
        [Temp Index]=min(abs(Temp));
        set(handles.ListBoxComments,'value',Index);
    end
    
    handles=DataLoad(handles);
    guidata(hObject,handles);
    handles = UpDatePlot(hObject,handles);
    guidata(hObject,handles);
end


%--------------------------------------------------------- Time Series Plot
% Get current position in time plots
Loc=get(handles.axes1,'CurrentPoint');
xLim = get(handles.axes1,'xlim');
yLim = get(handles.axes1,'ylim');

% check is current point is in time series plot
if (Loc(3)>yLim(1) & Loc(3)<yLim(2) & Loc(1)>xLim(1) & Loc(1)<xLim(2))
    
    
    Sel = round(Loc(1,2));
    if Sel>0
        Sel = 0;
    end
    Sel = abs(Sel)+1;
    
    handles.ActiveCh = Sel;
    
    % ChInfo = evalin('base','ChInfo');
    % SelectedCh = evalin('base','SelectedCh');
    ChInfo = handles.ChInfo;
    SelectedCh = handles.SelectedCh;
    
    
    if Sel > size(SelectedCh,1)
        Sel = size(SelectedCh,1);
    end
    
    SelectedChMap=[];
    
    for i=1:size(SelectedCh,1)
        if SelectedCh(i,2)==0
            SelectedChMap{i,1} = handles.ChInfo.Labels(SelectedCh(i,1),:);
        else
            SelectedChMap{i,1} = [handles.ChInfo.Labels(SelectedCh(i,1),:) '-' handles.ChInfo.Labels(SelectedCh(i,2),:)];
        end
        SelectedChMap{i,1}((SelectedChMap{i,1}==' '))=[];
    end
    
    set(handles.TextInfo,'string',['Active Ch : ' SelectedChMap{Sel,1}]);
    
    
end

% User selected outside view
if Loc(1)<0
    % Filter call seemed to cause an error
%     Sel = round(Loc(1,2));
% 
%     FilterSettings(Sel);
%     handles=DataLoad(handles);
%     
%     handles = UpDatePlot(hObject,handles);
%     handles.FilterSettingH = FilterSettingH;
end

guidata(hObject,handles);

%-------------------------------------------- figure1_WindowButtonMotionFcn
% --- Executes on mouse motion over figure - except title and menu.
function figure1_WindowButtonMotionFcn(hObject, eventdata, handles)
% hObject    handle to figure1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get current axis
Loc=get(handles.axes1,'CurrentPoint');

% Get window width
epochPopupIndex = get(handles.PopMenuWindowTime,'Value');
WindowTime = handles.epoch_menu_values(epochPopupIndex);


if ~isempty(handles.ActiveCh) & Loc(1)>0 & Loc(1)<WindowTime
    
    if handles.ActiveCh >length(handles.Data)
        handles.ActiveCh = 1;
    end
    
    Sel=fix(Loc(1)/WindowTime*length(handles.Data{handles.ActiveCh}));
    
    if Sel ==0 | length(handles.ActiveCh)<Sel
        Sel = 1;
    end
    
    Data = handles.Data{handles.ActiveCh}(Sel);
    
    SelectedCh = handles.SelectedCh;
    
    Ch = SelectedCh(handles.ActiveCh,1);
    
    % get back the digital value
    if handles.ChInfo.PhyMin(Ch)>0
        Data=-Data;
    end
    Data = Data*(handles.ChInfo.DiMax(Ch)-handles.ChInfo.DiMin(Ch));
    Data = Data+(handles.ChInfo.DiMax(Ch)+handles.ChInfo.DiMin(Ch))/2;
    
    % scale the data to get the actual value
    Slope  = (handles.ChInfo.PhyMax(Ch)-handles.ChInfo.PhyMin(Ch))/(handles.ChInfo.DiMax(Ch)-handles.ChInfo.DiMin(Ch));
    
    Value = (Data-handles.ChInfo.DiMin(Ch))*Slope + handles.ChInfo.PhyMin(Ch);
    
    Text = ['Signal value : ' num2str(Value,'%.2f') ' ' handles.ChInfo.PhyDim(Ch,:) ];
    
    set(handles.TextSignalValue,'string',Text);
end
%----------------------------------------------- CheckBoxSleepAxes_Callback
% --- Executes on button press in CheckBoxSleepAxes.
function CheckBoxSleepAxes_Callback(hObject, eventdata, handles)
% hObject    handle to CheckBoxSleepAxes (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of CheckBoxSleepAxes
clc

% Adjusted values taken from guide window
% A quick hack, adjustments should be extracted from components directly
extendedSignalAxisPos = ...
    [0.060203283815480846, 0.0660377358490566, ...
     0.91, 0.753369272237197];
 extendedSliderPos = ...
    [0.06411258795934324, 0.006738544474393531, ...
     0.924941360437842,   0.02425876010781671];

% Process Mizimize Signal Check Box Selection
if get(hObject,'value')
    % Maximize signal check box is checked
    
    % Make unnecessary  widgit invisible
    set(handles.axes2,'visible','off');
    set(handles.ListBoxComments,'visible','off');
    set(handles.pmAnnotations,'visible','off');
    
    % Extend top boarder of signal axis to cover hypnogram axis
    % axis([xmin xmax ymin ymax])
    TempAxes1  = get(handles.axes1,'outerposition');
    TempAxes2  = get(handles.axes2,'outerposition');
    Temp = [TempAxes1(1) -0.029 TempAxes1(3) 0.94];
    
    % Save extended signal axis position
    set(handles.axes1,'position',extendedSignalAxisPos);
    set(handles.SliderTime,'position',extendedSliderPos);
else
    % Maximize signal check box is off, show annotation widgets
    set(handles.axes2,'visible','on');
    set(handles.ListBoxComments,'visible','on');
    set(handles.pmAnnotations,'visible','on');
    
    % Resize compionents to default view
    set(handles.axes1,'outerposition',handles.Axes1OrgPos);
    set(handles.SliderTime,'position',handles.SliderOrgPos);
end

handles = UpDatePlot(hObject,handles);
guidata(hObject,handles);

% --- Executes on button press in pbRightButton.
function pbRightButton_Callback(hObject, eventdata, handles)
% hObject    handle to pbRightButton (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


if handles.EDF_LOADED == 1
    
    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    Value = get(handles.SliderTime,'value');
    Value = fix(Value/WindowTime)*WindowTime;
    maxSliderValue = get(handles.SliderTime,'max');
    
    % If slife value is in a valid range
    if Value<=(maxSliderValue-WindowTime)
        
        set(handles.SliderTime,'value',fix(Value+WindowTime));
        
        if handles.FlagAnn
            % find the closest comments
            Temp=[];
            for i=1:length(handles.ScoredEvent)
                Temp(i)=handles.ScoredEvent(i).Start;
            end
            Temp = Temp - get(handles.SliderTime,'value');
            [Temp Index]=min(abs(Temp));
            set(handles.ListBoxComments,'value',Index);
        end
        
        handles=DataLoad(handles);
        guidata(hObject,handles);
        handles = UpDatePlot(hObject,handles);
        guidata(hObject,handles);
    end
    
end

% --- Executes on button press in pbLeftEpochButton.
function pbLeftEpochButton_Callback(hObject, eventdata, handles)
% hObject    handle to pbLeftEpochButton (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


if handles.EDF_LOADED == 1
    
    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    Value = get(handles.SliderTime,'value');
    Value = fix(Value/WindowTime)*WindowTime;
    maxSliderValue = get(handles.SliderTime,'max');
    
    % Process key if new time in a ballid range
    if and(Value>=WindowTime, Value >=0)
        set(handles.SliderTime,'value',fix(Value-WindowTime));
        
        if handles.FlagAnn
            % find the closest comments
            Temp=[];
            for i=1:length(handles.ScoredEvent)
                Temp(i)=handles.ScoredEvent(i).Start;
            end
            Temp = Temp - get(handles.SliderTime,'value');
            [Temp Index]=min(abs(Temp));
            set(handles.ListBoxComments,'value',Index);
        end
        
        handles=DataLoad(handles);
        guidata(hObject,handles);
        handles = UpDatePlot(hObject,handles);
        guidata(hObject,handles);
    end
    
end

% --- Executes on button press in pb_GoToStart.
function pb_GoToStart_Callback(hObject, eventdata, handles)
% hObject    handle to pb_GoToStart (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


if handles.EDF_LOADED == 1
    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    Value = 0;
    Value = fix(Value/WindowTime)*WindowTime;
    maxSliderValue = get(handles.SliderTime,'max');
    
    % Set slider value
    set(handles.SliderTime,'value',Value);
    
    % Set annotations
    if handles.FlagAnn
        % find the closest comments
        Temp=[];
        for i=1:length(handles.ScoredEvent)
            Temp(i)=handles.ScoredEvent(i).Start;
        end
        Temp = Temp - get(handles.SliderTime,'value');
        [Temp Index]=min(abs(Temp));
        set(handles.ListBoxComments,'value',Index);
    end
    
    handles=DataLoad(handles);
    guidata(hObject,handles);
    handles = UpDatePlot(hObject,handles);
    guidata(hObject,handles);
end


% --- Executes on button press in pbGoToEnd.
function pbGoToEnd_Callback(hObject, eventdata, handles)
% hObject    handle to pbGoToEnd (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

if handles.EDF_LOADED == 1
    % Get display epoch width
    epochPopupIndex = get(handles.PopMenuWindowTime,'value');
    WindowTime = handles.epoch_menu_values(epochPopupIndex);
    
    % Get slider Information (time)
    maxSliderValue = get(handles.SliderTime,'max');
    Value = maxSliderValue;
    Value = fix(Value/WindowTime)*WindowTime;
    
    % Set slider value
    set(handles.SliderTime,'value',Value);
    
    % Set annotations
    if handles.FlagAnn
        % find the closest comments
        Temp=[];
        for i=1:length(handles.ScoredEvent)
            Temp(i)=handles.ScoredEvent(i).Start;
        end
        Temp = Temp - get(handles.SliderTime,'value');
        [Temp Index]=min(abs(Temp));
        set(handles.ListBoxComments,'value',Index);
    end
    
    handles=DataLoad(handles);
    guidata(hObject,handles);
    handles = UpDatePlot(hObject,handles);
    guidata(hObject,handles);
end


% --- Executes on button press in pbAutoScale.
function pbAutoScale_Callback(hObject, eventdata, handles)
% hObject    handle to pbAutoScale (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Use the auto scale values to set the scale

% Signals Plot

% Load Current Scale Factors
auto_scale_factor = handles.auto_scale_factor;

% FilterPara = evalin('base','FilterPara');
SelectedCh = handles.SelectedCh;
FilterPara = handles.FilterPara;
    
% If scale factors are set set fitlering parameters
if ~isempty(handles.Data)

    % Get data for current epoch
    Data = handles.Data;
    
    % Get data range and define scaling factor
    numSignals = length(Data);
    dmin = @(x)min(Data{x});  
    dmax = @(x)max(Data{x}); 
    dmean = @(x)mean(Data{x});
    DataMin = arrayfun(dmin, [1:numSignals])';
    DataMax = arrayfun(dmax, [1:numSignals])';
    DataMean = arrayfun(dmean, [1:numSignals])';
    DataRange = DataMax-DataMin;
    
    % Compute scale for each signal, check for divide by zerp
    index = find(DataRange~=0);
    scalingFactor = zeros(numSignals,1);
    if ~isempty(index)
        scalingFactor(index) = ...
           4*DataRange(index)./(DataMean(index)-DataMin(index));
    end
    
    % Set each scale parameter
    for c=1:size(SelectedCh,1)
        % Get data  and plot
        % Previous Approach

        FilterPara{c}.ScalingFactor = auto_scale_factor(c);
        
        % Epoch by Epoch Scale
        FilterPara{c}.ScalingFactor = scalingFactor(c);
    end
    
    % Update paramters and replot
    handles.FilterPara = FilterPara;
    guidata(hObject, handles);
    handles = UpDatePlot(hObject, handles);  
    guidata(hObject, handles);

% autoscale button not working 
%     % Set each scale parameter
%     for c=1:size(SelectedCh,1)
%         % Get data  and plot
%         FilterPara{c}.ScalingFactor = auto_scale_factor(c);
%     end
%     
%     % Update paramters and replot
%     handles.FilterPara = FilterPara;
%     guidata(hObject, handles);
%     handles = UpDatePlot(hObject, handles);  
%     guidata(hObject, handles);
end
handles.auto_scale_factor = auto_scale_factor;
handles.FilterPara = FilterPara;
guidata(hObject, handles);


% --- Executes on selection change in pmAnnotations.
function pmAnnotations_Callback(hObject, eventdata, handles)
% hObject    handle to pmAnnotations (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = cellstr(get(hObject,'String')) returns pmAnnotations contents as cell array
%        contents{get(hObject,'Value')} returns selected item from pmAnnotations


% --- Executes during object creation, after setting all properties.
function pmAnnotations_CreateFcn(hObject, ~, handles)
% hObject    handle to pmAnnotations (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end
