JMElementPotentiometer : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue = 0, <>lsbCCValue = 0;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, msbCC, lsbCC|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Potentiometer", "PO", elementNumber, midiChannel, deviceOSCpath).initPotentiometer(msbCC, lsbCC)
    }

    initPotentiometer { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.elementOSCpath = "/po" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";
        
        super.midi14bitReceivers;
        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }
}