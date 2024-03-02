JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Button", "BU", elementNumber, midiChannel).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1); // Control bus is now directly accessible
        super.midi7bitReceiver;
    }

    getMIDIValue {
        ^this.midiToOSCValue;
    } 
}