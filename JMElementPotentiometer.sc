JMElementPotentiometer : JMMIDIElements {
    var <>msbCC, <>lsbCC;
    var <>msbCCValue, <>lsbCCValue;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Potentiometer", "PO", elementNumber, midiChannel).initPotentiometer(msbCC, lsbCC)
    }

    initPotentiometer { |msbCC, lsbCC|
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.controlBus = Bus.control(Server.default, 1);
        super.midi14bitReceivers;
    }
}