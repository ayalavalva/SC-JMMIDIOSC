JMElementFader : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>elementOSCpath;
    var <>label1OSCpath;
    var <>label2OSCpath;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, msbCC, lsbCC|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Fader", "FA", elementNumber, midiChannel, deviceOSCpath).initFader(msbCC, lsbCC)
    }

    initFader { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);
        this.elementOSCpath = "/fa" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";
        super.midi14bitReceivers;
    }
}