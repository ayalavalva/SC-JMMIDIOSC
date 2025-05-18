JMElementPotentiometer : JMMIDIElement {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue = 0, <>lsbCCValue = 0;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC, msbCC, lsbCC|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Potentiometer", "PO", elementNumber, midiChannel, deviceOSCpath, postMIDIOSC).initPotentiometer(msbCC, lsbCC)
    }

    initPotentiometer { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.elementOSCpath = "/po" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";
        
        this.midiReceiver = JMMIDI14bitReceiver(this);
        this.prReceiveMidiValue;

        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    prSendCtrlBusValuetoOSClabel2 {
        JMOSCManager.getSharedInstance.send(this.deviceOSCpath ++ this.label2OSCpath, (this.controlBus.ctrlBusValue * 100).asInteger);
    }
}