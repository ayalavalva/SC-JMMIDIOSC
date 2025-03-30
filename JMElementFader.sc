JMElementFader : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue = 0, <>lsbCCValue = 0;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC, msbCC, lsbCC|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Fader", "FA", elementNumber, midiChannel, deviceOSCpath, postMIDIOSC).initFader(msbCC, lsbCC)
    }

    initFader { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.elementOSCpath = "/fa" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";

        super.midi14bitReceivers;
        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }
}