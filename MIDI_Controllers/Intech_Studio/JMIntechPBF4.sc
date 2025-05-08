// JMIntechPBF4: A subclass of JMIntechController designed specifically for the "Intech Studio PBF4" MIDI controller, which features 4 potentiometers, 4 faders and 4 buttons

JMIntechPBF4 : JMIntechController {
    classvar <>classDeviceNumb = 0; // // Class variable to keep track of the number of PBF4 device instances
    var <>startCC; // Starting MIDI CC (Control Change) number 
    var <>deviceNumb; // Unique device number assigned to each instance

    // Potentiometer, fader and button instances
    var <>po1, <>po2, <>po3, <>po4;
    var <>fa1, <>fa2, <>fa3, <>fa4;
    var <>bu1, <>bu2, <>bu3, <>bu4;

    var <>deviceOSCpath; // OSC path for the device

    *new { |midiChannel=0, startCC=64, deviceOSCpath="/pbf4", oscServAddr="127.0.0.1", oscServPort=9000, postMIDIOSC=false|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance.

        ^super.new.init("Intech Studio PBF4", "PBF4", midiChannel, oscServAddr, oscServPort, postMIDIOSC).initPBF4(startCC, deviceOSCpath) // Call  superclass's init method
    }

    // initPBF4: Initializes the PO16 device settings, specifically setting up the 4 potentiometers, 4 faders and 4 buttons
    initPBF4 { |startCC, deviceOSCpath|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC; // Set the starting MIDI CC number for the potentiometers
        this.deviceOSCpath = deviceOSCpath ++ "_" ++ this.deviceNumb; // Set the OSC path for the device
        
        // Assign 4 potentiometer instances in a loop
        (1..4).do { |i|
            var msb = this.startCC + 0 + (i - 1);
            var lsb = this.startCC + 12 + (i - 1);
            var po = JMElementPotentiometer.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, msb, lsb);
            this.perform(("po" ++ i.asString ++ "_").asSymbol, po;);
        };

        // Assign 4 fader instances in a loop
        (1..4).do { |i|
            var msb = this.startCC + 4 + (i - 1);
            var lsb = this.startCC + 16 + (i - 1);
            var fa = JMElementFader.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, msb, lsb);
            this.perform(("fa" ++ i.asString ++ "_").asSymbol, fa;);
        };

        // Assign 16 button instances in a loop
        (1..4).do { |i|
            var cc = this.startCC + 8 + (i - 1);
            var bu = JMElementButton.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, cc);
            this.perform(("bu" ++ i.asString ++ "_").asSymbol, bu;);
        };
    }
}