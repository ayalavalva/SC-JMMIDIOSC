JMIntechController {
    var <>deviceFullName, <>deviceShortName, <>deviceNumb, <>midiChannel, <>oscServAddr, <>oscServPort, <>postMIDIOSC; // Device identifiers
    var <>elementDict; // Dictionary to store element objects
    var <>busValueDict; // Dictionary to store bus values for some elements 

    *new { |deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort, postMIDIOSC|
        ^super.new.init(deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort)
    }

    init { |deviceFullName, deviceShortName, midiChannel, oscServAddr, oscServPort, postMIDIOSC|
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.midiChannel = midiChannel;
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.postMIDIOSC = postMIDIOSC; // Set the postMIDIOSC flag to determine whether to post MIDI and OSC messages
    }

    // Enables or disables OSC sending for specified element keys, controlling whether MIDI values are forwarded as OSC messages.
    midiOSC { |elementKeys, enableFlag = true|
        elementKeys.do { |key|
            var element = this.elementDict.at(key);
            element.oscSendEnabled = enableFlag;
        };
    }

    // Sends OSC messages for specified element keys, using the provided OSC path and value.
    sendtoOSClabel1 { |args|
        args.keysValuesDo({ |element, value|
            this.perform(element).sendtoOSClabel1(value);
        });
    }
}