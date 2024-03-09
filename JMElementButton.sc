JMElementButton : JMMIDIElements {
    var <>cc;
    var <>ccValue;
    var <>elementOSCpath;
    var <>label1OSCpath;
    var <>label2OSCpath;
    var <>midiToOSCValue;
    var <>controlBus;
    var <>oscSendEnabled = false;

    *new { |controller, deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel, deviceOSCpath, cc|
        ^super.new.init(controller, deviceFullName, deviceShortName, deviceNumb, "Button", "BU", elementNumber, midiChannel, deviceOSCpath).initButton(cc)
    }

    initButton { |cc|
        this.cc = cc;
        this.controlBus = Bus.control(Server.default, 1);
        this.elementOSCpath = "/bu" ++ this.elementNumber;
        this.label1OSCpath = this.elementOSCpath ++ "_lb1";
        this.label2OSCpath = this.elementOSCpath ++ "_lb2";
        super.midi7bitReceiver;
    }

    getMIDIValue {
        ^this.midiToOSCValue;
    } 
}