JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, controller, cc|
        ^super.new.init(deviceFullName, deviceShortName, deviceNumb, "Button", "BU", elementNumber, midiChannel, controller).initButton(cc)
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