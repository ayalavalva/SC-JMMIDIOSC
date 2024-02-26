JMMIntechControllers {
    var <>name, <>midiChannel, <>oscServAddr, <>oscServPort, <>oscRecvPort;

    *new { |name, midiChannel, oscServAddr, oscServPort, oscRecvPort|
        ^super.new.init(name, midiChannel, oscServAddr, oscServPort, oscRecvPort)
    }

    init { |name, midiChannel, oscServAddr, oscServPort, oscRecvPort|
        this.name = name;
        this.midiChannel = midiChannel;
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscRecvPort = oscRecvPort;
    }

}

JMMIntechPO16 : JMMIntechControllers {
    var <>startmsbCC, <>lsbCCoffset;
    var <>elementArray;

    *new { |midiChannel=0, startmsbCC=0, lsbCCoffset=16, oscServAddr="127.0.0.1", oscServPort=57120, oscRecvPort=9000|
        ^super.new.init(name, midiChannel, oscServAddr, oscServPort, oscRecvPort).initPO16(startmsbCC, lsbCCoffset)
    }

    initPO16 { |startmsbCC, lsbCCoffset|
        this.name="Intech Studio PO16";
        this.startmsbCC = startmsbCC;
        this.lsbCCoffset = lsbCCoffset;
        this.elementArray = Array.new(16);
        this.initElements;
    }

    initElements {
        16.do { |i|
            var elementNumber = i + 1;
            var msbCC = this.startmsbCC + i;
            var lsbCC = msbCC + this.lsbCCoffset;
            var element = JMMElementPotentiometer.new(this.name, elementNumber, this.midiChannel, msbCC, lsbCC);

            this.elementArray.add(element);

            // Print diagnostic information for each potentiometer
            ("Intech Studio PO16 Potentiometer" + elementNumber + "MIDI Channel" + this.midiChannel + "MSBCC" + msbCC + "LSBCC" + lsbCC).postln;
        };
    }

    size {
        ^this.elementArray.size;
    }

    // method to remove all elements from the array

    
}


JMMMIDIElements {
    var <>name, <>elementNumber, <>midiChannel;

    *new { |name, elementNumber, midiChannel|
        ^super.new(name, elementNumber, midiChannel)
    }
}

JMMElementPotentiometer {

    var <>name, <>elementNumber, <>midiChannel, <>msbCC, <>lsbCC;
    var <>msb, <>lsb;
    var <>msbUpdated = false, <>lsbUpdated = false;
    var <>busVarName, <>controlBus;

    *new { |name, elementNumber, midiChannel, msbCC, lsbCC|
        ^super.new.init(name, elementNumber, midiChannel, msbCC, lsbCC)
    }

    init { |name, elementNumber, midiChannel, msbCC, lsbCC|
        this.name = name;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
        this.msbCC = msbCC;
        this.lsbCC = lsbCC;
        this.busVarName = "controlBus%PO%".format(this.name, this.elementNumber).asSymbol;
        this.controlBus = Bus.control(Server.default, 1);
        currentEnvironment.put(this.busVarName, this.controlBus);

        this.midi14bit; // do I need to return here?
    }

    midi14bit {
        MIDIdef.cc("%_Pot%_msb".format(this.name, this.elementNumber), { |val|
            this.msb = val;
            this.msbUpdated = true;
            this.updateCombinedValue;
        }, ccNum: this.msbCC, chan: this.midiChannel);

        MIDIdef.cc("%_Pot%_lsb".format(this.name, this.elementNumber), { |val|
            this.lsb = val;
            this.lsbUpdated = true;
            this.updateCombinedValue;
        }, ccNum: this.lsbCC, chan: this.midiChannel);
    }

    updateCombinedValue {
        if (this.msbUpdated && this.lsbUpdated) {
            var combinedMappedValue = ((this.msb << 7) + this.lsb).linlin(0, 16383, 0, 1);
            this.controlBus.set(combinedMappedValue); // Send a smoothen combined value to the control bus
            "% Potentiometer % MIDI channel % msbCC % lsbCC %: %".format(this.name, this.elementNumber, this.midiChannel, this.msbCC, this.lsbCC, combinedMappedValue).postln; // Print potentiometer number and value
            // ~sendToTouchOSC.value("/po16/po" ++ (elementNumber).asString, combinedValue.linlin(0, 16383, 0, 1));
            this.msbUpdated = false;
            this.lsbUpdated = false;
        };
    }

    // Method to 
}

JMMOSCManager {
    var <>oscServAddr, <>oscServPort, <>oscRecvPort, <>oscAddr;

    *new {|oscServAddr, oscServPort, oscRecvPort|
        ^super.new.init(oscServAddr, oscServPort, oscRecvPort)
    }

    init { // |oscServAddr, oscServPort, oscRecvPort|
        oscServAddr = this.oscServAddr;
        oscServPort = this.oscServPort;
        oscRecvPort = this.oscRecvPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
        // Initialize OSC communication here
    }
}