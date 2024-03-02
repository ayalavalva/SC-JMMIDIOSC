JMElementEncoder : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, controller, cc|
        ^super.new.init(deviceFullName, deviceShortName, deviceNumb, "Encoder", "EN", elementNumber, midiChannel, controller).initEncoder(cc)
    }

    initEncoder { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);
        super.midi7bitReceiver;
    }    
}