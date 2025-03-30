JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Button", "BU", elementNumber, midiChannel, deviceOSCpath, postMIDIOSC).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.elementOSCpath = "/bu" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";

        super.midi7bitReceiver;
        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    // Handles the conversion of MIDI values to control bus values (bypasses the super method in JMMIDIElements)
    midiValueToControlBusValue {
        this.busValue = this.ccValue.linlin(0, 127, this.lowValue, this.highValue);
    }
}