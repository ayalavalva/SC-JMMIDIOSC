JMElementButton : JMMIDIElement {
    var <>cc;
    // var <>ccValue; // Replaced by midiValue in JMMIDIElement

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, postMIDIOSC, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Button", "BU", elementNumber, midiChannel, deviceOSCpath, postMIDIOSC).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.elementOSCpath = "/bu" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";

        this.midiReceiver = JMMIDI7bitReceiver.new(this);
        this.updateMidiValue;
        
        super.receiveOSCValuetoControlBus; // also receive values from OSC, updates the control bus and allows to get the OSC value in patch code
    }

    convertMidiValueToNormalizedControlBusValue { |midiValue|
        this.busValue = this.midiValue.linlin(0, 127, this.lowValue, this.highValue);
    }


    sendBusValuetoOSClabel2 {
    }
}