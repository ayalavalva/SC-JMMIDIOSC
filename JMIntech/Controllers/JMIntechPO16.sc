// JMIntechPO16: A subclass of JMIntechControllers designed for the "Intech Studio PO16" MIDI controller, which features 16 potentiometers

JMIntechPO16 : JMIntechControllers {
    classvar <>classDeviceNumb = 0; // Class variable to keep track of the number of PO16 device instances;
    var <>startCC; // Starting MIDI CC (Control Change) number
    var <>deviceNumb; // Unique device number assigned to each instance

    // Potentiometer instances
    var <>po1, <>po2, <>po3, <>po4;
    var <>po5, <>po6, <>po7, <>po8;
    var <>po9, <>po10, <>po11, <>po12;
    var <>po13, <>po14, <>po15, <>po16;

    var <>deviceOSCpath; // OSC path for the device

    *new { |midiChannel=0, startCC=0, deviceOSCpath="/po16", oscServAddr="127.0.0.1", oscServPort=9000, postMIDIOSC=false|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance
        
        ^super.new.init("Intech Studio PO16", "PO16", midiChannel, oscServAddr, oscServPort, postMIDIOSC).initPO16(startCC, deviceOSCpath) // Call  superclass's init method
    }

    // initPO16: Initializes the PO16 device settings, specifically setting up the 16 potentiometers
    initPO16 { |startCC, deviceOSCpath|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC; // Set the starting MIDI CC number for the potentiometers
        this.deviceOSCpath = deviceOSCpath; // Set the OSC path for the device

        // Assign 16 potentiometer instances in a loop
        (1..16).do { |i|
            var msb = this.startCC + (i - 1);
            var lsb = this.startCC + 16 + (i - 1);
            var po = JMElementPotentiometer.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, msb, lsb);
            this.perform(("po" ++ i.asString ++ "_").asSymbol, po;);
        };
    }
}