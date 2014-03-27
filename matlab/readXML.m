%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This file is part of the EDFViewer, Physio-MIMI Application tools
%
% EDFViewer is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% EDFViewer is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
%
% Copyright 2010, Case Western Reserve University
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [events, stages, epochLength,annotation] = readXML(xmlfile)
try
    xdoc = xmlread(xmlfile);
catch
    error('Failed to read XML file %s.',xmlfile);
end
[events, stages, epochLength,annotation] = parseNodes(xdoc);



function [eventsVector, stages, epochLength,annotation] = parseNodes(xmldoc)
%eventStruct = struct('EventConcept','StartTime','Duration','SpO2Baseline','SpO2Nadir');
epoch = xmldoc.getElementsByTagName('EpochLength');
epochLength = str2double(epoch.item(0).getTextContent);

events = xmldoc.getElementsByTagName('ScoredEvent');
if events.getLength>0
    annotation = 1;
    numEvents = events.getLength;
    eventsVector = [];
    stages = [];
    % slight modification by fp
    stagesConcept = {'SDO:NonRapidEyeMovementSleep-N1',...
        'SDO:NonRapidEyeMovementSleep-N2',...
        'SDO:NonRapidEyeMovementSleep-N3',...
        'SDO:NonRapidEyeMovementSleep-N4',...
        'SDO:RapidEyeMovementSleep',...
        'SDO:WakeState'};
    
    
    
    for i = 0: numEvents-1 % evnets.item(i): ScoredEvent
        if events.item(i).getElementsByTagName('EventConcept').getLength >0
            name = char(events.item(i).getElementsByTagName('EventConcept').item(0).getTextContent);
            starttime = str2num(events.item(i).getElementsByTagName('Start').item(0).getTextContent);
            duration  = str2num(events.item(i).getElementsByTagName('Duration').item(0).getTextContent);
            baseline = 0;
            nadir = 0;
            text = char('');
            if events.item(i).getElementsByTagName('Desaturation').getLength>0
                baseline = str2num(events.item(i).getElementsByTagName('Desaturation').item(0).getTextContent);
            end
            if events.item(i).getElementsByTagName('SpO2Nadir').getLength > 0
                nadir = str2num(events.item(i).getElementsByTagName('SpO2Nadir').item(0).getTextContent);
            end
            if events.item(i).getElementsByTagName('Text').getLength > 0
                text = char(events.item(i).getElementsByTagName('Text').item(0).getTextContent);
            end
            eventStruct = struct('EventConcept',name, 'Start',starttime,'Duration',duration,'Desaturation',baseline,'SpO2Nadir',nadir,'Text',text);
            
            %        if ~isempty(strfind(stagesConcept,name))
            if strcmp(stagesConcept{1},name)==1
                stages = [stages, ones(1,duration)+3];
            elseif strcmp(stagesConcept{2},name)==1
                stages = [stages, ones(1,duration)+2];
            elseif strcmp(stagesConcept{3},name)==1
                stages = [stages, ones(1,duration)+1];
            elseif strcmp(stagesConcept{4},name)==1
                stages = [stages, ones(1,duration)];
            elseif strcmp(stagesConcept{5},name)==1
                stages = [stages, zeros(1,duration)];
            elseif strcmp(stagesConcept{6},name)==1
                stages = [stages, zeros(1,duration)+5];
                %             end
            else
                eventsVector = [eventsVector, eventStruct];
            end
        else
            annotation = 0;
            break;
        end
        
    end
else
    annotation = 0;
end


